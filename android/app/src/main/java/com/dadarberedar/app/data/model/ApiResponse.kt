package com.dadarberedar.app.data.model

import com.google.gson.annotations.SerializedName

data class ProductsResponse(
    val success: Boolean = false,
    val products: List<Product> = emptyList()
)

data class ProductResponse(
    val success: Boolean = false,
    val product: Product? = null
)

data class CategoriesResponse(
    val success: Boolean = false,
    val categories: List<Category> = emptyList()
)

data class OutletsResponse(
    val success: Boolean = false,
    val outlets: List<Outlet> = emptyList()
)

data class OutletResponse(
    val success: Boolean = false,
    val outlet: Outlet? = null
)

data class OutletProductsResponse(
    val success: Boolean = false,
    @SerializedName("outlet_id") val outletId: Long? = null,
    val products: List<Product> = emptyList()
)

data class OutletReviewsResponse(
    val success: Boolean = false,
    @SerializedName("outlet_id") val outletId: Long? = null,
    val reviews: List<Review> = emptyList()
)

data class ReviewsResponse(
    val success: Boolean = false,
    val reviews: List<Review> = emptyList()
)

data class OrdersResponse(
    val success: Boolean = false,
    val orders: List<Order> = emptyList()
)

data class OrderResponse(
    val success: Boolean = false,
    val order: Order? = null
)

data class OrderActionResponse(
    val success: Boolean = false,
    val message: String? = null,
    val order: Order? = null
)

data class OrderTrackingResponse(
    val success: Boolean = false,
    val order: Order? = null
)

data class OrderCreateResponse(
    val success: Boolean = false,
    val message: String? = null,
    val order: Order? = null
)

data class SearchResponse(
    val success: Boolean = false,
    val query: String? = null,
    val products: List<Product> = emptyList(),
    val outlets: List<Outlet> = emptyList(),
    val total: Int = 0
)
