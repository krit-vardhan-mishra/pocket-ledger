package com.just_for_fun.pocketledger.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.just_for_fun.pocketledger.data.model.enums.Category
import com.just_for_fun.pocketledger.data.model.enums.TransactionType

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val category: Category,
    val note: String,
    val date: Long,        // epoch millis
    val createdAt: Long = System.currentTimeMillis()
)