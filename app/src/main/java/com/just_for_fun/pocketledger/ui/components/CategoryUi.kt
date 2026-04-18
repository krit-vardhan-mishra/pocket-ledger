package com.just_for_fun.pocketledger.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector
import com.just_for_fun.pocketledger.data.model.enums.Category

fun iconForCategory(category: Category): ImageVector {
    return when (category) {
        Category.FOOD -> Icons.Default.Restaurant
        Category.TRANSPORT -> Icons.Default.DirectionsCar
        Category.SHOPPING -> Icons.Default.ShoppingCart
        Category.HEALTH -> Icons.Default.MedicalServices
        Category.ENTERTAINMENT -> Icons.Default.Movie
        Category.SALARY -> Icons.Default.Payments
        Category.BILLS -> Icons.Default.Receipt
        Category.EDUCATION -> Icons.Default.School
        Category.OTHER -> Icons.Default.Category
    }
}
