package com.pierre2803.functionapp

import com.microsoft.azure.functions.HttpMethod

enum class Operation(val id: String) {
    CREATE("POST"),
    UPDATE("PUT"),
    READ("GET"),
    DELETE("DELETE");

    companion object {
        fun toOperation(httpMethod: HttpMethod): Operation {
            return when (httpMethod) {
                HttpMethod.GET -> READ
                HttpMethod.POST -> CREATE
                HttpMethod.PUT -> UPDATE
                HttpMethod.DELETE -> DELETE
                else -> throw IllegalArgumentException("No supported operation matches the HTTP method $httpMethod.")
            }
        }
    }
}
