package com.pierre2803.functionapp

import com.microsoft.azure.functions.HttpStatus
import com.microsoft.azure.functions.HttpStatus.*

open class ApplicationError(val status: HttpStatus,
                            protected val code: Int,
                            protected val messageTemplate: String,
                            protected val operation: Operation = Operation.READ,
                            protected val resourceType: String = "GENERAL",
                            protected val arguments: Array<out Any> = emptyArray(),
                            protected val argumentPairs: Array<out Pair<String, Any>> = emptyArray(),
) {

    fun getCode() = "${resourceType}_${operation.id}_${code}"

    fun getMessage(): String {
        return messageTemplate.format(*arguments)
    }

    fun getMessageArguments(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        argumentPairs.forEach { map[it.first] = it.second }
        return map
    }

    fun forCreation() = forOperation(Operation.CREATE)

    fun forRetrieval() = forOperation(Operation.READ)

    fun forUpdate() = forOperation(Operation.UPDATE)

    fun forDelete() = forOperation(Operation.DELETE)

    fun forOperation(operation: Operation): ApplicationError {
        return ApplicationError(
            this.status,
            this.code,
            this.messageTemplate,
            operation,
            this.resourceType,
            this.arguments
        )
    }

    fun args(vararg args: Any): ApplicationError {
        return ApplicationError(
            this.status,
            this.code,
            this.messageTemplate,
            this.operation,
            this.resourceType,
            args
        )
    }

    fun args(vararg argumentPairs: Pair<String, Any>): ApplicationError {
        val arguments: List<Any> = argumentPairs.map { it.second }
        return ApplicationError(
            this.status,
            this.code,
            this.messageTemplate,
            this.operation,
            this.resourceType,
            arguments.toTypedArray(),
            argumentPairs
        )
    }

    companion object {
        val DEFAULT_ERROR = GeneralApplicationError(
            status = INTERNAL_SERVER_ERROR,
            code = 500,
            messageTemplate = "Could not fulfill the request because of an unexpected and unhandled exception."
        )
        val INVALID_BODY = GeneralApplicationError(
            status = BAD_REQUEST,
            code = 400,
            messageTemplate = "Invalid body has been provided in the request."
        )
        val NO_PRODUCT_FOUND = GeneralApplicationError(
            status = FORBIDDEN,
            code = 1001,
            messageTemplate = "Not allowed to access product '%s' or the product does not exist."
        )
        val INVALID_UUID = GeneralApplicationError(
            status = BAD_REQUEST,
            code = 1010,
            messageTemplate = "Invalid UUID has been provided '%s'."
        )
    }

    class GeneralApplicationError(status: HttpStatus, code: Int, messageTemplate: String) : ApplicationError(status = status, code = code, messageTemplate = messageTemplate) {
        fun forProduct(): ApplicationError = resource("PRD")
        fun resource(resourceType: String): ApplicationError {
            return ApplicationError(status = this.status, code = this.code, messageTemplate = this.messageTemplate, operation = this.operation, resourceType = resourceType, arguments = this.arguments)
        }
    }

    class ProductApplicationError(status: HttpStatus, code: Int, messageTemplate: String) : ApplicationError(status = status, code = code, messageTemplate = messageTemplate, resourceType = resource) {
        companion object {
            const val resource = "PRD"
        }
    }

    object Product {
        val NO_NAME = ProductApplicationError(
            status = BAD_REQUEST,
            code = 10001,
            messageTemplate = "No Product name provided."
        )
        val NAME_TOO_LONG = ProductApplicationError(
            status = BAD_REQUEST,
            code = 10002,
            messageTemplate = "Product name cannot be greater than %d characters."
        )
        val NAME_DEFINES_INVALID_CHAR = ProductApplicationError(
            status = BAD_REQUEST,
            code = 10003,
            messageTemplate = "Product name contains invalid characters. Letters a to z, the dash '-' and the underscore '_' are accepted."
        )
        val INVALID_PRODUCT_SORT = ProductApplicationError(
            status = BAD_REQUEST,
            code = 10004,
            messageTemplate = "Sort parameter '%s' is invalid, supported values are '%s'."
        )
        // TODO add validattion here
        val NO_PRODUCT_TYPE = ProductApplicationError(
            status = BAD_REQUEST,
            code = 10005,
            messageTemplate = "No Product Type provided."
        )
        val INVALID_PRODUCT_TYPE = ProductApplicationError(
            status = BAD_REQUEST,
            code = 10006,
            messageTemplate = "Product Type '%s' is not valid. Use either Online or Retail."
        )
        val DUPLICATE_PRODUCT_NAME = ProductApplicationError(
            status = CONFLICT,
            code = 20001,
            messageTemplate = "A Product with name '%s' already exists."
        )
    }

}
