package com.example.pharmacist.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pharmacist.domain.model.Drug
import com.example.pharmacist.ui.DrugDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrugDetailScreen(
    viewModel: DrugDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val drug by remember { viewModel.drug }.collectAsState(initial = null)
    val isLoading by remember { viewModel.isLoading }.collectAsState(initial = false)
    val error by remember { viewModel.error }.collectAsState(initial = null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Drug Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                error != null -> {
                    Text(
                        text = "Error: $error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                drug != null -> {
                    DrugDetailContent(drug = drug!!)
                }
            }
        }
    }
}

@Composable
private fun DrugDetailContent(drug: Drug) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Drug Name Section
        Text(
            text = drug.drugName,
            style = MaterialTheme.typography.headlineMedium,
            color = colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Main Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                InfoRow(
                    label = "Ingredient",
                    value = drug.ingredient,
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = 1.dp,
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                InfoRow(
                    label = "Drug Code",
                    value = drug.drugCode
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = 1.dp,
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                InfoRow(
                    label = "Manufacturer",
                    value = drug.manufacturer
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = 1.dp,
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Insurance Coverage",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (drug.isCoveredByInsurance) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Covered",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.primary
                            )
                        } else {
                            Text(
                                text = "Not Covered",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = style,
            color = colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = style,
            color = colorScheme.onSurfaceVariant
        )
    }
} 