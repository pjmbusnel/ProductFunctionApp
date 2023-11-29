package com.pierre2803.functionapp.db

import java.util.*

data class ProductDBUpdate(
    val id: UUID,
    val name: String,
    val productType: String)