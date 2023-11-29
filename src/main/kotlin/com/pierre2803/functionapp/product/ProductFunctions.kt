package com.pierre2803.functionapp.product

import com.microsoft.azure.functions.HttpMethod.*
import com.microsoft.azure.functions.HttpRequestMessage
import com.microsoft.azure.functions.HttpResponseMessage
import com.microsoft.azure.functions.annotation.AuthorizationLevel.ANONYMOUS
import com.microsoft.azure.functions.annotation.BindingName
import com.microsoft.azure.functions.annotation.FunctionName
import com.microsoft.azure.functions.annotation.HttpTrigger
import com.pierre2803.functionapp.ConfigurationFunction
import io.reactivex.rxjava3.core.Observable
import com.pierre2803.functionapp.ApplicationError.ProductApplicationError.Companion.resource as PRODUCT

class ProductFunctions(
    private val productService: ProductService = ProductService()) : ConfigurationFunction() {

    @FunctionName("ProductCreation")
    fun createProduct(
            @HttpTrigger(
                    route = "products",
                    name = "request",
                    methods = [POST],
                    authLevel = ANONYMOUS) request: HttpRequestMessage<String>): HttpResponseMessage {

        return extractBody<ProductCreationRequest>(request, PRODUCT)
                .map { it.validateAndTransform() }
                .map { productService.createProduct(it) }
                .map { toCreatedResponse<Product>(resource = it, newlyCreatedLocation = generateLocation(it.id, request), request = request) }
                .onErrorReturn { toErrorResponse(it, request) }
                .blockingSingle(toDefaultErrorMessage(request))
    }

    @FunctionName("GetProduct")
    fun getProduct(
            @HttpTrigger(
                    name = "request",
                    route = "products/{productId}",
                    methods = [GET],
                    authLevel = ANONYMOUS) request: HttpRequestMessage<String>,
            @BindingName("productId") productId: String): HttpResponseMessage {

        return Observable.fromCallable { ProductRetrievalRequest(productId) }
                .map { it.validateAndTransform() }
                .map { productService.getProduct(it) }
                .map { toOkResponse(it, request) }
                .onErrorReturn { toErrorResponse(it, request) }
                .blockingSingle(toDefaultErrorMessage(request))
    }

    @FunctionName("GetProducts")
    fun getProducts(
            @HttpTrigger(
                    name = "request",
                    route = "products",
                    methods = [GET],
                    authLevel = ANONYMOUS) request: HttpRequestMessage<String>): HttpResponseMessage {

        return Observable.fromCallable { extractQueryParam(request, "sort") }
                .map { ProductsRetrievalRequest(it) }
                .map { it.validateAndTransform() }
                .map { productService.getProducts(it) }
                .map { toOkResponse(it, request) }
                .onErrorReturn { toErrorResponse(it, request) }
                .blockingSingle(toDefaultErrorMessage(request))
    }

    @FunctionName("UpdateProduct")
    fun updateProduct(
            @HttpTrigger(
                    name = "request",
                    route = "products/{productId}",
                    methods = [PUT],
                    authLevel = ANONYMOUS) request: HttpRequestMessage<String>,
            @BindingName("productId") productId: String): HttpResponseMessage {

        return extractBody<ProductUpdateRequest>(request, PRODUCT)
                .map { it.validateAndTransform(productId) }
                .map { productService.updateProduct(it) }
                .map { toOkResponse(it, request) }
                .onErrorReturn { toErrorResponse(it, request) }
                .blockingSingle(toDefaultErrorMessage(request))
    }

}