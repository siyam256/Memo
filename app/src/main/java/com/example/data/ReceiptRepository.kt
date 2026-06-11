package com.example.data

import kotlinx.coroutines.flow.Flow

class ReceiptRepository(private val receiptDao: ReceiptDao) {
    val allReceipts: Flow<List<ReceiptEntity>> = receiptDao.getAllReceipts()
    val companySettings: Flow<CompanySettingsEntity?> = receiptDao.getCompanySettings()
    val allCategories: Flow<List<CategoryEntity>> = receiptDao.getAllCategories()

    suspend fun getReceiptById(id: Int): ReceiptEntity? {
        return receiptDao.getReceiptById(id)
    }

    suspend fun insertReceipt(receipt: ReceiptEntity) {
        receiptDao.insertReceipt(receipt)
    }

    suspend fun deleteReceipt(receipt: ReceiptEntity) {
        receiptDao.deleteReceipt(receipt)
    }

    suspend fun deleteReceiptById(id: Int) {
        receiptDao.deleteReceiptById(id)
    }

    suspend fun saveCompanySettings(settings: CompanySettingsEntity) {
        receiptDao.insertOrUpdateCompanySettings(settings)
    }

    suspend fun insertCategory(category: CategoryEntity) {
        receiptDao.insertCategory(category)
    }

    suspend fun deleteCategory(category: CategoryEntity) {
        receiptDao.deleteCategory(category)
    }

    suspend fun initializeDefaultSettingsIfEmpty() {
        // Run check sync
        val current = receiptDao.getCompanySettingsSync()
        if (current == null) {
            receiptDao.insertOrUpdateCompanySettings(CompanySettingsEntity())
        }
    }
}
