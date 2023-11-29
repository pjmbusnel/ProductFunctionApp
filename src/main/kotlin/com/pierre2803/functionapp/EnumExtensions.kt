package com.pierre2803.functionapp

inline fun <reified T : Enum<T>> safeValueOf(name: String): T? {
    return try {
        java.lang.Enum.valueOf(T::class.java, name.uppercase())
    } catch (e: IllegalArgumentException) {
        null
    }
}