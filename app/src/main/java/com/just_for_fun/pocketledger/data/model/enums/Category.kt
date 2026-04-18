package com.just_for_fun.pocketledger.data.model.enums

enum class Category {
    FOOD, TRANSPORT, SHOPPING, HEALTH, ENTERTAINMENT, SALARY, BILLS, EDUCATION, OTHER
}

fun Category.displayName(): String = when (this) {
    Category.FOOD -> "Food"
    Category.TRANSPORT -> "Transport"
    Category.SHOPPING -> "Shopping"
    Category.HEALTH -> "Health"
    Category.ENTERTAINMENT -> "Entertainment"
    Category.SALARY -> "Salary"
    Category.BILLS -> "Bills"
    Category.EDUCATION -> "Education"
    Category.OTHER -> "Other"
}
