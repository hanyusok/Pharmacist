package com.example.pharmacist.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.pharmacist.domain.model.Drug

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrugListScreen(
    drugs: List<Drug>,
    isLoading: Boolean,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        SearchBar(
            query = searchQuery,
            onQueryChange = { newQuery -> 
                searchQuery = newQuery
                if (newQuery.length >= 2) {
                    onSearch(newQuery)
                } else if (newQuery.isEmpty()) {
                    onSearch("")
                }
            },
            onSearch = { 
                onSearch(searchQuery)
                isSearchActive = false
            },
            active = isSearchActive,
            onActiveChange = { isSearchActive = it },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            placeholder = { Text("Search drugs...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {}

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (drugs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No drugs found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(drugs) { drug ->
                    DrugCard(drug = drug)
                }
            }
        }
    }
}

@Composable
fun DrugCard(
    drug: Drug,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
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
            Text(
                text = drug.drugName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = drug.ingredient,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Code: ${drug.drugCode}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (drug.isCoveredByInsurance) {
                    Text(
                        text = "Covered by Insurance",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DrugCardPreview() {
    val previewDrug = Drug(
        id = 1,
        mainCode = "123",
        ingredient = "Sample Ingredient",
        drugCode = "ABC123",
        drugName = "Sample Drug Name",
        manufacturer = "Sample Manufacturer",
        isCoveredByInsurance = true
    )
    DrugCard(drug = previewDrug)
}

@Preview(showBackground = true)
@Composable
fun DrugListScreenPreview() {
    MaterialTheme {
        DrugListScreen(
            drugs = listOf(
                Drug(
                    id = 1,
                    mainCode = "ABC123",
                    ingredient = "Paracetamol",
                    drugCode = "PARA001",
                    drugName = "Sample Drug",
                    manufacturer = "Sample Manufacturer",
                    isCoveredByInsurance = true
                )
            ),
            isLoading = false,
            onSearch = {}
        )
    }
} 