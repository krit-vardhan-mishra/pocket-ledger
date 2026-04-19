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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.just_for_fun.pocketledger.data.model.enums.Category

fun iconForCategory(category: Category): ImageVector {
    return when (category) {
        Category.FOOD          -> Icons.Default.Restaurant
        Category.TRANSPORT     -> Icons.Default.DirectionsCar
        Category.SHOPPING      -> Icons.Default.ShoppingCart
        Category.HEALTH        -> Icons.Default.MedicalServices
        Category.ENTERTAINMENT -> Icons.Default.Movie
        Category.SALARY        -> Icons.Default.Payments
        Category.BILLS         -> Icons.Default.Receipt
        Category.EDUCATION     -> Icons.Default.School
        Category.OTHER         -> Icons.Default.Category
    }
}

/**
 * Returns a distinctive, semantically appropriate background color for each
 * category, used in icon containers and chart legends.
 */
fun colorForCategory(category: Category): Color {
    return when (category) {
        Category.FOOD          -> Color(0xFFE65100) // deep orange
        Category.TRANSPORT     -> Color(0xFF1565C0) // blue
        Category.SHOPPING      -> Color(0xFF6A1B9A) // purple
        Category.HEALTH        -> Color(0xFFC62828) // red
        Category.ENTERTAINMENT -> Color(0xFF00838F) // teal
        Category.SALARY        -> Color(0xFF2E7D32) // green
        Category.BILLS         -> Color(0xFF4527A0) // deep purple
        Category.EDUCATION     -> Color(0xFF00695C) // dark teal
        Category.OTHER         -> Color(0xFF546E7A) // blue-grey
    }
}