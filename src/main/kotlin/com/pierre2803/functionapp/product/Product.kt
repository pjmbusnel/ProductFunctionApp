package com.pierre2803.functionapp.product

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

data class Product(
        @JsonProperty("id") val id: UUID,
        @JsonProperty("name") val name: String,
        @JsonProperty("enabled") val enabled: Boolean = true,
        @JsonProperty("deletion_time") val deletionTime: Long? = null)
