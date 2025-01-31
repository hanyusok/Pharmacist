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


sealed class Screen(val route: String) {
    object DrugList : Screen("drug_list")
    object DrugDetail : Screen("drug_detail/{drugId}") {
        fun createRoute(drugId: Long) = "drug_detail/$drugId"
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
                navArgument("drugId") { type = NavType.LongType }
            )
        ) {
            DrugDetailScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }
    }
} 