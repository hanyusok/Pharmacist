package com.example.pharmacist.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pharmacist.R
import com.example.pharmacist.domain.model.Drug
import com.example.pharmacist.ui.theme.PharmacistTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pharmacist.ui.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrugListScreen(
    drugs: List<Drug>,
    isLoading: Boolean,
    onSearch: (String) -> Unit,
    onDrugClick: (String) -> Unit,
    onAddNewDrug: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    
    // Keep track of recent searches
    var recentSearches by remember { mutableStateOf(listOf<String>()) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNewDrug,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add new drug",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        topBar = {
            TopAppBar(
                title = { Text("Drugs") },
                actions = {
                    IconButton(
                        onClick = {
                            authViewModel.signOut()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Sign Out"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize()) {
            DockedSearchBar(
                query = searchQuery,
                onQueryChange = { newQuery ->
                    searchQuery = newQuery
                    if (newQuery.length >= 2) {
                        onSearch(newQuery)
                    } else if (newQuery.isEmpty()) {
                        onSearch("")
                    }
                },
                onSearch = { query ->
                    onSearch(query)
                    isSearchActive = false
                    if (query.isNotEmpty() && !recentSearches.contains(query)) {
                        recentSearches = (listOf(query) + recentSearches).take(5)
                    }
                },
                active = isSearchActive,
                onActiveChange = { isSearchActive = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = {
                    Text(
                        text = "Search drugs...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (isSearchActive) {
                        // Show close/back button when search is active
                        IconButton(
                            onClick = { 
                                if (searchQuery.isNotEmpty()) {
                                    searchQuery = ""
                                } else {
                                    isSearchActive = false
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (searchQuery.isNotEmpty()) 
                                    Icons.Default.Clear 
                                else 
                                    Icons.Default.ArrowBack,
                                contentDescription = if (searchQuery.isNotEmpty()) 
                                    "Clear search" 
                                else 
                                    "Close search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            ) {
                // This content is shown when the search bar is active
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    // Show recent searches with clear all option
                    if (searchQuery.isEmpty()) {
                        if (recentSearches.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Recent Searches",
                                    style = MaterialTheme.typography.labelMedium
                                )
                                TextButton(
                                    onClick = { recentSearches = emptyList() }
                                ) {
                                    Text("Clear All")
                                }
                            }
                            
                            recentSearches.forEach { recent ->
                                SearchSuggestionItem(
                                    text = recent,
                                    onItemClick = {
                                        searchQuery = recent
                                        onSearch(recent)
                                        isSearchActive = false
                                    },
                                    onRemove = {
                                        recentSearches = recentSearches.filter { it != recent }
                                    }
                                )
                            }
                        }
                    }
                    
                    // Show search results/suggestions based on current query
                    if (searchQuery.isNotEmpty()) {
                        // Filter drugs that match the query
                        val suggestions = drugs.filter { 
                            it.drugName.contains(searchQuery, ignoreCase = true) ||
                            it.ingredient.contains(searchQuery, ignoreCase = true)
                        }.take(5)  // Limit to 5 suggestions
                        
                        suggestions.forEach { drug ->
                            SearchSuggestionItem(
                                text = drug.drugName,
                                secondaryText = drug.ingredient,
                                onItemClick = {
                                    drug.id?.let { onDrugClick(it) }
                                    isSearchActive = false
                                }
                            )
                        }
                    }
                }
            }

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
                        DrugCard(
                            drug = drug,
                            onClick = { drug.id?.let { onDrugClick(it) } }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DrugCard(
    drug: Drug,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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

@Composable
private fun SearchSuggestionItem(
    text: String,
    secondaryText: String? = null,
    onItemClick: () -> Unit,
    onRemove: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 12.dp)
            )
            Column {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge
                )
                if (secondaryText != null) {
                    Text(
                        text = secondaryText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // Show remove button only for recent searches
        if (onRemove != null) {
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove from recent searches",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DrugCardPreview() {
    PharmacistTheme {
        val previewDrug = Drug(
            id = "1",
            mainCode = "123",
            ingredient = "Sample Ingredient",
            drugCode = "ABC123",
            drugName = "Sample Drug Name",
            manufacturer = "Sample Manufacturer",
            isCoveredByInsurance = true
        )
        DrugCard(
            drug = previewDrug,
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DrugListScreenPreview() {
    PharmacistTheme {
        Surface {
            DrugListScreen(
                drugs = listOf(
                    Drug(
                        id = "1",
                        mainCode = "ABC123",
                        ingredient = "Paracetamol",
                        drugCode = "PARA001",
                        drugName = "Sample Drug",
                        manufacturer = "Sample Manufacturer",
                        isCoveredByInsurance = true
                    )
                ),
                isLoading = false,
                onSearch = {},
                onDrugClick = {},
                onAddNewDrug = {}
            )
        }
    }
} 