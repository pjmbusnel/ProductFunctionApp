package com.pierre2803.functionapp

class ValidationException(private vararg val applicationError: ApplicationError) : RuntimeException() {
    fun getApplicationErrors() = applicationError.asList()
}
