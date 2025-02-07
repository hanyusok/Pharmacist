package com.example.pharmacist.navigation

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
    object SignUp : Screen("signup")
    object Profile : Screen("profile")
    object Orders : Screen("orders")
    object OrderDetail : Screen("order_detail/{orderId}") {
        fun createRoute(orderId: String) = "order_detail/$orderId"
    }
    object CreateOrder : Screen("create_order")
} 