package com.pierre2803.functionapp.db

import java.sql.Connection
import java.sql.ResultSet
import java.util.*

@Suppress("UNUSED")
class ProductsRepository(private val databaseConnectionPool: javax.sql.DataSource) {

    fun createProduct(productDBCreation: ProductDBCreation) {
        return databaseConnectionPool.connection.use { createProduct(productDBCreation, it) }
    }

    fun updateProduct(product: ProductDBUpdate): Boolean {
        return databaseConnectionPool.connection.use { updateProduct(product, connection = it) }
    }

    fun getProduct(productId: UUID): ProductDB? {
        return databaseConnectionPool.connection.use { getProduct(productId, connection = it) }
    }

    fun getProducts(): List<ProductDB> {
        return databaseConnectionPool.connection.use { selectProducts(connection = it) }
    }

    fun disableProduct(productId: UUID): Boolean {
        return databaseConnectionPool.connection.use { disable(productId = productId, connection = it) }
    }

    fun isProductExists(productId: UUID): Boolean {
        return databaseConnectionPool.connection.use { isProductExists(productId = productId, connection = it) }
    }

    fun isNameExists(name: String): Boolean {
        return databaseConnectionPool.connection.use { isNameExists(name = name, connection = it) }
    }

    private fun createProduct(productDBCreation: ProductDBCreation, connection: Connection) {
        val insertSql = connection.prepareStatement("INSERT INTO products (id,name,product_type,enabled) VALUES (?,?,?,?)")
        insertSql.setObject(1, productDBCreation.id)
        insertSql.setString(2, productDBCreation.name)
        insertSql.setString(3, productDBCreation.productType.value)
        insertSql.setBoolean(4, productDBCreation.enabled)
        insertSql.execute()
    }

    private fun getProduct(productId: UUID, connection: Connection): ProductDB? {
        val select = connection.prepareStatement("SELECT id,name,product_type,enabled,creation_time,last_update_time,deletion_time FROM products where id = ?")
        select.setObject(1, productId)
        val rs = select.executeQuery()
        return if (rs.next()) toProductDB(rs) else null
    }

    private fun updateProduct(productDBUpdate: ProductDBUpdate, connection: Connection): Boolean {
        val sql = "UPDATE products SET name = ?, last_update_time=now() where enabled=true AND id = ?"
        val updateSql = connection.prepareStatement(sql)
        updateSql.setString(1, productDBUpdate.name)
        updateSql.setObject(2, productDBUpdate.id)

        val updated = updateSql.executeUpdate()
        return updated == 1
    }

    private fun selectProducts(connection: Connection): List<ProductDB> {
        val select = connection.prepareStatement("SELECT id,name,product_type,enabled,creation_time,last_update_time,deletion_time FROM products")
        val rs = select.executeQuery()
        val list = mutableListOf<ProductDB>()
        while (rs.next()) {
            list.add(toProductDB(rs))
        }
        return list
    }

    private fun disable(productId: UUID, connection: Connection): Boolean {
        val sql = "UPDATE products SET enabled = false, last_update_time=now() where enabled=true AND id = ?"
        val updateSQL = connection.prepareStatement(sql)
        updateSQL.setObject(1, productId)
        val updated = updateSQL.executeUpdate()
        return updated == 1
    }

    private fun isProductExists(productId: UUID, connection: Connection): Boolean {
        val selectIdExistsSql = connection.prepareStatement("SELECT EXISTS(SELECT 1 FROM products WHERE enabled=true AND id = ?)")
        selectIdExistsSql.setObject(1, productId)
        val rs = selectIdExistsSql.executeQuery()
        return rs.next() && rs.getBoolean(1)
    }

    private fun isNameExists(name: String, connection: Connection): Boolean {
        val selectNameExistsSql = connection.prepareStatement("SELECT EXISTS(SELECT 1 FROM products WHERE name = ?)")
        selectNameExistsSql.setString(1, name)
        val rs = selectNameExistsSql.executeQuery()
        return rs.next() && rs.getBoolean(1)
    }

    private fun toProductDB(resultSet: ResultSet): ProductDB {
        return ProductDB(
                id = resultSet.getObject("id", UUID::class.java),
                name = resultSet.getString("name"),
                productType = ProductType.forValue(resultSet.getString("product_type").uppercase()),
                enabled = resultSet.getBoolean("enabled"),
                creationTime = resultSet.getTimestamp("creation_time").time,
                lastUpdateTime = resultSet.getTimestamp("last_update_time").time,
                deletionTime = resultSet.getTimestamp("deletion_time")?.time)
    }
}