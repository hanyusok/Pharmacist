package com.example.pharmacist.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.material3.Scaffold
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pharmacist.ui.components.BottomNavBar
import com.example.pharmacist.ui.DrugViewModel
import com.example.pharmacist.ui.DrugDetailViewModel
import com.example.pharmacist.ui.screens.*

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Login.route
) {
    Scaffold(
        bottomBar = {
            if (navController.currentBackStackEntryAsState().value?.destination?.route in listOf(
                    Screen.DrugList.route,
                    Screen.Orders.route
                )) {
                BottomNavBar(navController)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.DrugList.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToSignUp = {
                        navController.navigate(Screen.SignUp.route)
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
                    },
                    onSignOutSuccess = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.DrugList.route) { inclusive = true }
                        }
                    },
                    onNavigateToProfile = {
                        navController.navigate(Screen.Profile.route)
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

            composable(Screen.SignUp.route) {
                SignUpScreen(
                    onSignUpSuccess = {
                        navController.navigate(Screen.DrugList.route) {
                            popUpTo(Screen.SignUp.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.SignUp.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Profile.route) {
                UserProfileScreen(
                    onNavigateBack = { navController.navigateUp() }
                )
            }

            composable(Screen.Orders.route) {
                OrdersScreen(
                    onCreateOrder = { navController.navigate(Screen.CreateOrder.route) },
                    onOrderClick = { orderId -> 
                        navController.navigate(Screen.OrderDetail.createRoute(orderId))
                    }
                )
            }

            composable(Screen.CreateOrder.route) {
                CreateOrderScreen(
                    onNavigateBack = { navController.navigateUp() },
                    onOrderCreated = { 
                        navController.navigateUp()
                    }
                )
            }

            composable(
                route = Screen.OrderDetail.route,
                arguments = listOf(
                    navArgument("orderId") { type = NavType.StringType }
                )
            ) {
                OrderDetailScreen(
                    onNavigateBack = { navController.navigateUp() }
                )
            }
        }
    }
}