package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ReceiptViewModel(private val repository: ReceiptRepository) : ViewModel() {

    val allReceipts: StateFlow<List<ReceiptEntity>> = repository.allReceipts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val companySettings: StateFlow<CompanySettingsEntity> = repository.companySettings
        .map { it ?: CompanySettingsEntity() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CompanySettingsEntity())

    // UI state for creating a receipt
    private val _studentName = MutableStateFlow("")
    val studentName = _studentName.asStateFlow()

    private val _productName = MutableStateFlow("")
    val productName = _productName.asStateFlow()

    private val _purchasedDate = MutableStateFlow(System.currentTimeMillis())
    val purchasedDate = _purchasedDate.asStateFlow()

    private val _itemsList = MutableStateFlow<List<ReceiptItem>>(emptyList())
    val itemsList = _itemsList.asStateFlow()

    private val _discountValue = MutableStateFlow(0.0)
    val discountValue = _discountValue.asStateFlow()

    private val _discountType = MutableStateFlow("PERCENTAGE") // "PERCENTAGE" or "FIXED"
    val discountType = _discountType.asStateFlow()

    private val _remarks = MutableStateFlow("")
    val remarks = _remarks.asStateFlow()

    val allCategories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _category = MutableStateFlow("")
    val category = _category.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeDefaultSettingsIfEmpty()
        }
    }

    fun setStudentName(name: String) { _studentName.value = name }
    fun setProductName(name: String) { _productName.value = name }
    fun setPurchasedDate(date: Long) { _purchasedDate.value = date }
    fun setDiscountValue(value: Double) { _discountValue.value = value }
    fun setDiscountType(type: String) { _discountType.value = type }
    fun setRemarks(remarks: String) { _remarks.value = remarks }
    fun setCategory(category: String) { _category.value = category }

    fun addItem(name: String, price: Double) {
        if (name.isNotBlank() && price >= 0.0) {
            _itemsList.value = _itemsList.value + ReceiptItem(name, price)
        }
    }

    fun removeItem(index: Int) {
        val current = _itemsList.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _itemsList.value = current
        }
    }

    fun clearReceiptBuilder() {
        _studentName.value = ""
        _productName.value = ""
        _purchasedDate.value = System.currentTimeMillis()
        _itemsList.value = emptyList()
        _discountValue.value = 0.0
        _discountType.value = "PERCENTAGE"
        _remarks.value = ""
        _category.value = allCategories.value.firstOrNull()?.name ?: ""
    }

    fun addCategory(name: String) {
        viewModelScope.launch {
            val trimmed = name.trim()
            if (trimmed.isNotBlank()) {
                repository.insertCategory(CategoryEntity(trimmed))
                if (_category.value.isBlank()) {
                    _category.value = trimmed
                }
            }
        }
    }

    fun removeCategory(name: String) {
        viewModelScope.launch {
            repository.deleteCategory(CategoryEntity(name))
            if (_category.value == name) {
                _category.value = allCategories.value.firstOrNull { it.name != name }?.name ?: ""
            }
        }
    }

    // Settings actions
    fun updateSettings(
        companyName: String,
        companyAddress: String,
        companyPhone: String,
        currencySymbol: String,
        logoText: String,
        logoSeed: Int,
        signatureName: String,
        signatureDesignation: String,
        signaturePointsJson: String
    ) {
        viewModelScope.launch {
            val updated = CompanySettingsEntity(
                id = 1,
                companyName = companyName,
                companyAddress = companyAddress,
                companyPhone = companyPhone,
                currencySymbol = currencySymbol,
                logoText = logoText,
                logoSeed = logoSeed,
                signatureName = signatureName,
                signatureDesignation = signatureDesignation,
                signaturePointsJson = signaturePointsJson
            )
            repository.saveCompanySettings(updated)
        }
    }

    fun generateReceiptAndSave(onSuccess: (ReceiptEntity) -> Unit) {
        viewModelScope.launch {
            val settings = companySettings.value
            val currentItems = _itemsList.value
            val subtotalCalc = currentItems.sumOf { it.price }
            val discountAmt = if (_discountType.value == "PERCENTAGE") {
                subtotalCalc * (_discountValue.value / 100.0)
            } else {
                _discountValue.value
            }
            val finalTotal = (subtotalCalc - discountAmt).coerceAtLeast(0.0)
            
            // Format recipe number nicely starting with #TCC followed by 5 random digits
            val random5Num = String.format("%05d", (0..99999).random())
            val receiptNumber = "#TCC$random5Num"

            val newReceipt = ReceiptEntity(
                receiptNo = receiptNumber,
                studentName = _studentName.value.trim().ifBlank { "Guest Student" },
                productName = _productName.value.trim().ifBlank { "General Purchase" },
                purchasedDate = _purchasedDate.value,
                itemsJson = ReceiptItem.serializeList(currentItems),
                subtotal = subtotalCalc,
                discountValue = _discountValue.value,
                discountType = _discountType.value,
                totalAmount = finalTotal,
                remarks = _remarks.value.trim(),
                category = _category.value,
                companyName = settings.companyName,
                companyAddress = settings.companyAddress,
                companyPhone = settings.companyPhone,
                currencySymbol = settings.currencySymbol,
                logoText = settings.logoText,
                logoSeed = settings.logoSeed,
                signatureName = settings.signatureName,
                signatureDesignation = settings.signatureDesignation,
                signaturePointsJson = settings.signaturePointsJson
            )

            repository.insertReceipt(newReceipt)
            onSuccess(newReceipt)
        }
    }

    fun deleteReceipt(receipt: ReceiptEntity) {
        viewModelScope.launch {
            repository.deleteReceipt(receipt)
        }
    }
    
    fun deleteReceiptById(id: Int) {
        viewModelScope.launch {
            repository.deleteReceiptById(id)
        }
    }
}

class ReceiptViewModelFactory(private val repository: ReceiptRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReceiptViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReceiptViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
