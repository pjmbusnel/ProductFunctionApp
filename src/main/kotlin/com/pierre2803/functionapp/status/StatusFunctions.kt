package com.pierre2803.functionapp.status

import com.microsoft.azure.functions.HttpMethod.GET
import com.microsoft.azure.functions.HttpRequestMessage
import com.microsoft.azure.functions.HttpResponseMessage
import com.microsoft.azure.functions.HttpStatus
import com.microsoft.azure.functions.annotation.AuthorizationLevel.ANONYMOUS
import com.microsoft.azure.functions.annotation.FunctionName
import com.microsoft.azure.functions.annotation.HttpTrigger
import com.pierre2803.functionapp.DatabaseClient
import com.pierre2803.functionapp.db.FlywayRepository
import java.util.*


class StatusFunctions(
    private val flywayRepository: FlywayRepository = FlywayRepository(DatabaseClient.getConnectionPool())
) {

    @FunctionName("GetServiceStatus")
    fun getStatus(
        @HttpTrigger(
            route = "status",
            name = "request",
            methods = [GET],
            authLevel = ANONYMOUS) request: HttpRequestMessage<String>): HttpResponseMessage {

        val gitProperties = readPropertyFile("git.properties")
        val flywayInfo = flywayRepository.getFlywayInfo()

        return request
            .createResponseBuilder(HttpStatus.OK)
            .body(combineStatus(gitProperties, flywayInfo))
            .build()
    }

    private fun readPropertyFile(fileName: String): Properties {
        val properties = Properties()
        try {
            properties.load(javaClass.classLoader.getResourceAsStream(fileName))
        } catch (_: Throwable) {
            properties.setProperty("git.branch","Failed to retrieve $fileName")
        }
        return properties
    }

    private fun combineStatus(gitProperties: Properties, flywayInfo: FlywayRepository.FlywayInfo): ServiceStatus {
        return ServiceStatus(
            gitBranch = gitProperties.getProperty("git.branch"),
            gitCommitIdAbbrev = gitProperties.getProperty("git.commit.id.abbrev"),
            gitCommitTime = gitProperties.getProperty("git.commit.time"),
            gitBuildHost = gitProperties.getProperty("git.build.host"),
            gitBuildTime = gitProperties.getProperty("git.build.time"),
            dbVersion = flywayInfo.version,
            dbVersionDescription = flywayInfo.description,
            dbVersionInstalledOn = flywayInfo.installedOn.substringBefore("."))
    }

    data class ServiceStatus(
        val gitBranch: String? = null,
        val gitCommitIdAbbrev: String? = null,
        val gitCommitTime: String? = null,
        val gitBuildHost: String? = null,
        val gitBuildTime: String? = null,
        val dbVersion: Double? = null,
        val dbVersionDescription: String? = null,
        val dbVersionInstalledOn: String? = null)

}

