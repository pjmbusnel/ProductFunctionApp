package com.pierre2803.functionapp.db

import java.util.*

data class ProductDBCreation(
    val id: UUID,
    val name: String,
    val productType: ProductType,
    val enabled: Boolean = true)