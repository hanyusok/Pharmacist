package com.example.pharmacist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pharmacist.ui.theme.PharmacistTheme
import com.example.pharmacist.ui.DrugViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.Modifier
import com.example.pharmacist.ui.screens.DrugListScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PharmacistTheme {
                val viewModel: DrugViewModel = hiltViewModel()
                val drugs by viewModel.drugs.collectAsState()
                val isLoading by viewModel.isLoading.collectAsState()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DrugListScreen(
                        drugs = drugs,
                        isLoading = isLoading,
                        onSearch = viewModel::searchDrugs
                    )
                }
            }
        }
    }
}