package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceiptDao {
    @Query("SELECT * FROM receipts ORDER BY purchasedDate DESC, id DESC")
    fun getAllReceipts(): Flow<List<ReceiptEntity>>

    @Query("SELECT * FROM receipts WHERE id = :id")
    suspend fun getReceiptById(id: Int): ReceiptEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(receipt: ReceiptEntity)

    @Delete
    suspend fun deleteReceipt(receipt: ReceiptEntity)

    @Query("DELETE FROM receipts WHERE id = :id")
    suspend fun deleteReceiptById(id: Int)

    @Query("SELECT * FROM company_settings WHERE id = 1")
    fun getCompanySettings(): Flow<CompanySettingsEntity?>

    @Query("SELECT * FROM company_settings WHERE id = 1")
    suspend fun getCompanySettingsSync(): CompanySettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCompanySettings(settings: CompanySettingsEntity)

    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)
}
