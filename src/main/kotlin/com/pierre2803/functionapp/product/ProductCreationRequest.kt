package com.pierre2803.functionapp.product

import com.fasterxml.jackson.annotation.JsonProperty
import com.pierre2803.functionapp.ApplicationError.Product.INVALID_PRODUCT_TYPE
import com.pierre2803.functionapp.ApplicationError.Product.NAME_DEFINES_INVALID_CHAR
import com.pierre2803.functionapp.ApplicationError.Product.NAME_TOO_LONG
import com.pierre2803.functionapp.ApplicationError.Product.NO_NAME
import com.pierre2803.functionapp.Operation.CREATE
import com.pierre2803.functionapp.ValidationException
import com.pierre2803.functionapp.Validations.alphaAccentNumDashUnderscoreSpace
import com.pierre2803.functionapp.Validations.maxLength
import com.pierre2803.functionapp.Validations.required
import com.pierre2803.functionapp.Validations.supportedValue
import com.pierre2803.functionapp.db.ProductDBCreation
import com.pierre2803.functionapp.db.ProductType
import java.util.*

internal const val maxNameLength = 100

data class ProductCreationRequest(
    @JsonProperty("name") val name: String? = null,
    @JsonProperty("product_type") val productType: String? = null) {

    fun validateAndTransform(): ProductDBCreation {
        val sanitizedRequest = ProductCreationRequest(
            name = name?.trim(),
            productType = productType?.trim()?.uppercase())

        val errors = mutableListOf<com.pierre2803.functionapp.ApplicationError>()
        // Name
        required(sanitizedRequest.name, CREATE, NO_NAME)?.let { errors.add(it) }
        maxLength(sanitizedRequest.name, maxNameLength, CREATE, NAME_TOO_LONG)?.let { errors.add(it) }
        alphaAccentNumDashUnderscoreSpace(sanitizedRequest.name, CREATE, NAME_DEFINES_INVALID_CHAR)?.let { errors.add(it) }
        // productType
        supportedValue(sanitizedRequest.productType, ProductType.values().map { it.name }, CREATE, INVALID_PRODUCT_TYPE)
            ?.let { errors.add(it) }

        if (errors.isNotEmpty()) throw ValidationException(*errors.toTypedArray())

        return ProductDBCreation(
                id = UUID.randomUUID(),
                name = sanitizedRequest.name!!,
                productType = if (sanitizedRequest.productType.isNullOrBlank())
                    ProductType.ONLINE
                else
                    ProductType.valueOf(sanitizedRequest.productType),
                enabled = true)
    }
}
