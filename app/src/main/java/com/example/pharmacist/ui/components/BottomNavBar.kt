package com.example.pharmacist.ui.components

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.pharmacist.navigation.Screen

@Composable
fun BottomNavBar(navController: NavHostController) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == Screen.DrugList.route,
            onClick = { 
                if (currentRoute != Screen.DrugList.route) {
                    navController.navigate(Screen.DrugList.route) {
                        // Pop up to the root to avoid building up a large stack
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            },
            icon = { Icon(Icons.Default.List, contentDescription = "Drugs") },
            label = { Text("Drugs") }
        )
        
        NavigationBarItem(
            selected = currentRoute == Screen.Orders.route,
            onClick = {
                if (currentRoute != Screen.Orders.route) {
                    navController.navigate(Screen.Orders.route) {
                        // Pop up to the root to avoid building up a large stack
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Orders") },
            label = { Text("Orders") }
        )
    }
} 