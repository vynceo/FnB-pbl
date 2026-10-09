package com.dadarberedar.app.data.model

import com.google.gson.annotations.SerializedName

data class OrderItem(
    val id: Long? = null,
    @SerializedName("product_id") val productId: Long,
    @SerializedName("product_name") val productName: String = "",
    val price: Double = 0.0,
    val quantity: Int = 1,
    @SerializedName("spice_level") val spiceLevel: Int = 0,
    val notes: String? = null,
    val subtotal: Double = 0.0
)

data class Order(
    val id: Long,
    @SerializedName("order_number") val orderNumber: String,
    @SerializedName("outlet_id") val outletId: Long,
    @SerializedName("customer_name") val customerName: String = "",
    @SerializedName("customer_phone") val customerPhone: String = "",
    @SerializedName("customer_email") val customerEmail: String? = null,
    @SerializedName("delivery_type") val deliveryType: String = "takeaway",
    @SerializedName("delivery_address") val deliveryAddress: String? = null,
    @SerializedName("delivery_notes") val deliveryNotes: String? = null,
    val subtotal: Double = 0.0,
    @SerializedName("delivery_fee") val deliveryFee: Double = 0.0,
    @SerializedName("tax_amount") val taxAmount: Double = 0.0,
    @SerializedName("total_amount") val totalAmount: Double = 0.0,
    @SerializedName("payment_method") val paymentMethod: String? = null,
    @SerializedName("payment_status") val paymentStatus: String = "pending",
    @SerializedName("order_status") val orderStatus: String = "received",
    val items: List<OrderItem>? = null,
    val outlet: Outlet? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

data class CheckoutItem(
    @SerializedName("product_id") val productId: Long,
    val quantity: Int,
    val notes: String? = null,
    @SerializedName("spice_level") val spiceLevel: Int = 0
)

data class CheckoutRequest(
    @SerializedName("outlet_id") val outletId: Long,
    @SerializedName("customer_name") val customerName: String,
    @SerializedName("customer_phone") val customerPhone: String,
    @SerializedName("customer_email") val customerEmail: String? = null,
    @SerializedName("delivery_type") val deliveryType: String = "takeaway",
    @SerializedName("delivery_address") val deliveryAddress: String? = null,
    @SerializedName("delivery_notes") val deliveryNotes: String? = null,
    @SerializedName("payment_method") val paymentMethod: String,
    val items: List<CheckoutItem>
)

data class OrderUpdateRequest(
    @SerializedName("customer_name") val customerName: String? = null,
    @SerializedName("customer_phone") val customerPhone: String? = null,
    @SerializedName("delivery_address") val deliveryAddress: String? = null,
    @SerializedName("delivery_notes") val deliveryNotes: String? = null
)

data class Review(
    val id: Long,
    @SerializedName("outlet_id") val outletId: Long,
    @SerializedName("author_name") val authorName: String = "",
    @SerializedName("author_avatar") val authorAvatar: String? = null,
    val rating: Float = 5.0f,
    val comment: String = "",
    @SerializedName("relative_time") val relativeTime: String = "",
    @SerializedName("verified_buyer") val verifiedBuyer: Boolean = true,
    @SerializedName("is_featured") val isFeatured: Boolean = false
)
