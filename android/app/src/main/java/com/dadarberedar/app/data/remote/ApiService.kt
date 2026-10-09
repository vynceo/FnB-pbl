package com.dadarberedar.app.data.remote

import com.dadarberedar.app.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("api/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("api/user")
    suspend fun getUser(@Header("Authorization") token: String): Response<User>

    @POST("api/logout")
    suspend fun logout(@Header("Authorization") token: String): Response<Map<String, String>>

    @GET("api/search")
    suspend fun search(@Query("q") query: String): Response<SearchResponse>

    @GET("api/categories")
    suspend fun getCategories(): Response<CategoriesResponse>

    @GET("api/outlets")
    suspend fun getOutlets(@Query("q") query: String? = null): Response<OutletsResponse>

    @GET("api/outlets/nearby")
    suspend fun getNearbyOutlets(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double
    ): Response<OutletsResponse>

    @GET("api/outlets/{id}")
    suspend fun getOutletDetail(@Path("id") id: Long): Response<OutletResponse>

    @GET("api/outlets/{id}/products")
    suspend fun getOutletProducts(
        @Path("id") id: Long,
        @Query("category_id") categoryId: Long? = null
    ): Response<OutletProductsResponse>

    @GET("api/outlets/{id}/reviews")
    suspend fun getOutletReviews(@Path("id") id: Long): Response<OutletReviewsResponse>

    @GET("api/products")
    suspend fun getProducts(
        @Query("category_id") categoryId: Long? = null,
        @Query("bestseller") bestseller: Boolean? = null,
        @Query("q") query: String? = null
    ): Response<ProductsResponse>

    @GET("api/products/{id}")
    suspend fun getProductDetail(@Path("id") id: Long): Response<ProductResponse>

    @GET("api/reviews")
    suspend fun getReviews(
        @Query("featured") featured: Boolean? = null
    ): Response<ReviewsResponse>

    @POST("api/orders")
    suspend fun createOrder(
        @Header("Authorization") token: String?,
        @Body request: CheckoutRequest
    ): Response<OrderCreateResponse>

    @GET("api/orders/{orderNumber}")
    suspend fun getOrderTracking(@Path("orderNumber") orderNumber: String): Response<OrderTrackingResponse>

    @PUT("api/orders/{orderNumber}")
    suspend fun updateOrder(
        @Path("orderNumber") orderNumber: String,
        @Header("Authorization") token: String?,
        @Body request: OrderUpdateRequest
    ): Response<OrderActionResponse>

    @POST("api/orders/{orderNumber}/cancel")
    suspend fun cancelOrder(
        @Path("orderNumber") orderNumber: String,
        @Header("Authorization") token: String?
    ): Response<OrderActionResponse>

    @POST("api/orders/{orderNumber}/simulate-payment")
    suspend fun simulatePayment(@Path("orderNumber") orderNumber: String): Response<OrderActionResponse>

    @POST("api/orders/{orderNumber}/simulate-status")
    suspend fun simulateStatus(
        @Path("orderNumber") orderNumber: String,
        @Body body: Map<String, String>
    ): Response<OrderActionResponse>

    @GET("api/orders")
    suspend fun getMyOrders(@Header("Authorization") token: String): Response<OrdersResponse>
}
