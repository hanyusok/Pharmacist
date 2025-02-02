package com.example.pharmacist.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pharmacist.domain.model.Drug
import com.example.pharmacist.ui.DrugEditViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrugEditScreen(
    drugId: String?,
    viewModel: DrugEditViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val drug by viewModel.drug.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(drugId) {
        if (drugId != null) {
            viewModel.loadDrug(drugId)
        }
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
                    IconButton(onClick = { viewModel.saveDrug(onSuccess = onNavigateBack) }) {
                        Icon(Icons.Default.Save, "Save")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                CircularProgressIndicator()
            }
        } else {
            DrugEditForm(
                drug = drug,
                onDrugChange = viewModel::updateDrug,
                modifier = Modifier.padding(padding)
            )
        }

        error?.let { errorMessage ->
            AlertDialog(
                onDismissRequest = { viewModel.clearError() },
                title = { Text("Error") },
                text = { Text(errorMessage) },
                confirmButton = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("OK")
                    }
                }
            )
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