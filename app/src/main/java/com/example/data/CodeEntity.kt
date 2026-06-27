package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "codes")
data class CodeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val code: String,
    val category: String,
    val expiryDate: Long = 0L, // 0 means no expiry
    val isRedeemed: Boolean = false,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val type: String = "COUPON", // "COUPON", "OTP_2FA", "SERIAL_KEY"
    val otpSecret: String = "" // Used if type is OTP_2FA
)
