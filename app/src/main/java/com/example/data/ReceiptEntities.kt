package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "receipts")
data class ReceiptEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val receiptNo: String,
    val studentName: String,
    val productName: String,
    val purchasedDate: Long,
    val itemsJson: String, // serialized item list: format "item1_name:100.0|item2_name:200.0" (using safe delimiters)
    val subtotal: Double,
    val discountValue: Double,
    val discountType: String, // "PERCENTAGE" or "FIXED"
    val totalAmount: Double,
    val remarks: String,
    val category: String = "Tuition",
    // Snapshot of settings when generated
    val companyName: String,
    val companyAddress: String,
    val companyPhone: String,
    val currencySymbol: String,
    val logoText: String,
    val logoSeed: Int,
    val signatureName: String,
    val signatureDesignation: String,
    val signaturePointsJson: String // serialized handdrawn signature points
)

@Entity(tableName = "company_settings")
data class CompanySettingsEntity(
    @PrimaryKey val id: Int = 1, // Only 1 settings row exists
    val companyName: String = "Spark Academy",
    val companyAddress: String = "Plot 14, Block B, Banani, Dhaka",
    val companyPhone: String = "+880 1712-345678",
    val currencySymbol: String = "৳",
    val logoText: String = "SA",
    val logoSeed: Int = 1,
    val signatureName: String = "Md. Arif Rahman",
    val signatureDesignation: String = "Accounts Officer",
    val signaturePointsJson: String = "" // Serialized points
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val name: String
)

// Helper class for handling items
data class ReceiptItem(
    val name: String,
    val price: Double
) {
    companion object {
        fun serializeList(items: List<ReceiptItem>): String {
            return items.joinToString(";") { "${escape(it.name)}:${it.price}" }
        }

        fun deserializeList(serialized: String): List<ReceiptItem> {
            if (serialized.isBlank()) return emptyList()
            return try {
                serialized.split(";").mapNotNull {
                    val parts = it.split(":")
                    if (parts.size >= 2) {
                        ReceiptItem(unescape(parts[0]), parts[1].toDoubleOrNull() ?: 0.0)
                    } else null
                }
            } catch (e: Exception) {
                emptyList()
            }
        }

        private fun escape(s: String): String = s.replace(":", "\\:").replace(";", "\\;")
        private fun unescape(s: String): String = s.replace("\\:", ":").replace("\\;", ";")
    }
}
