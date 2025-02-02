package com.example.pharmacist.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pharmacist.domain.model.Drug
import com.example.pharmacist.ui.DrugEditViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrugEditScreen(
    drugId: String?,
    viewModel: DrugEditViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onUpdateComplete: () -> Unit = {}
) {
    val drug by viewModel.drug.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var showConfirmDialog by remember { mutableStateOf(false) }
    
    // Validation function
    fun validateDrug(drug: Drug?): String? {
        if (drug == null) return "Drug data is missing"
        if (drug.drugName.isBlank()) return "Drug name is required"
        if (drug.mainCode.isBlank()) return "Main code is required"
        if (drug.ingredient.isBlank()) return "Ingredient is required"
        if (drug.drugCode.isBlank()) return "Drug code is required"
        if (drug.manufacturer.isBlank()) return "Manufacturer is required"
        return null
    }

    LaunchedEffect(drugId) {
        if (drugId != null) {
            viewModel.loadDrug(drugId)
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            title = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (drugId != null) "Update Drug" else "Create Drug",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    HorizontalDivider(
                        modifier = Modifier
                            .padding(top = 8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Please confirm the following details:",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    DetailItem(
                        label = "Drug Name",
                        value = drug?.drugName ?: "",
                        icon = Icons.Default.Medication
                    )
                    DetailItem(
                        label = "Main Code",
                        value = drug?.mainCode ?: "",
                        icon = Icons.Default.QrCode
                    )
                    DetailItem(
                        label = "Ingredient",
                        value = drug?.ingredient ?: "",
                        icon = Icons.Default.Science
                    )
                    DetailItem(
                        label = "Drug Code",
                        value = drug?.drugCode ?: "",
                        icon = Icons.Default.Numbers
                    )
                    DetailItem(
                        label = "Manufacturer",
                        value = drug?.manufacturer ?: "",
                        icon = Icons.Default.Factory
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Covered by Insurance",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = drug?.isCoveredByInsurance ?: false,
                            onCheckedChange = null,
                            enabled = false
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        viewModel.saveDrug(
                            onSuccess = onNavigateBack,
                            onUpdateComplete = onUpdateComplete
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showConfirmDialog = false },
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (drugId != null) "Edit Drug" else "Add Drug") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBackIosNew, "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { 
                            val validationError = validateDrug(drug)
                            if (validationError != null) {
                                viewModel.setError(validationError)
                            } else {
                                showConfirmDialog = true
                            }
                        }
                    ) {
                        Icon(Icons.Default.Save, "Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Show error if exists
                error?.let { errorMessage ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            IconButton(onClick = { viewModel.clearError() }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Clear error",
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
                
                DrugEditForm(
                    drug = drug,
                    onDrugChange = viewModel::updateDrug,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DrugEditForm(
    drug: Drug?,
    onDrugChange: (Drug) -> Unit,
    modifier: Modifier = Modifier
) {
    var mainCode by remember(drug) { mutableStateOf(drug?.mainCode ?: "") }
    var drugName by remember(drug) { mutableStateOf(drug?.drugName ?: "") }
    var ingredient by remember(drug) { mutableStateOf(drug?.ingredient ?: "") }
    var drugCode by remember(drug) { mutableStateOf(drug?.drugCode ?: "") }
    var manufacturer by remember(drug) { mutableStateOf(drug?.manufacturer ?: "") }
    var isCoveredByInsurance by remember(drug) { mutableStateOf(drug?.isCoveredByInsurance ?: false) }

    LaunchedEffect(
        mainCode, drugName, ingredient, drugCode, manufacturer, isCoveredByInsurance
    ) {
        if (drug != null) {
            onDrugChange(drug.copy(
                mainCode = mainCode,
                drugName = drugName,
                ingredient = ingredient,
                drugCode = drugCode,
                manufacturer = manufacturer,
                isCoveredByInsurance = isCoveredByInsurance
            ))
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = mainCode,
            onValueChange = { mainCode = it },
            label = { Text("Main Code") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = drugName,
            onValueChange = { drugName = it },
            label = { Text("Drug Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = ingredient,
            onValueChange = { ingredient = it },
            label = { Text("Ingredient") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = drugCode,
            onValueChange = { drugCode = it },
            label = { Text("Drug Code") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = manufacturer,
            onValueChange = { manufacturer = it },
            label = { Text("Manufacturer") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Covered by Insurance")
            Switch(
                checked = isCoveredByInsurance,
                onCheckedChange = { isCoveredByInsurance = it }
            )
        }
    }
}

@Composable
private fun DetailItem(
    label: String,
    value: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}