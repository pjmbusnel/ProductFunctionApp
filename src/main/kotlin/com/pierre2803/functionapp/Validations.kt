package com.pierre2803.functionapp

import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.ISO_LOCAL_TIME
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import java.util.*

object Validations {

    private val alphaNumDashUnderscoreRegex = "[a-zA-Z0-9-_]+\$".toRegex()
    private val alphaAccentNumDashUnderscoreSpaceRegex = "(?i)^(?:(?![×Þß÷þø])[-_ 0-9a-zÀ-ÿ])+\$".toRegex()

    fun required(value: String?, operation: Operation, noValueError: ApplicationError): ApplicationError? {
        return if (value.isNullOrBlank()) noValueError.forOperation(operation).args(value ?: "") else null
    }

    fun required(value: Long?, operation: Operation, noValueError: ApplicationError): ApplicationError? {
        return if (value == null) noValueError.forOperation(operation) else null
    }

    fun required(value: List<Any>?, operation: Operation, noValueError: ApplicationError): ApplicationError? {
        return if (value.isNullOrEmpty()) noValueError.forOperation(operation) else null
    }

    fun required(value: String?, supportedValues: List<String>, operation: Operation, noValueError: ApplicationError, unsupportedEnumValueError: ApplicationError): ApplicationError? {
        if (value.isNullOrBlank()) return noValueError.forOperation(operation)
        if (!supportedValues.contains(value)) {
            val supportedValuesAsString = supportedValues.joinToString(separator = ", ")
            return unsupportedEnumValueError.forOperation(operation).args(value, supportedValuesAsString)
        }
        return null
    }

    fun validUUID(value: String?, resourceType: String, operation: Operation, invalidValueError: ApplicationError.GeneralApplicationError): ApplicationError? {
        if (value.isNullOrBlank()) return null
        return try {
            UUID.fromString(value)
            null
        } catch (_: Throwable) {
            invalidValueError.resource(resourceType).forOperation(operation).args(value)
        }
    }

    fun maxLength(value: String?, maxLength: Int, operation: Operation, valueTooLongError: ApplicationError): ApplicationError? {
        return if (!value.isNullOrBlank() && value.trim().length > maxLength) valueTooLongError.forOperation(operation).args(Pair("max_length", maxLength)) else null
    }

    fun positiveNumber(value: Long?, operation: Operation, invalidValueError: ApplicationError): ApplicationError? {
        if (value == null) return null
        return if (value <= 0) invalidValueError.forOperation(operation).args(value) else null
    }

    fun <T : Comparable<T>> greaterOrEqualsTo(value: T?, minValue: T, operation: Operation, invalidValueError: ApplicationError): ApplicationError? {
        if (value == null) return null
        return if (value < minValue) invalidValueError.forOperation(operation).args(value, minValue) else null
    }

    fun <T : Comparable<T>> greaterThan(value: T?, minValue: T, operation: Operation, invalidValueError: ApplicationError): ApplicationError? {
        if (value == null) return null
        return if (value <= minValue) invalidValueError.forOperation(operation).args(value, minValue) else null
    }

    fun <T : Comparable<T>> lowerOrEqualsTo(value: T?, maxValue: T, operation: Operation, invalidValueError: ApplicationError): ApplicationError? {
        if (value == null) return null
        return if (value > maxValue) invalidValueError.forOperation(operation).args(value, maxValue) else null
    }

    fun alphaNumDashUnderscore(value: String?, operation: Operation, invalidValueError: ApplicationError): ApplicationError? {
        return if (!value.isNullOrBlank() && !value.trim().matches(alphaNumDashUnderscoreRegex)) invalidValueError.forOperation(operation).args(value) else null
    }

    fun alphaAccentNumDashUnderscoreSpace(value: String?, operation: Operation, invalidValueError: ApplicationError): ApplicationError? {
        return if (!value.isNullOrBlank() && !value.trim().matches(alphaAccentNumDashUnderscoreSpaceRegex)) invalidValueError.forOperation(operation).args(value) else null
    }

    fun noBlankElement(list: List<String>?, resourceType: String, operation: Operation, blankListElementError: ApplicationError.GeneralApplicationError): ApplicationError? {
        if (list.isNullOrEmpty()) return null
        val blankCount = list.count { it.isBlank() }
        return if (blankCount > 0) blankListElementError.resource(resourceType).forOperation(operation).args(blankCount) else null
    }

    fun supportedValue(value: String?, supportedValues: List<String>, operation: Operation, invalidValueError: ApplicationError): ApplicationError? {
        if (value.isNullOrBlank()) return null
        if (!supportedValues.contains(value.trim())) {
            return invalidValueError.forOperation(operation).args(value, supportedValues.joinToString())
        }
        return null
    }

    fun validDate(value: String?, operation: Operation, invalidValueError: ApplicationError): ApplicationError? {
        if (value.isNullOrBlank()) return null
        return try {
            ISO_OFFSET_DATE_TIME.parse(value.trim())
            null
        } catch (_: Throwable) {
            invalidValueError.forOperation(operation).args(value)
        }
    }

    fun validTime(value: String?, operation: Operation, invalidValueError: ApplicationError): ApplicationError? {
        if (value.isNullOrBlank()) return null
        return try {
            ISO_LOCAL_TIME.parse(value.trim())
            null
        } catch (_: Throwable) {
            invalidValueError.forOperation(operation).args(value)
        }
    }

    fun dateTimeBeforeThan(dateTime: String?, beforeThan: String?, operation: Operation, errorToReturn: ApplicationError): ApplicationError? {
        val start = tryToParseDateTime(dateTime?.trim())
        val end = tryToParseDateTime(beforeThan?.trim())
        if (start != null && end != null && isAfterOrEqualDateTime(start, end)) {
            val arg1 = beforeThan ?: end
            val arg2 = dateTime ?: start
            return errorToReturn.forOperation(operation).args(arg1, arg2)
        }
        return null
    }

    private fun isAfterOrEqualDateTime(start: Instant, end: Instant?) = start.isAfter(end) || start == end

    fun validTimezone(value: String?, operation: Operation, invalidValueError: ApplicationError): ApplicationError? {
        return if (!value.isNullOrBlank() && !TimeZone.getAvailableIDs().contains(value.trim())) invalidValueError.forOperation(operation).args(value) else null
    }

    private fun tryToParseDateTime(v: String?): Instant? {
        return try {
            ISO_OFFSET_DATE_TIME.parse(v, ZonedDateTime::from).toInstant()
        } catch (ex: Throwable) {
            null
        }
    }

}
