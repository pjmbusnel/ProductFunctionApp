package com.pierre2803.functionapp.product

import com.pierre2803.functionapp.ApplicationError.Companion.INVALID_UUID
import com.pierre2803.functionapp.Operation.READ
import com.pierre2803.functionapp.ValidationException
import com.pierre2803.functionapp.Validations.validUUID
import com.pierre2803.functionapp.Validations.required
import com.pierre2803.functionapp.ApplicationError.ProductApplicationError.Companion.resource as PRODUCT
import java.util.*

data class ProductRetrievalRequest(val productId: String) {

    fun validateAndTransform(): UUID {
        required(productId, READ, INVALID_UUID.forProduct())?.let { throw ValidationException(it) }
        validUUID(productId, PRODUCT, READ, INVALID_UUID)?.let { throw ValidationException(it) }

        return UUID.fromString(productId)
    }
}