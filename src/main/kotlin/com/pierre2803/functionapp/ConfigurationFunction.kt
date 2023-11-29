package com.pierre2803.functionapp

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonProperty
import com.microsoft.azure.functions.HttpRequestMessage
import com.microsoft.azure.functions.HttpResponseMessage
import com.microsoft.azure.functions.HttpStatus
import com.pierre2803.functionapp.ApplicationError.Companion.DEFAULT_ERROR
import com.pierre2803.functionapp.ApplicationError.Companion.INVALID_BODY
import com.pierre2803.functionapp.JsonUtils.fromJson
import com.pierre2803.functionapp.JsonUtils.toJson
import com.pierre2803.functionapp.Operation.Companion.toOperation
import io.reactivex.rxjava3.core.Observable
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

abstract class ConfigurationFunction {

    private val transformErrorToJsonFunction: (com.pierre2803.functionapp.ApplicationError) -> String = { errorToJson(code = it.getCode(), msg = it.getMessage(), args = it.getMessageArguments()) }

    protected inline fun <reified OUTPUT : Any> extractBody(request: HttpRequestMessage<String>, resourceType: String, logger: Logger? = null): Observable<OUTPUT> {
        val jsonBody = request.body
                ?: return Observable.error(ConfigurationException(INVALID_BODY.resource(resourceType).forOperation(toOperation(request.httpMethod))))
        return try {
            val obj = fromJson<OUTPUT>(jsonBody)
            Observable.fromCallable { obj }
        } catch (t: Throwable) {
            logger?.log(Level.SEVERE, "Unable to parse JSON body ($jsonBody).", t)
            Observable.error(ConfigurationException(INVALID_BODY.resource(resourceType).forOperation(toOperation(request.httpMethod))))
        }
    }

    @Suppress("SameParameterValue")
    protected fun extractQueryParam(request: HttpRequestMessage<*>, paramName: String) = request.queryParameters[paramName]
            ?: ""

    protected fun <INPUT> generateLocation(resourceId: UUID, request: HttpRequestMessage<INPUT>): String {
        return "${request.uri.toASCIIString()}/$resourceId"
    }

    protected fun <INPUT> toCreatedResponse(resource: INPUT, newlyCreatedLocation: String? = null, request: HttpRequestMessage<*>): HttpResponseMessage {
        val response = request.createResponseBuilder(HttpStatus.CREATED)
            .header("Content-Type", "application/json")
            .body(toJson(resource))

        if (!newlyCreatedLocation.isNullOrBlank()) response.header("Location", newlyCreatedLocation)

        return response.build()
    }

    protected fun <INPUT> toOkResponse(resource: INPUT, request: HttpRequestMessage<String>): HttpResponseMessage {
        return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(toJson(resource))
                .build()
    }

    protected fun toErrorResponse(ex: Throwable, request: HttpRequestMessage<*>, resourceType: String = "GENERAL", operation: Operation = Operation.READ, logger: Logger? = null): HttpResponseMessage {
        return when (ex) {
            is ConfigurationException -> toErrorResponse(ex.httpStatus, ex.errorCode, ex.errorMessage, ex.errorMessageArguments, request)
            is ValidationException -> toErrorResponse(ex.getApplicationErrors(), request)
            else -> toDefaultErrorMessage(request, resourceType, operation, logger, ex)
        }
    }

    private fun toErrorResponse(errors: List<ApplicationError>, request: HttpRequestMessage<*>): HttpResponseMessage {
        val highestHttpStatus = errors.maxOf { it.status.value() }
        val errorsJson = errors.joinToString(separator = ", ", transform = transformErrorToJsonFunction)
        val json = """{"errors": [$errorsJson]}"""
        return request.createResponseBuilder(HttpStatus.valueOf(highestHttpStatus))
                .header("Content-Type", "application/json")
                .body(json)
                .build()
    }

    private fun toErrorResponse(httpStatus: HttpStatus, errorCode: String, errorMessage: String, messageArguments: Map<String, Any>, request: HttpRequestMessage<*>): HttpResponseMessage {
        val errorJson = """{"errors": [ ${errorToJson(errorCode, errorMessage, messageArguments)} ]}"""
        return request.createResponseBuilder(httpStatus)
                .header("Content-Type", "application/json")
                .body(errorJson)
                .build()
    }

    private fun errorToJson(code: String, msg: String, args: Map<String, Any>): String {
        return toJson(ErrorsForJson(code = code, message = msg, arguments = if (args.isEmpty()) null else args))
    }

    protected fun toDefaultErrorMessage(request: HttpRequestMessage<*>, resourceType: String = "GENERAL", operation: Operation = Operation.READ, logger: Logger? = null, ex: Throwable? = null): HttpResponseMessage {
        val applicationError = DEFAULT_ERROR.resource(resourceType)
        val errorJson = """{"errors": [ {"code": "${applicationError.forOperation(operation).getCode()}", "message": "${applicationError.getMessage()}"} ]}"""
        ex?.let { logger?.log(Level.SEVERE, it.message) }
        return request.createResponseBuilder(applicationError.status)
                .header("Content-Type", "application/json")
                .body(errorJson)
                .build()
    }

    private data class ErrorsForJson(
            @JsonProperty("code") val code: String,
            @JsonProperty("message") val message: String,
            @JsonProperty("arguments") @JsonInclude(NON_NULL) val arguments: Map<String, Any>? = null)
}
