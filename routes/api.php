<?php

use App\Http\Controllers\Api\AuthController;
use App\Http\Controllers\Api\CategoryController;
use App\Http\Controllers\Api\OrderController as ApiOrderController;
use App\Http\Controllers\Api\OutletController as ApiOutletController;
use App\Http\Controllers\Api\PaymentWebhookController;
use App\Http\Controllers\Api\ProductController;
use App\Http\Controllers\Api\ReviewController;
use App\Http\Controllers\Api\SearchController;
use Illuminate\Support\Facades\Route;

/*
|--------------------------------------------------------------------------
| Public API routes
|--------------------------------------------------------------------------
*/

Route::post('/register', [AuthController::class, 'register']);
Route::post('/login', [AuthController::class, 'login']);

Route::get('/search', [SearchController::class, 'search']);
Route::get('/categories', [CategoryController::class, 'index']);

Route::get('/outlets', [ApiOutletController::class, 'index']);
Route::get('/outlets/nearby', [ApiOutletController::class, 'nearby']);
Route::get('/outlets/{outlet}', [ApiOutletController::class, 'show']);
Route::get('/outlets/{outlet}/products', [ApiOutletController::class, 'products']);
Route::get('/outlets/{outlet}/reviews', [ApiOutletController::class, 'reviews']);

Route::get('/products', [ProductController::class, 'index']);
Route::get('/products/{product}', [ProductController::class, 'show']);

Route::get('/reviews', [ReviewController::class, 'index']);

// Guest checkout is allowed; order is linked to the user when a Sanctum token is sent.
Route::post('/orders', [ApiOrderController::class, 'store']);
Route::get('/orders/{orderNumber}', [ApiOrderController::class, 'show']);
Route::put('/orders/{orderNumber}', [ApiOrderController::class, 'update']);
Route::post('/orders/{orderNumber}/cancel', [ApiOrderController::class, 'cancel']);
Route::post('/orders/{orderNumber}/simulate-payment', [ApiOrderController::class, 'simulatePayment']);
Route::post('/orders/{orderNumber}/simulate-status', [ApiOrderController::class, 'simulateStatus']);

Route::post('/webhooks/payment', [PaymentWebhookController::class, 'handleWebhook']);

/*
|--------------------------------------------------------------------------
| Authenticated API routes (Sanctum bearer token)
|--------------------------------------------------------------------------
*/

Route::middleware('auth:sanctum')->group(function () {
    Route::get('/user', [AuthController::class, 'user']);
    Route::post('/logout', [AuthController::class, 'logout']);
    Route::get('/orders', [ApiOrderController::class, 'myOrders']);
});
