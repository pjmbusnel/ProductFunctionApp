package com.pierre2803.functionapp.product

import com.pierre2803.functionapp.ApplicationError.Product.INVALID_PRODUCT_SORT
import com.pierre2803.functionapp.Operation
import com.pierre2803.functionapp.ValidationException
import com.pierre2803.functionapp.Validations.supportedValue

private val supportedSort = ProductsRetrievalSort.values().map { it.value }

data class ProductsRetrievalRequest(val sort: String) {

    fun validateAndTransform(): ProductsRetrieval {
        val sanitizedRequest = ProductsRetrievalRequest(
            sort = sort.trim())

        supportedValue(sanitizedRequest.sort, supportedSort, Operation.READ, INVALID_PRODUCT_SORT)?.let { throw ValidationException(it) }

        return ProductsRetrieval(
            sort = ProductsRetrievalSort.forValueOrDefault(sanitizedRequest.sort)
        )
    }
}

data class ProductsRetrieval(val sort: ProductsRetrievalSort)

enum class ProductsRetrievalSort(val value: String) {
    NAME("name");

    companion object {
        fun forValueOrDefault(v: String?) = values().firstOrNull { it.value == v } ?: NAME
    }
}