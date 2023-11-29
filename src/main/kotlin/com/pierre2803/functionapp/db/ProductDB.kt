package com.pierre2803.functionapp.db

import com.fasterxml.jackson.annotation.JsonValue
import java.util.*

data class ProductDB(
    val id: UUID,
    val name: String,
    val productType: ProductType,
    val enabled: Boolean,
    val creationTime: Long,
    val lastUpdateTime: Long,
    val deletionTime: Long?)


enum class ProductType(@JsonValue val value: String) {
    ONLINE(value = "Online"),
    RETAIL(value = "Retail");

    companion object {
        fun forValue(bucketSource: String): ProductType {
            val value = bucketSource.uppercase()
            return values().firstOrNull { it.value.uppercase() == value } ?: throw IllegalArgumentException("Unsupported bucket source '$bucketSource'.")
        }
    }
}