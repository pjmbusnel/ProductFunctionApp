package com.pierre2803.functionapp

import com.microsoft.azure.functions.HttpStatus

class ConfigurationException(
        val httpStatus: HttpStatus,
        val errorCode: String,
        val errorMessage: String,
        val errorMessageArguments: Map<String, Any> = emptyMap(),
        ) : RuntimeException(errorMessage) {
    constructor(appError: ApplicationError) : this(
            httpStatus = appError.status,
            errorCode = appError.getCode(),
            errorMessage = appError.getMessage(),
            errorMessageArguments = appError.getMessageArguments()
    )
}
