package com.example.pharmacist.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pharmacist.ui.screens.DrugDetailScreen
import com.example.pharmacist.ui.screens.DrugListScreen
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pharmacist.ui.DrugViewModel
import com.example.pharmacist.ui.screens.DrugEditScreen
import com.example.pharmacist.ui.DrugDetailViewModel
import android.util.Log
import androidx.compose.runtime.LaunchedEffect
import com.example.pharmacist.ui.screens.LoginScreen


sealed class Screen(val route: String) {
    object Login : Screen("login")
    object DrugList : Screen("drug_list")
    object DrugDetail : Screen("drug_detail/{drugId}") {
        fun createRoute(drugId: String) = "drug_detail/$drugId"
    }
    object DrugEdit : Screen("drug_edit/{drugId}") {
        fun createRoute(drugId: String?) = "drug_edit/$drugId"
    }
    object AddDrug : Screen("add_drug")
}

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.DrugList.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.DrugList.route) {
            val viewModel: DrugViewModel = hiltViewModel()
            val drugs by viewModel.drugs.collectAsState()
            val isLoading by viewModel.isLoading.collectAsState()
            
            DrugListScreen(
                drugs = drugs,
                isLoading = isLoading,
                onSearch = viewModel::searchDrugs,
                onDrugClick = { drugId ->
                    navController.navigate(Screen.DrugDetail.createRoute(drugId))
                },
                onAddNewDrug = {
                    navController.navigate(Screen.AddDrug.route)
                }
            )
        }
        
        composable(
            route = Screen.DrugDetail.route,
            arguments = listOf(
                navArgument("drugId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val drugId = checkNotNull(backStackEntry.arguments?.getString("drugId"))
            val drugListViewModel: DrugViewModel = hiltViewModel(
                remember { navController.getBackStackEntry(Screen.DrugList.route) }
            )
            DrugDetailScreen(
                viewModel = hiltViewModel(),
                onNavigateBack = {
                    drugListViewModel.loadDrugs(forceRefresh = true)
                    navController.navigateUp()
                },
                onNavigateToEdit = { id -> 
                    navController.navigate(Screen.DrugEdit.createRoute(id))
                }
            )
        }
        
        composable(
            route = Screen.DrugEdit.route,
            arguments = listOf(
                navArgument("drugId") { 
                    type = NavType.StringType 
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val drugId = backStackEntry.arguments?.getString("drugId")
            val drugListViewModel: DrugViewModel = hiltViewModel(
                remember { navController.getBackStackEntry(Screen.DrugList.route) }
            )
            val drugDetailViewModel: DrugDetailViewModel = hiltViewModel(
                remember { navController.getBackStackEntry(Screen.DrugDetail.route) }
            )
            
            DrugEditScreen(
                drugId = drugId,
                onNavigateBack = { navController.navigateUp() },
                onUpdateComplete = {
                    drugListViewModel.loadDrugs(forceRefresh = true)
                    drugDetailViewModel.refresh()
                }
            )
        }

        composable(Screen.AddDrug.route) {
            val drugListViewModel: DrugViewModel = hiltViewModel(
                remember { navController.getBackStackEntry(Screen.DrugList.route) }
            )
            DrugEditScreen(
                drugId = null,
                onNavigateBack = { navController.navigateUp() },
                onUpdateComplete = {
                    drugListViewModel.loadDrugs(forceRefresh = true)
                }
            )
        }
    }
} 