package com.pierre2803.functionapp.product

import com.fasterxml.jackson.annotation.JsonProperty
import com.pierre2803.functionapp.ApplicationError
import com.pierre2803.functionapp.ApplicationError.Companion.INVALID_UUID
import com.pierre2803.functionapp.ApplicationError.Product.NAME_DEFINES_INVALID_CHAR
import com.pierre2803.functionapp.ApplicationError.Product.NAME_TOO_LONG
import com.pierre2803.functionapp.ApplicationError.Product.NO_NAME
import com.pierre2803.functionapp.Operation
import com.pierre2803.functionapp.Operation.UPDATE
import com.pierre2803.functionapp.ValidationException
import com.pierre2803.functionapp.Validations
import com.pierre2803.functionapp.Validations.alphaAccentNumDashUnderscoreSpace
import com.pierre2803.functionapp.Validations.maxLength
import com.pierre2803.functionapp.Validations.required
import com.pierre2803.functionapp.Validations.validUUID
import com.pierre2803.functionapp.db.ProductType
import java.util.*
import com.pierre2803.functionapp.ApplicationError.ProductApplicationError.Companion.resource as PRODUCT

data class ProductUpdateRequest(
    @JsonProperty("name") val name: String? = null,
    @JsonProperty("product_type") val productType: String? = null) {

    fun validateAndTransform(productId: String): ProductUpdate {
        val sanitizedRequest = ProductUpdateRequest(
            name = name?.trim())

        val errors = mutableListOf<ApplicationError>()
        // ID
        required(productId, UPDATE, INVALID_UUID.forProduct())?.let { errors.add(it) }
        validUUID(productId, PRODUCT, UPDATE, INVALID_UUID)?.let { errors.add(it) }
        // Name
        required(sanitizedRequest.name, UPDATE, NO_NAME)?.let { errors.add(it) }
        maxLength(sanitizedRequest.name, maxNameLength, UPDATE, NAME_TOO_LONG)?.let { errors.add(it) }
        alphaAccentNumDashUnderscoreSpace(sanitizedRequest.name, UPDATE, NAME_DEFINES_INVALID_CHAR)?.let { errors.add(it) }

        // Product Type
        // TODO add validation for missing Product Type
        Validations.supportedValue(sanitizedRequest.productType,ProductType.values().map { it.name },Operation.CREATE, ApplicationError.Product.INVALID_PRODUCT_TYPE)
            ?.let { errors.add(it) }

        if (errors.isNotEmpty()) throw ValidationException(*errors.toTypedArray())

        return ProductUpdate(
                id = UUID.fromString(productId),
                name = sanitizedRequest.name!!,
            productType = sanitizedRequest.name!!)
    }
}

data class ProductUpdate(
    @JsonProperty("id") val id: UUID,
    @JsonProperty("name") val name: String,
    @JsonProperty("product_type") val productType: String)