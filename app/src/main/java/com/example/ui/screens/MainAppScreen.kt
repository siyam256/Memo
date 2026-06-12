package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.ReceiptViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: ReceiptViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    
    // Dialog states
    var viewReceiptDialogItem by remember { mutableStateOf<ReceiptEntity?>(null) }
    var successGeneratedReceipt by remember { mutableStateOf<ReceiptEntity?>(null) }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_app_logo_vector),
                            contentDescription = "App Logo",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "RECEIPT WRITER",
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 15.sp,
                            color = Color(0xFF1E293B),
                            letterSpacing = 2.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                ),
                modifier = Modifier.drawBehind {
                    val strokeWidth = 1f
                    val y = size.height - strokeWidth
                    drawLine(
                        color = Color(0xFFE2E8F0),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = strokeWidth
                    )
                }
            )
        },
        bottomBar = {
            NavigationBar(
                tonalElevation = 0.dp,
                containerColor = Color.White,
                windowInsets = WindowInsets.navigationBars,
                modifier = Modifier.drawBehind {
                    val strokeWidth = 1f
                    drawLine(
                        color = Color(0xFFE2E8F0),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = strokeWidth
                    )
                }
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.AddCard, contentDescription = "রিসিট তৈরি") },
                    label = { Text("রিসিট তৈরি", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF4F46E5),
                        unselectedIconColor = Color(0xFF94A3B8),
                        selectedTextColor = Color(0xFF4F46E5),
                        unselectedTextColor = Color(0xFF94A3B8),
                        indicatorColor = Color(0xFFEEF2FF)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.History, contentDescription = "রিসিটসমূহ") },
                    label = { Text("রিসিটসমূহ", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF4F46E5),
                        unselectedIconColor = Color(0xFF94A3B8),
                        selectedTextColor = Color(0xFF4F46E5),
                        unselectedTextColor = Color(0xFF94A3B8),
                        indicatorColor = Color(0xFFEEF2FF)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Business, contentDescription = "কোম্পানি সেটিংস") },
                    label = { Text("কোম্পানি সেটিংস", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF4F46E5),
                        unselectedIconColor = Color(0xFF94A3B8),
                        selectedTextColor = Color(0xFF4F46E5),
                        unselectedTextColor = Color(0xFF94A3B8),
                        indicatorColor = Color(0xFFEEF2FF)
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC)) // Soft, premium background color
        ) {
            when (selectedTab) {
                0 -> CreateReceiptTab(
                    viewModel = viewModel,
                    onReceiptGenerated = { generated ->
                        successGeneratedReceipt = generated
                    }
                )
                1 -> HistoryReceiptsTab(
                    viewModel = viewModel,
                    onViewReceipt = { viewReceiptDialogItem = it }
                )
                2 -> CompanySettingsTab(
                    viewModel = viewModel
                )
            }
        }
    }

    // Success dialog after generator
    if (successGeneratedReceipt != null) {
        ReceiptPreviewDialog(
            receipt = successGeneratedReceipt!!,
            onDismiss = { successGeneratedReceipt = null },
            isSuccessFlow = true
        )
    }

    // Viewing receipt from historical database
    if (viewReceiptDialogItem != null) {
        ReceiptPreviewDialog(
            receipt = viewReceiptDialogItem!!,
            onDismiss = { viewReceiptDialogItem = null },
            isSuccessFlow = false
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorialTextField(
    value: String,
    onValueChange: (String) -> Unit,
    labelText: String,
    hintText: String = "",
    leadingIcon: ImageVector? = null,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default,
    trailingIcon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(start = 2.dp)
        ) {
            Text(
                text = labelText.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8), // slate-400
                letterSpacing = 1.sp
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            readOnly = readOnly,
            singleLine = singleLine,
            maxLines = maxLines,
            placeholder = if (hintText.isNotEmpty()) { { Text(hintText, color = Color(0xFF94A3B8), fontSize = 13.sp) } } else null,
            keyboardOptions = keyboardOptions,
            leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.size(18.dp)) } },
            trailingIcon = trailingIcon,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF8FAFC),
                unfocusedContainerColor = Color(0xFFF8FAFC),
                focusedBorderColor = Color(0xFF6366F1),
                unfocusedBorderColor = Color(0xFFE2E8F0),
                focusedTextColor = Color(0xFF1E293B),
                unfocusedTextColor = Color(0xFF1E293B)
            )
        )
    }
}

@Composable
fun CreateReceiptTab(
    viewModel: ReceiptViewModel,
    onReceiptGenerated: (ReceiptEntity) -> Unit
) {
    val context = LocalContext.current
    
    // States from viewmodel
    val studentName by viewModel.studentName.collectAsStateWithLifecycle()
    val productName by viewModel.productName.collectAsStateWithLifecycle()
    val purchasedDate by viewModel.purchasedDate.collectAsStateWithLifecycle()
    val itemsList by viewModel.itemsList.collectAsStateWithLifecycle()
    val discountValue by viewModel.discountValue.collectAsStateWithLifecycle()
    val discountType by viewModel.discountType.collectAsStateWithLifecycle()
    val remarks by viewModel.remarks.collectAsStateWithLifecycle()
    val settings by viewModel.companySettings.collectAsStateWithLifecycle()

    // Temporary price item states
    var tempItemName by remember { mutableStateOf("") }
    var tempItemPrice by remember { mutableStateOf("") }

    // Error warnings
    var itemErrorMsg by remember { mutableStateOf<String?>(null) }
    var formErrorMsg by remember { mutableStateOf<String?>(null) }
    var showCategoryDialog by remember { mutableStateOf(false) }

    if (showCategoryDialog) {
        CategoryManagerDialog(
            viewModel = viewModel,
            onDismiss = { showCategoryDialog = false }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RectangleShape,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "১. সাধারণ তথ্য",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4F46E5),
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "GENERAL INFORMATION",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.SansSerif,
                            color = Color(0xFF1F2937)
                        )
                    }

                    EditorialTextField(
                        value = studentName,
                        onValueChange = { viewModel.setStudentName(it) },
                        labelText = "স্টুডেন্টের নাম (Student Name)",
                        hintText = "স্টুডেন্টের পূর্ণ নাম লিখুন",
                        leadingIcon = Icons.Default.Person,
                        modifier = Modifier.fillMaxWidth()
                    )

                    EditorialTextField(
                        value = productName,
                        onValueChange = { viewModel.setProductName(it) },
                        labelText = "কোর্স বা প্রডাক্টের নাম (Course / Product)",
                        hintText = "কোর্স বা প্রডাক্ট যেমন: Advanced Web Development",
                        leadingIcon = Icons.Default.School,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Date selector prefilled
                    val readableDate = SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault()).format(Date(purchasedDate))
                    EditorialTextField(
                        value = readableDate,
                        onValueChange = {},
                        labelText = "ক্রয়ের তারিখ (Purchased Date)",
                        leadingIcon = Icons.Default.CalendarToday,
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { viewModel.setPurchasedDate(System.currentTimeMillis()) }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Reset Date", tint = Color(0xFF6366F1))
                            }
                        }
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.shadow(2.dp, shape = RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "২. প্রাইস টেবিল",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4F46E5),
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "PRICE TABLE ITEMS",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.SansSerif,
                            color = Color(0xFF1F2937)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        EditorialTextField(
                            value = tempItemName,
                            onValueChange = { tempItemName = it },
                            labelText = "বিবরণ (Item Name)",
                            hintText = "যেমন: Monthly Tuition",
                            modifier = Modifier.weight(1.8f)
                        )

                        EditorialTextField(
                            value = tempItemPrice,
                            onValueChange = { tempItemPrice = it },
                            labelText = "টাকা (Fee)",
                            hintText = "0.00",
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                            ),
                            modifier = Modifier.weight(1.1f)
                        )

                        IconButton(
                            onClick = {
                                val priceVal = tempItemPrice.toDoubleOrNull()
                                if (tempItemName.isBlank()) {
                                    itemErrorMsg = "আইটেম বিবরণ খালি রাখা যাবে না!"
                                } else if (priceVal == null || priceVal < 0) {
                                    itemErrorMsg = "সঠিক অংক লিখুন!"
                                } else {
                                    viewModel.addItem(tempItemName.trim(), priceVal)
                                    tempItemName = ""
                                    tempItemPrice = ""
                                    itemErrorMsg = null
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0xFF4F46E5), RoundedCornerShape(12.dp)),
                            colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Item", modifier = Modifier.size(22.dp))
                        }
                    }

                    if (itemErrorMsg != null) {
                        Text(
                            text = itemErrorMsg!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

                    if (itemsList.isEmpty()) {
                        Text(
                            text = "কোন আইটেম যোগ করা হয়নি।",
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        )
                    } else {
                        itemsList.forEachIndexed { index, item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${index + 1}. ${item.name}",
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 14.sp,
                                        color = Color(0xFF1E293B)
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "${settings.currencySymbol}${String.format("%.2f", item.price)}",
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF4F46E5),
                                        fontSize = 14.sp
                                    )
                                    IconButton(
                                        onClick = { viewModel.removeItem(index) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Remove Item",
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.shadow(2.dp, shape = RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "৩. ডিসকাউন্ট ও অতিরিক্ত তথ্য",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4F46E5),
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "DISCOUNT & OVERHEAD OPTIONS",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.SansSerif,
                            color = Color(0xFF1F2937)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        EditorialTextField(
                            value = if (discountValue == 0.0) "" else discountValue.toString(),
                            onValueChange = {
                                val value = it.toDoubleOrNull() ?: 0.0
                                viewModel.setDiscountValue(value)
                            },
                            labelText = "ডিসকাউন্ট মান (Discount)",
                            hintText = "0.00",
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                            ),
                            modifier = Modifier.weight(1.5f)
                        )

                        // Selector for percentage vs fixed
                        Column(
                            modifier = Modifier.weight(1.5f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "টাইপ (Unit)".uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8), // slate-400
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(start = 2.dp)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                                    .background(Color(0xFFF8FAFC)),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clickable { viewModel.setDiscountType("PERCENTAGE") }
                                        .background(if (discountType == "PERCENTAGE") Color(0xFFE0E7FF) else Color.Transparent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "%",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = if (discountType == "PERCENTAGE") Color(0xFF4F46E5) else Color(0xFF64748B)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clickable { viewModel.setDiscountType("FIXED") }
                                        .background(if (discountType == "FIXED") Color(0xFFE0E7FF) else Color.Transparent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = settings.currencySymbol,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = if (discountType == "FIXED") Color(0xFF4F46E5) else Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }

                    EditorialTextField(
                        value = remarks,
                        onValueChange = { viewModel.setRemarks(it) },
                        labelText = "রিসিভ নোট / Remarks (ঐচ্ছিক)",
                        hintText = "যেমন: Paid in Full via Card",
                        leadingIcon = Icons.Default.Notes,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        maxLines = 2
                    )
                }
            }
        }

        item {
            // Error warning box
            if (formErrorMsg != null) {
                Text(
                    text = formErrorMsg!!,
                    color = Color(0xFFEF4444),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
            }

            Button(
                onClick = {
                    if (studentName.isBlank()) {
                        formErrorMsg = "স্টুডেন্টের নাম অবশ্যই দিন!"
                    } else if (itemsList.isEmpty()) {
                        formErrorMsg = "অন্তত একটি আইটেম যোগ করুন!"
                    } else {
                        formErrorMsg = null
                        viewModel.generateReceiptAndSave { generated ->
                            onReceiptGenerated(generated)
                            viewModel.clearReceiptBuilder()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF111827) // Slate-900 / Deep Slate Navy button
                )
            ) {
                Icon(Icons.Default.Receipt, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "ট্যাক্স রিসিট জেনারেট করুন",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.SansSerif
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun HistoryReceiptsTab(
    viewModel: ReceiptViewModel,
    onViewReceipt: (ReceiptEntity) -> Unit
) {
    val receipts by viewModel.allReceipts.collectAsStateWithLifecycle()
    var searchKeyword by remember { mutableStateOf("") }
    
    val filteredReceipts = remember(receipts, searchKeyword) {
        if (searchKeyword.isBlank()) receipts
        else {
            receipts.filter {
                it.studentName.contains(searchKeyword, ignoreCase = true) ||
                it.productName.contains(searchKeyword, ignoreCase = true) ||
                it.receiptNo.contains(searchKeyword, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        OutlinedTextField(
            value = searchKeyword,
            onValueChange = { searchKeyword = it },
            placeholder = { Text("আইডি (#TCCxxxxx), শিক্ষার্থী বা কোর্স দিয়ে খুঁজুন...", fontSize = 11.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RectangleShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.onSurface,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface,
            ),
            trailingIcon = {
                if (searchKeyword.isNotEmpty()) {
                    IconButton(onClick = { searchKeyword = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                    }
                }
            }
        )



        if (filteredReceipts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Receipt,
                        contentDescription = null,
                        modifier = Modifier.size(54.dp),
                        tint = Color(0xFFCBD5E1)
                    )
                    Text(
                        text = if (searchKeyword.isEmpty()) "কোন রিসিট সংরক্ষণ করা নেই।" else "কোন সাদৃশ্যপূর্ণ রিসিট পাওয়া যায়নি।",
                        fontSize = 14.sp,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredReceipts, key = { it.id }) { receipt ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onViewReceipt(receipt) }
                            .shadow(2.dp, shape = RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFEEF2FF), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = receipt.receiptNo,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            color = Color(0xFF4F46E5)
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        val shortCat = receipt.category
                                        Text(
                                            text = shortCat.ifBlank { "General" },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = Color(0xFF475569)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "স্টুডেন্ট: ${receipt.studentName}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = "কোর্স: ${receipt.productName}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(receipt.purchasedDate)),
                                    fontSize = 10.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "${receipt.currencySymbol}${String.format("%.2f", receipt.totalAmount)}",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    color = Color(0xFF4F46E5)
                                )
                                
                                IconButton(
                                    onClick = { viewModel.deleteReceiptById(receipt.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete",
                                        tint = Color(0xFFEF4444)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CompanySettingsTab(
    viewModel: ReceiptViewModel
) {
    val currentSettings by viewModel.companySettings.collectAsStateWithLifecycle()
    
    // Values
    var cName by remember(currentSettings) { mutableStateOf(currentSettings.companyName) }
    var cAddress by remember(currentSettings) { mutableStateOf(currentSettings.companyAddress) }
    var cPhone by remember(currentSettings) { mutableStateOf(currentSettings.companyPhone) }
    var curSymbol by remember(currentSettings) { mutableStateOf(currentSettings.currencySymbol) }
    var lText by remember(currentSettings) { mutableStateOf(currentSettings.logoText) }
    var lSeed by remember(currentSettings) { mutableStateOf(currentSettings.logoSeed) }
    var sigName by remember(currentSettings) { mutableStateOf(currentSettings.signatureName) }
    var sigDesig by remember(currentSettings) { mutableStateOf(currentSettings.signatureDesignation) }
    
    // Signature points
    var sigPoints by remember(currentSettings) {
        mutableStateOf(SignatureSerializer.deserialize(currentSettings.signaturePointsJson))
    }

    var successSaveIndicator by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item {
            Column(
                modifier = Modifier.padding(bottom = 2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "কোম্পানি প্রোফাইল এবং সেটিংস",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = "রিসিটের হেডার, কারেন্সি, লোগো এবং সিগনেচার কাস্টমাইজ করুন। আপনার দেওয়া প্রফেশনাল তথ্যগুলো অফলাইন রিসিট পেপারে প্রদর্শিত হবে।",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B),
                    lineHeight = 18.sp
                )
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.shadow(2.dp, shape = RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "কোম্পানির সাধারণ তথ্য",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4F46E5),
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "OFFICIAL PROFILE",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.SansSerif,
                            color = Color(0xFF1F2937)
                        )
                    }

                    EditorialTextField(
                        value = cName,
                        onValueChange = { cName = it },
                        labelText = "প্রতিষ্ঠানের নাম (Company Name)",
                        hintText = "যেমন: Creative Software",
                        modifier = Modifier.fillMaxWidth()
                    )

                    EditorialTextField(
                        value = cAddress,
                        onValueChange = { cAddress = it },
                        labelText = "প্রতিষ্ঠানের ঠিকানা (Address)",
                        hintText = "যেমন: Road 12, Dhanmondi, Dhaka",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        EditorialTextField(
                            value = cPhone,
                            onValueChange = { cPhone = it },
                            labelText = "ফোন নম্বর (Contact)",
                            hintText = "+8801700...",
                            modifier = Modifier.weight(1.5f)
                        )

                        EditorialTextField(
                            value = curSymbol,
                            onValueChange = { curSymbol = it },
                            labelText = "কারেন্সি (Currency)",
                            hintText = "$",
                            modifier = Modifier.weight(0.8f)
                        )
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.shadow(2.dp, shape = RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "লোগো কাস্টমাইজেশন",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4F46E5),
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "LOGO SETTINGS",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.SansSerif,
                            color = Color(0xFF1F2937)
                        )
                    }

                    EditorialTextField(
                        value = lText,
                        onValueChange = { lText = it.take(4).uppercase() },
                        labelText = "লোগো লেটার (Logo Initials - Max 4)",
                        hintText = "CS",
                        modifier = Modifier.fillMaxWidth()
                    )

                    val context = LocalContext.current
                    var logoFileExists by remember { mutableStateOf(context.filesDir.resolve("custom_logo.png").exists()) }
                    val logoPickerLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent()
                    ) { uri ->
                        if (uri != null) {
                            try {
                                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                                    context.filesDir.resolve("custom_logo.png").outputStream().use { outputStream ->
                                        inputStream.copyTo(outputStream)
                                    }
                                }
                                logoFileExists = true
                                Toast.makeText(context, "লোগো সফলভাবে আপলোড হয়েছে!", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(context, "লোগো আপলোড ব্যর্থ হয়েছে!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { logoPickerLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("লোগো আপলোড করুন 📁", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        if (logoFileExists) {
                            OutlinedButton(
                                onClick = {
                                    try {
                                        context.filesDir.resolve("custom_logo.png").delete()
                                        logoFileExists = false
                                        Toast.makeText(context, "লোগো ডিলিট করা হয়েছে!", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFFEF4444)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                            ) {
                                Text("লোগো ডিলিট করুন 🗑️", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Text(
                        text = "লোগো আইকন নির্বাচন করুন",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 2.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(
                            Triple(1, Icons.Default.ReceiptLong, "রিসিট"),
                            Triple(2, Icons.Default.School, "একাডেমি"),
                            Triple(3, Icons.Default.Star, "ষ্টার"),
                            Triple(4, Icons.Default.AutoAwesome, "মডার্ন")
                        ).forEach { (id, icon, label) ->
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (lSeed == id) Color(0xFFEEF2FF)
                                        else Color(0xFFF8FAFC)
                                    )
                                    .border(
                                        1.dp,
                                        if (lSeed == id) Color(0xFF6366F1)
                                        else Color(0xFFE2E8F0),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { lSeed = id }
                                    .padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (lSeed == id) Color(0xFF4F46E5) else Color(0xFF64748B),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (lSeed == id) Color(0xFF4F46E5) else Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.shadow(2.dp, shape = RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "স্বাক্ষরকারী ও পদবী",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4F46E5),
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "AUTHORIZED SIGNATORY",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.SansSerif,
                            color = Color(0xFF1F2937)
                        )
                    }

                    EditorialTextField(
                        value = sigName,
                        onValueChange = { sigName = it },
                        labelText = "স্বাক্ষরকারীর নাম (Signatory Name)",
                        hintText = "যেমন: Tanvir Rahman",
                        modifier = Modifier.fillMaxWidth()
                    )

                    EditorialTextField(
                        value = sigDesig,
                        onValueChange = { sigDesig = it },
                        labelText = "পদবী (Designation)",
                        hintText = "যেমন: Head of Accounts",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ডিজিটাল সিগনেচার আঁকুন",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8),
                            letterSpacing = 1.sp
                        )
                        
                        TextButton(
                            onClick = { sigPoints = emptyList() },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("মুছে ফেলুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Touch Signature Board
                    SignaturePad(
                        points = sigPoints,
                        onPointsChanged = { sigPoints = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                            .background(Color(0xFFF8FAFC))
                    )
                }
            }
        }

        item {
            AnimatedVisibility(
                visible = successSaveIndicator,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF10B981)), // emerald 500
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "কোম্পানি প্রোফাইল এবং সিগনেচার সফলভাবে সেভ করা হয়েছে!",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Button(
                onClick = {
                    val serializedSig = SignatureSerializer.serialize(sigPoints)
                    viewModel.updateSettings(
                        companyName = cName.trim().ifBlank { "My Business" },
                        companyAddress = cAddress.trim().ifBlank { "Unknown Address" },
                        companyPhone = cPhone.trim().ifBlank { "Phone Number" },
                        currencySymbol = curSymbol.trim().ifBlank { "৳" },
                        logoText = lText.trim().ifBlank { "RG" },
                        logoSeed = lSeed,
                        signatureName = sigName.trim().ifBlank { "Authorized Signature" },
                        signatureDesignation = sigDesig.trim().ifBlank { "Officer" },
                        signaturePointsJson = serializedSig
                    )
                    successSaveIndicator = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF111827) // Slate-900 / Deep Slate Navy button
                )
            ) {
                Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "সেটিংস সংরক্ষণ করুন (Save Settings)",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.SansSerif
                )
            }

            LaunchedEffect(successSaveIndicator) {
                if (successSaveIndicator) {
                    kotlinx.coroutines.delay(3000)
                    successSaveIndicator = false
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SignaturePad(
    points: List<List<Offset>>,
    onPointsChanged: (List<List<Offset>>) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStroke by remember { mutableStateOf<List<Offset>>(emptyList()) }

    Box(
        modifier = modifier
            .background(Color.White)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        currentStroke = listOf(offset)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        currentStroke = currentStroke + change.position
                        // Create a mutable copy of existing points to redraw in real time
                        onPointsChanged(points.filter { it.isNotEmpty() } + listOf(currentStroke))
                    },
                    onDragEnd = {
                        if (currentStroke.isNotEmpty()) {
                            onPointsChanged(points + listOf(currentStroke))
                            currentStroke = emptyList()
                        }
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw completed strokes
            points.forEach { stroke ->
                for (i in 0 until stroke.size - 1) {
                    drawLine(
                        color = Color(30, 48, 128), // Authentic Dark Blue ink
                        start = stroke[i],
                        end = stroke[i + 1],
                        strokeWidth = 5f,
                        cap = StrokeCap.Round
                    )
                }
            }
            // Draw current active stroke
            for (i in 0 until currentStroke.size - 1) {
                drawLine(
                    color = Color(30, 48, 128),
                    start = currentStroke[i],
                    end = currentStroke[i + 1],
                    strokeWidth = 5f,
                    cap = StrokeCap.Round
                )
            }
        }

        if (points.isEmpty() && currentStroke.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "এখানে হাত দিয়ে স্বাক্ষর আঁকুন",
                    fontSize = 13.sp,
                    color = Color.LightGray,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// -------------------------------------------------------------
// DESIGN ENHANCEMENT: HIGH FIDELITY THERMAL JAGGED WRAPPER
// -------------------------------------------------------------
@Composable
fun ReceiptJaggedContainer(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val density = LocalDensity.current
    val toothWidth = 6.dp
    val toothHeight = 4.dp

    Box(
        modifier = modifier
            .shadow(6.dp, shape = object : Shape {
                override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
                    val path = Path()
                    val tWidthPx = with(density) { toothWidth.toPx() }
                    val tHeightPx = with(density) { toothHeight.toPx() }
                    val teethCount = (size.width / tWidthPx).toInt() + 1

                    // Top jagged line
                    path.moveTo(0f, tHeightPx)
                    for (i in 0..teethCount) {
                        val x = i * tWidthPx
                        val y = if (i % 2 == 0) tHeightPx else 0f
                        path.lineTo(x, y)
                    }
                    
                    // Down to bottom
                    path.lineTo(size.width, size.height - tHeightPx)
                    
                    // Bottom jagged line (from right to left)
                    for (i in teethCount downTo 0) {
                        val x = i * tWidthPx
                        val y = if (i % 2 == 0) size.height - tHeightPx else size.height
                        path.lineTo(x, y)
                    }
                    
                    path.close()
                    return Outline.Generic(path)
                }
            })
            .background(Color.White)
            .padding(top = toothHeight + 6.dp, bottom = toothHeight + 6.dp)
            .border(BorderStroke(1.dp, Color(0xFFE2E8F0)))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                content = content
            )
        }
    }
}

// Retro Barcode
@Composable
fun RetroBarcode(
    modifier: Modifier = Modifier,
    text: String
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(modifier = Modifier
            .fillMaxWidth()
            .height(35.dp)) {
            val width = size.width
            val height = size.height
            
            // Draw simple pseudo barcode lines
            val seed = text.hashCode().toLong()
            val random = java.util.Random(seed)
            
            var x = 15f
            val endX = width - 15f
            while (x < endX) {
                val barWidth = if (random.nextBoolean()) 2f else 5f
                val gap = if (random.nextBoolean()) 3f else 6f
                
                if (x + barWidth <= endX) {
                    drawRect(
                        color = Color(30, 30, 30),
                        topLeft = Offset(x, 0f),
                        size = Size(barWidth, height)
                    )
                }
                x += barWidth + gap
            }
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = text,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = Color(30, 30, 30),
            letterSpacing = 1.2.sp
        )
    }
}

// Dialog displaying physical paper receipt
@Composable
fun ReceiptQrCode(dataText: String, modifier: Modifier = Modifier) {
    val qrBitmap = remember(dataText) {
        try {
            val writer = com.google.zxing.qrcode.QRCodeWriter()
            val bitMatrix = writer.encode(dataText, com.google.zxing.BarcodeFormat.QR_CODE, 150, 150)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                }
            }
            bmp
        } catch (e: Exception) {
            null
        }
    }

    if (qrBitmap != null) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = modifier
        ) {
            Image(
                bitmap = qrBitmap.asImageBitmap(),
                contentDescription = "QR Code",
                modifier = Modifier
                    .size(68.dp)
                    .border(BorderStroke(1.dp, Color(220, 220, 220)))
                    .background(Color.White)
                    .padding(4.dp)
            )
            Text(
                text = "SECURE VERIFY",
                fontFamily = FontFamily.Monospace,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptPreviewDialog(
    receipt: ReceiptEntity,
    onDismiss: () -> Unit,
    isSuccessFlow: Boolean = false
) {
    val context = LocalContext.current
    val items = remember(receipt.itemsJson) { ReceiptItem.deserializeList(receipt.itemsJson) }
    
    // Header Logo Icon Mapper
    val chosenLogoIcon = when (receipt.logoSeed) {
        1 -> Icons.Default.ReceiptLong
        2 -> Icons.Default.School
        3 -> Icons.Default.Star
        4 -> Icons.Default.AutoAwesome
        else -> Icons.Default.ReceiptLong
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth()
            .padding(15.dp),
        content = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Title info based on flow
                    if (isSuccessFlow) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "রিসিট সফলভাবে জেনারেট হয়েছে!",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF429F45),
                                fontSize = 16.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    } else {
                        Text(
                            text = "রিসিট ভিউয়ার (Receipt)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Styled physical receipt paper
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    ) {
                        val logoFile = remember { context.filesDir.resolve("custom_logo.png") }
                        val customLogoBitmap = remember(logoFile) {
                            if (logoFile.exists()) {
                                try {
                                    android.graphics.BitmapFactory.decodeFile(logoFile.absolutePath)?.asImageBitmap()
                                } catch (e: Exception) {
                                    null
                                }
                            } else {
                                null
                            }
                        }

                        ReceiptJaggedContainer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                        ) {
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Logo display
                        if (customLogoBitmap != null) {
                            Image(
                                bitmap = customLogoBitmap,
                                contentDescription = "Logo",
                                modifier = Modifier
                                    .height(55.dp)
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp),
                                contentScale = androidx.compose.ui.layout.ContentScale.Fit
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        } else {
                            Text(
                                text = receipt.logoText,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color(30, 30, 30),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        // Company info name ONLY
                        Text(
                            text = receipt.companyName.uppercase(),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = Color(30, 30, 30),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "================================",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Invoice meta
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "NO: ${receipt.receiptNo}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(30, 30, 30)
                            )
                            
                            val formattedDate = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(Date(receipt.purchasedDate))
                            Text(
                                text = "DATE: ${formattedDate.uppercase()}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(30, 30, 30)
                            )
                        }

                        Text(
                            text = "--------------------------------",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Student Name and Product Info formatted beautifully
                        Text(
                            text = "STUDENT NAME: ${receipt.studentName.uppercase()}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(20, 20, 20),
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                        
                        Text(
                            text = "PRODUCT/COURSE: ${receipt.productName.uppercase()}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(20, 20, 20),
                            modifier = Modifier.padding(vertical = 2.dp)
                        )

                        Text(
                            text = "--------------------------------",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Table Headers
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "ITEMS / DESCRIPTION",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(30, 30, 30)
                            )
                            Text(
                                text = "PRICE",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(30, 30, 30)
                            )
                        }

                        Text(
                            text = "--------------------------------",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Item Rows with dotted vertical-like spacing
                        items.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = item.name.uppercase(),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    color = Color(30, 30, 30),
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${receipt.currencySymbol}${String.format("%.2f", item.price)}",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(30, 30, 30)
                                )
                            }
                        }

                        Text(
                            text = "--------------------------------",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Summary section: Subtotal, Discount, Grand Total
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "SUBTOTAL:",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = Color(40, 40, 40)
                            )
                            Text(
                                text = "${receipt.currencySymbol}${String.format("%.2f", receipt.subtotal)}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = Color(30, 30, 30)
                            )
                        }

                        // Handle discount formatting elegantly
                        if (receipt.discountValue > 0.0) {
                            val discountText = if (receipt.discountType == "PERCENTAGE") {
                                "DISCOUNT (${receipt.discountValue.toInt()}%):"
                            } else {
                                "DISCOUNT:"
                            }
                            
                            val subtotal = receipt.subtotal
                            val discountAmt = if (receipt.discountType == "PERCENTAGE") {
                                subtotal * (receipt.discountValue / 100.0)
                            } else {
                                receipt.discountValue
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = discountText,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    color = Color(40, 40, 40)
                                )
                                Text(
                                    text = "-${receipt.currencySymbol}${String.format("%.2f", discountAmt)}",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    color = Color(30, 30, 30)
                                )
                            }
                        }

                        Text(
                            text = "--------------------------------",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "GRAND TOTAL:",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(20, 20, 20)
                            )
                            Text(
                                text = "${receipt.currencySymbol}${String.format("%.2f", receipt.totalAmount)}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(10, 10, 10)
                            )
                        }

                        Text(
                            text = "================================",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Center-aligned Paid Stamp in the vertical flow to prevent any overlap
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            PaidStamp()
                        }

                        if (receipt.remarks.isNotBlank()) {
                            Text(
                                text = "NOTE: ${receipt.remarks.uppercase()}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = Color(50, 50, 50),
                                modifier = Modifier.padding(bottom = 6.dp),
                                lineHeight = 14.sp
                            )
                            Text(
                                text = "--------------------------------",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = Color.DarkGray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Signature and Signatory Info!
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            // Verification QR Code
                            val qrText = """
                                === VERIFIED RECEIPT ===
                                No: ${receipt.receiptNo}
                                Paid: ${receipt.currencySymbol}${String.format("%.2f", receipt.totalAmount)}
                                Student: ${receipt.studentName}
                                Course: ${receipt.productName}
                                Category: ${receipt.category}
                                Status: VERIFIED SAFE
                            """.trimIndent()
                            ReceiptQrCode(dataText = qrText, modifier = Modifier.padding(start = 4.dp))

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(160.dp)
                            ) {
                                // Draw actual rendered vector hand-drawn signature strokes!
                                val strokes = remember(receipt.signaturePointsJson) {
                                    SignatureSerializer.deserialize(receipt.signaturePointsJson)
                                }

                                if (strokes.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .width(130.dp)
                                            .height(55.dp)
                                            .background(Color.Transparent)
                                    ) {
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            // Real-time auto scaling of drawn signature coordinates so it fits perfectly!
                                            // Find bounds of signature coordinates
                                            val allPoints = strokes.flatten()
                                            if (allPoints.isNotEmpty()) {
                                                val minX = allPoints.minOf { it.x }
                                                val maxX = allPoints.maxOf { it.x }
                                                val minY = allPoints.minOf { it.y }
                                                val maxY = allPoints.maxOf { it.y }
                                                
                                                val widthPoints = (maxX - minX).coerceAtLeast(1f)
                                                val heightPoints = (maxY - minY).coerceAtLeast(1f)

                                                // Scale coordinates to fit our 130dp x 55dp canvas while preserving aspect ratio!
                                                val scaleX = size.width / widthPoints
                                                val scaleY = size.height / heightPoints
                                                val scale = minOf(scaleX, scaleY) * 0.85f

                                                val offsetX = (size.width - widthPoints * scale) / 2f - minX * scale
                                                val offsetY = (size.height - heightPoints * scale) / 2f - minY * scale

                                                strokes.forEach { stroke ->
                                                    for (i in 0 until stroke.size - 1) {
                                                        val p1 = Offset(stroke[i].x * scale + offsetX, stroke[i].y * scale + offsetY)
                                                        val p2 = Offset(stroke[i+1].x * scale + offsetX, stroke[i+1].y * scale + offsetY)
                                                        
                                                        drawLine(
                                                            color = Color(30, 48, 128),
                                                            start = p1,
                                                            end = p2,
                                                            strokeWidth = 3.5f,
                                                            cap = StrokeCap.Round
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .width(135.dp)
                                            .height(35.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "[ NO SIGNATURE ]",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 9.sp,
                                            color = Color.LightGray
                                        )
                                    }
                                }

                                Text(
                                    text = receipt.signatureName.uppercase(),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(30, 30, 30),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                                Text(
                                    text = receipt.signatureDesignation.uppercase(),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    color = Color(60, 60, 60),
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    lineHeight = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // High fidelity retro barcodes!
                        RetroBarcode(text = receipt.receiptNo)

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "================================",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = "THANK YOU FOR YOUR PAYMENT",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(30, 30, 30),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                // Exporter and Sharing Actions
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Download PDF + Image row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    ReceiptExporter.exportReceiptAsPdf(context, receipt)
                                },
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(1.dp, Color(0xFF4F46E5)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF4F46E5)),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "PDF ডাউনলোড",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    ReceiptExporter.exportReceiptAsImage(context, receipt)
                                },
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(1.dp, Color(0xFF0EA5E9)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0EA5E9)),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "IMAGE ডাউনলোড",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        // Closing & Text Share Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1.5f),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = "বন্ধ করুন",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = {
                                    // Compose perfectly formatted alignment sharing text
                                    val subtotal = receipt.subtotal
                                    val discountAmt = if (receipt.discountType == "PERCENTAGE") {
                                        subtotal * (receipt.discountValue / 100.0)
                                    } else {
                                        receipt.discountValue
                                    }

                                    val itemsShareStr = items.joinToString("\n") { 
                                        val rowName = { if (it.name.length > 20) it.name.take(17) + "..." else it.name }.invoke().padEnd(20)
                                        val rowPrice = "${receipt.currencySymbol}${String.format("%.2f", it.price)}".padStart(10)
                                        rowName + rowPrice
                                    }

                                    val shareBody = """
================================
${receipt.companyName.uppercase()}
${receipt.companyAddress.uppercase()}
CONTACT: ${receipt.companyPhone}
================================
RECEIPT NO: ${receipt.receiptNo}
DATE: ${SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(Date(receipt.purchasedDate)).uppercase()}
--------------------------------
STUDENT: ${receipt.studentName.uppercase()}
COURSE/PRODUCT: ${receipt.productName.uppercase()}
CATEGORY: ${receipt.category.uppercase()}
--------------------------------
${itemsShareStr}
--------------------------------
SUBTOTAL:           ${receipt.currencySymbol}${String.format("%.2f", receipt.subtotal).padStart(10)}
DISCOUNT:          -${receipt.currencySymbol}${String.format("%.2f", discountAmt).padStart(10)}
--------------------------------
GRAND TOTAL:        ${receipt.currencySymbol}${String.format("%.2f", receipt.totalAmount).padStart(10)}
================================
NOTE: ${receipt.remarks.uppercase().ifBlank { "PAID IN FULL" }}
--------------------------------
PREPARED BY: ${receipt.signatureName.uppercase()}
DESIGNATION: ${receipt.signatureDesignation.uppercase()}
================================
*** THANK YOU FOR YOUR PAYMENT ***
                                    """.trimIndent()

                                    val sendIntent: Intent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, shareBody)
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, "রিসিট শেয়ার করুন")
                                    context.startActivity(shareIntent)
                                },
                                modifier = Modifier.weight(2f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "রিসিট শেয়ার",
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagerDialog(
    viewModel: ReceiptViewModel,
    onDismiss: () -> Unit
) {
    val categoriesList by viewModel.allCategories.collectAsStateWithLifecycle()
    var newCategoryName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "বন্ধ করুন", fontWeight = FontWeight.Bold, color = Color(0xFF4F46E5))
            }
        },
        title = {
            Text(
                text = "ক্যাটাগরি সমাধান (CATEGORY MANAGER)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = Color(15, 23, 42)
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "নতুন ক্যাটাগরি তৈরি করুন এবং ব্যবহৃত রিসিটের জন্য এগুলো সেট করুন।",
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        placeholder = { Text("যেমন: Tuition", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(30, 41, 59),
                            unfocusedTextColor = Color(30, 41, 59),
                            focusedBorderColor = Color(0xFF4F46E5),
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        )
                    )

                    Button(
                        onClick = {
                            if (newCategoryName.trim().isNotBlank()) {
                                viewModel.addCategory(newCategoryName.trim())
                                newCategoryName = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("যোগ", fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider(color = Color(241, 245, 249))

                Text(
                    text = "বিদ্যমান ক্যাটাগরি তালিকা:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(148, 163, 184)
                )

                if (categoriesList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "কোনো ক্যাটাগরি নেই। অনুগ্রহ করে একটি তৈরি করুন।",
                            fontSize = 12.sp,
                            color = Color(148, 163, 184),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                    ) {
                        items(categoriesList) { cat ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = cat.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(30, 41, 59)
                                )
                                IconButton(
                                    onClick = { viewModel.removeCategory(cat.name) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}

@Composable
fun PaidStamp(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .graphicsLayer(rotationZ = -4f)
            .border(
                width = 3.dp,
                color = Color(0xFFEF4444).copy(alpha = 0.85f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = "PAID",
            color = Color(0xFFEF4444).copy(alpha = 0.85f),
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}
