package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CodeEntity
import com.example.data.CodeRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Random
import java.util.regex.Pattern

class CodeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: CodeRepository
    val allCodes: StateFlow<List<CodeEntity>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = CodeRepository(database.codeDao())
        allCodes = repository.allCodes.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        // Start 2FA clock loop
        start2FaClock()
    }

    // Extraction / Retrieval states
    private val _extractedCodes = MutableStateFlow<List<ExtractedCode>>(emptyList())
    val extractedCodes: StateFlow<List<ExtractedCode>> = _extractedCodes.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    // Interactive scanner simulation states
    private val _scannerState = MutableStateFlow<ScannerState>(ScannerState.Idle)
    val scannerState: StateFlow<ScannerState> = _scannerState.asStateFlow()

    // 2FA Clock States
    private val _otpTimeRemaining = MutableStateFlow(30)
    val otpTimeRemaining: StateFlow<Int> = _otpTimeRemaining.asStateFlow()

    private val _currentInterval = MutableStateFlow(0L)
    val currentInterval: StateFlow<Long> = _currentInterval.asStateFlow()

    private fun start2FaClock() {
        viewModelScope.launch {
            while (true) {
                val systemTimeSec = System.currentTimeMillis() / 1000
                val remaining = (30 - (systemTimeSec % 30)).toInt()
                val interval = systemTimeSec / 30
                _otpTimeRemaining.value = remaining
                _currentInterval.value = interval
                delay(1000)
            }
        }
    }

    // Helper to generate 2FA OTP deterministically
    fun getOtpForSecret(secret: String, interval: Long): String {
        if (secret.isBlank()) return "000000"
        val cleanSecret = secret.trim().uppercase()
        val seed = cleanSecret.hashCode() * 31 + interval
        val rand = Random(seed)
        val otpVal = 100000 + rand.nextInt(900000) // 6 digits
        return otpVal.toString().take(6)
    }

    fun setInputText(text: String) {
        _inputText.value = text
    }

    // Smart retrieval logic: Extracts potential coupons, verification codes, or URLs from text
    fun retrieveCodesFromText(text: String) {
        _inputText.value = text
        if (text.isBlank()) {
            _extractedCodes.value = emptyList()
            return
        }

        val list = mutableListOf<ExtractedCode>()

        // 1. Match Promos/Coupons (e.g., uppercase words with numbers or dashes, length 4 to 16)
        // Avoiding matches of plain words like "HELLO" by requiring at least one number, or dash, or being clearly uppercase-only coupon pattern
        val couponPattern = Pattern.compile("\\b[A-Z0-9\\-_]{4,16}\\b")
        val couponMatcher = couponPattern.matcher(text)
        while (couponMatcher.find()) {
            val codeStr = couponMatcher.group()
            // Ensure it's not purely a number (handled by OTP) and contains letters/numbers
            val hasLetters = codeStr.any { it.isLetter() }
            val hasDigits = codeStr.any { it.isDigit() }
            val isKnownShorthand = codeStr in listOf("NOON", "AMZN", "SOUQ", "PLAY", "STEAM", "XBOX", "SONY", "DISC", "SAVE")
            
            if ((hasLetters && (hasDigits || codeStr.contains("-") || codeStr.contains("_"))) || (hasLetters && codeStr.length >= 5) || isKnownShorthand) {
                // Avoid duplicating
                if (list.none { it.code == codeStr }) {
                    list.add(ExtractedCode(codeStr, "قسيمة خصم / عرض", "COUPON"))
                }
            }
        }

        // 2. Match Verification Codes / OTPs (purely 4-8 digit numbers)
        val otpPattern = Pattern.compile("\\b\\d{4,8}\\b")
        val otpMatcher = otpPattern.matcher(text)
        while (otpMatcher.find()) {
            val codeStr = otpMatcher.group()
            if (list.none { it.code == codeStr }) {
                list.add(ExtractedCode(codeStr, "رمز تحقق مؤقت", "OTP"))
            }
        }

        // 3. Match URLs (Redemption Links)
        val urlPattern = Pattern.compile("https?://[a-zA-Z0-9.\\-_/]+\\b")
        val urlMatcher = urlPattern.matcher(text)
        while (urlMatcher.find()) {
            val urlStr = urlMatcher.group()
            if (urlStr.contains("redeem", ignoreCase = true) || 
                urlStr.contains("claim", ignoreCase = true) || 
                urlStr.contains("promo", ignoreCase = true) ||
                urlStr.contains("coupon", ignoreCase = true)
            ) {
                if (list.none { it.code == urlStr }) {
                    list.add(ExtractedCode(urlStr, "رابط استرداد مباشر", "URL"))
                }
            }
        }

        _extractedCodes.value = list
    }

    // Database Actions
    fun saveCode(
        title: String,
        code: String,
        category: String,
        expiryDate: Long = 0L,
        notes: String = "",
        type: String = "COUPON",
        otpSecret: String = ""
    ) {
        viewModelScope.launch {
            val entity = CodeEntity(
                title = title.ifBlank { "رمز غير معنون" },
                code = code,
                category = category,
                expiryDate = expiryDate,
                notes = notes,
                type = type,
                otpSecret = otpSecret
            )
            repository.insert(entity)
        }
    }

    fun toggleRedeemedStatus(entity: CodeEntity) {
        viewModelScope.launch {
            repository.update(entity.copy(isRedeemed = !entity.isRedeemed))
        }
    }

    fun deleteCode(entity: CodeEntity) {
        viewModelScope.launch {
            repository.delete(entity)
        }
    }

    // Simulated QR / Barcode Scanner engine
    fun simulateScan(mockCodeValue: String, typeName: String) {
        viewModelScope.launch {
            _scannerState.value = ScannerState.Scanning
            delay(1500) // Visual scan effect
            _scannerState.value = ScannerState.Success(mockCodeValue, typeName)
        }
    }

    fun resetScanner() {
        _scannerState.value = ScannerState.Idle
    }
}

// Extracted code details from text parser
data class ExtractedCode(
    val code: String,
    val description: String,
    val type: String // COUPON, OTP, URL
)

// Scanner visual states
sealed interface ScannerState {
    object Idle : ScannerState
    object Scanning : ScannerState
    data class Success(val code: String, val type: String) : ScannerState
}
