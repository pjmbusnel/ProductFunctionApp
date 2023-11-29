package com.pierre2803.functionapp

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

object DatabaseClient {

    private val databaseConnectionPool = createConnectionPool()

    private fun createConnectionPool(): HikariDataSource {
        val config = HikariConfig()
        config.jdbcUrl = getDatabaseProperty("WX_DB_BASIC_URL")
        config.username = getDatabaseProperty("WX_DB_USER")
        config.password = getDatabaseProperty("WX_DB_PASSWORD")
        config.isAutoCommit = true
        config.maximumPoolSize = getDatabaseProperty("WX_DB_CONNECTION_POOL_MAX_SIZE").toInt()
        config.addDataSourceProperty("sslmode", getDatabaseProperty("WX_DB_SSL_MODE"))
        config.addDataSourceProperty("cachePrepStmts", "true")
        config.addDataSourceProperty("useServerPrepStmts", "true")
        config.addDataSourceProperty("prepStmtCacheSize", getDatabaseProperty("WX_DB_PREPARED_STATEMENT_CACHE_SIZE"))
        config.addDataSourceProperty("prepStmtCacheSqlLimit", getDatabaseProperty("WX_DB_PREPARED_STATEMENT_CACHE_SQL_MAX_LENGTH"))
        return HikariDataSource(config)
    }

    fun getConnectionPool() = databaseConnectionPool

    private fun getDatabaseProperty(name: String): String {
        return System.getenv(name) ?: throw IllegalArgumentException("Could not retrieve the Database $name for the configuration entities.")
    }
}
