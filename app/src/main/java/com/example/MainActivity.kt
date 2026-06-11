package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.data.ReceiptDatabase
import com.example.data.ReceiptRepository
import com.example.ui.ReceiptViewModel
import com.example.ui.ReceiptViewModelFactory
import com.example.ui.screens.MainAppScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Setup offline data layers
        val database = ReceiptDatabase.getDatabase(applicationContext)
        val repository = ReceiptRepository(database.receiptDao)
        val viewModelFactory = ReceiptViewModelFactory(repository)
        
        // Use standard ViewModels delegate
        val viewModel: ReceiptViewModel by viewModels { viewModelFactory }

        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainAppScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
