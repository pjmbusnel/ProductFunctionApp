package com.pierre2803.functionapp.product

import com.pierre2803.functionapp.ApplicationError.Companion.NO_PRODUCT_FOUND
import com.pierre2803.functionapp.ApplicationError.Product.DUPLICATE_PRODUCT_NAME
import com.pierre2803.functionapp.ConfigurationException
import com.pierre2803.functionapp.DatabaseClient
import com.pierre2803.functionapp.Operation
import com.pierre2803.functionapp.Operation.CREATE
import com.pierre2803.functionapp.Operation.UPDATE
import com.pierre2803.functionapp.db.ProductDB
import com.pierre2803.functionapp.db.ProductDBCreation
import com.pierre2803.functionapp.db.ProductDBUpdate
import com.pierre2803.functionapp.db.ProductsRepository
import com.pierre2803.functionapp.product.ProductsRetrievalSort.NAME
import java.util.*

class ProductService(private val productsRepository: ProductsRepository = ProductsRepository(DatabaseClient.getConnectionPool())) {

    fun createProduct(productDBCreation: ProductDBCreation): Product {
        try {
            productsRepository.createProduct(toProductCreationDB(productDBCreation))
            return toProduct(productDBCreation)
        } catch (ex: Exception) {
            assertUniqueName(productDBCreation.name, CREATE)
            throw ex
        }
    }

    fun updateProduct(productUpdate: ProductUpdate): Product {
        if (update(productUpdate))
            return getProduct(productUpdate.id)
        else
            throw ConfigurationException(NO_PRODUCT_FOUND.forProduct().forUpdate().args(productUpdate.id))
    }

    fun getProduct(productId: UUID): Product {
        return productsRepository.getProduct(productId)
            ?.takeIf { it.enabled }
            ?. let { toProduct(it) }
            ?: throw ConfigurationException(NO_PRODUCT_FOUND.forProduct().forRetrieval().args(productId))
    }

    fun getProducts(productsRetrieval: ProductsRetrieval): List<Product> {
        val sortComparator = getSorting(productsRetrieval.sort)
        return productsRepository.getProducts()
            .filter { it.enabled }
            .map { toProduct(it) }
            .sortedWith(sortComparator)
    }

    fun disableProduct(productId: UUID) {
        if (!productsRepository.disableProduct(productId))
            throw ConfigurationException(NO_PRODUCT_FOUND.forProduct().forDelete().args(productId))
    }

    private fun update(productUpdate: ProductUpdate): Boolean {
        try {
            return productsRepository.updateProduct(toProductUpdateDB(productUpdate))
        } catch (ex: Exception) {
            assertUniqueName(productUpdate.name, UPDATE)
            throw ex
        }
    }

    private fun assertUniqueName(name: String, operation: Operation) {
        if (productsRepository.isNameExists(name)) throw ConfigurationException(DUPLICATE_PRODUCT_NAME.forOperation(operation).args(name))
    }

    private fun getSorting(sortBy: ProductsRetrievalSort): Comparator<Product> {
        return when (sortBy) {
            NAME -> compareBy { it.name }
        }
    }

    private fun toProductCreationDB(productCreation: ProductDBCreation): ProductDBCreation {
        return ProductDBCreation(
            id = productCreation.id,
            name = productCreation.name,
            productType = productCreation.productType
        )
    }

    private fun toProductUpdateDB(productCreation: ProductUpdate): ProductDBUpdate {
        return ProductDBUpdate(
            id = productCreation.id,
            name = productCreation.name,
            productType = productCreation.productType
        )
    }

    private fun toProduct(productDB: ProductDB): Product {
        return Product(
            id = productDB.id,
            name = productDB.name,
            enabled = productDB.enabled,
            //deletionTime  = productDB.deletionTime
        )
    }

    private fun toProduct(productDBCreation: ProductDBCreation): Product {
        return Product(
            id = productDBCreation.id,
            name = productDBCreation.name,
            enabled = productDBCreation.enabled,
            //deletionTime  = productCreation.deletionTime
        )
    }
}