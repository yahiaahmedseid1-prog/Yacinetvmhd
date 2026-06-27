package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CodeDao {
    @Query("SELECT * FROM codes ORDER BY isRedeemed ASC, createdAt DESC")
    fun getAllCodes(): Flow<List<CodeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCode(code: CodeEntity): Long

    @Update
    suspend fun updateCode(code: CodeEntity)

    @Delete
    suspend fun deleteCode(code: CodeEntity)

    @Query("DELETE FROM codes WHERE id = :id")
    suspend fun deleteCodeById(id: Int)
}
