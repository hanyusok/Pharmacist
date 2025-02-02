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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pharmacist.ui.DrugViewModel
import com.example.pharmacist.ui.screens.DrugEditScreen


sealed class Screen(val route: String) {
    object DrugList : Screen("drug_list")
    object DrugDetail : Screen("drug_detail/{drugId}") {
        fun createRoute(drugId: String) = "drug_detail/$drugId"
    }
    object DrugEdit : Screen("drug_edit/{drugId}") {
        fun createRoute(drugId: String) = "drug_edit/$drugId"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.DrugList.route
    ) {
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
                }
            )
        }
        
        composable(
            route = Screen.DrugDetail.route,
            arguments = listOf(
                navArgument("drugId") { type = NavType.StringType }
            )
        ) {
            DrugDetailScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToEdit = { drugId ->
                    navController.navigate(Screen.DrugEdit.createRoute(drugId))
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
            DrugEditScreen(
                drugId = drugId,
                onNavigateBack = { navController.navigateUp() }
            )
        }
    }
} 