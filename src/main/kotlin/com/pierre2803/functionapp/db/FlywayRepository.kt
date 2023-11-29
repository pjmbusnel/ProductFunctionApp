package com.pierre2803.functionapp.db

import java.sql.Connection

class FlywayRepository(private val databaseConnectionPool: javax.sql.DataSource) {

    fun getFlywayInfo(): FlywayInfo {
        return databaseConnectionPool.connection.use { selectFlywayInfo(connection = it) }
    }

    private fun selectFlywayInfo(connection: Connection): FlywayInfo {
        val sql = """
            SELECT version, description, installed_on 
            FROM flyway_schema_history
            ORDER BY installed_rank desc limit 1
            """.trimIndent()
        val statement = connection.prepareStatement(sql)
        val rs = statement.executeQuery()
        if(rs.next()) {
            val version = rs.getDouble("version")
            val description = rs.getString("description")
            val installedOn = rs.getString("installed_on")
            return FlywayInfo(version, description,  installedOn)
        }
        return FlywayInfo(Double.NaN, "No version found", "-")
    }

    data class FlywayInfo(val version: Double, val description: String, val installedOn: String) {

    }
}
