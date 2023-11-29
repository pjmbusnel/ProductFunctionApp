package com.pierre2803.functionapp

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.time.DayOfWeek
import java.time.Month
import java.util.*


object JsonUtils {

    val objectMapper = buildObjectMapper()

    fun <T> toJson(o: T): String = objectMapper.writeValueAsString(o)

    inline fun <reified T : Any> fromJson(json: String): T {
        val typeReference: TypeReference<T> = object : TypeReference<T>() {}
        return objectMapper.readValue(json, typeReference)
    }

    private fun buildObjectMapper(): ObjectMapper {
        val myModule = SimpleModule()
        myModule.addSerializer(Locale::class.java, LocaleSerializer())
        myModule.addDeserializer(Locale::class.java, LocaleDeserializer())
        myModule.addSerializer(Month::class.java, MonthSerializer())
        myModule.addDeserializer(Month::class.java, MonthDeserializer())
        myModule.addSerializer(DayOfWeek::class.java, DayOfWeekSerializer())
        myModule.addDeserializer(DayOfWeek::class.java, DayOfWeekDeserializer())

        val mapper = ObjectMapper()
        mapper.registerModule(JavaTimeModule())
        mapper.registerModule(KotlinModule())
        mapper.registerModule(myModule)
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        mapper.propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        return mapper
    }
}

class LocaleSerializer : JsonSerializer<Locale>() {
    override fun serialize(locale: Locale, jgen: JsonGenerator, provider: SerializerProvider) {
        jgen.writeString(locale.language)
    }
}

class LocaleDeserializer : JsonDeserializer<Locale>() {
    override fun deserialize(parser: JsonParser, context: DeserializationContext): Locale {
        return Locale(parser.text)
    }
}

class MonthSerializer : JsonSerializer<Month>() {
    override fun serialize(month: Month, jgen: JsonGenerator, provider: SerializerProvider) {
        jgen.writeString(month.name.lowercase())
    }
}

class MonthDeserializer : JsonDeserializer<Month>() {
    override fun deserialize(jsonParser: JsonParser, context: DeserializationContext) = Month.valueOf(jsonParser.text.uppercase())
}

class DayOfWeekSerializer : JsonSerializer<DayOfWeek>() {
    override fun serialize(day: DayOfWeek, jgen: JsonGenerator, provider: SerializerProvider) {
        jgen.writeString(day.name.lowercase())
    }
}

class DayOfWeekDeserializer : JsonDeserializer<DayOfWeek>() {
    override fun deserialize(jsonParser: JsonParser, context: DeserializationContext) = DayOfWeek.valueOf(jsonParser.text.uppercase())
}
