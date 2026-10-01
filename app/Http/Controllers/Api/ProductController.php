<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Product;
use Illuminate\Http\Request;

class ProductController extends Controller
{
    /**
     * GET /api/products?category_id=&q=&bestseller=1
     */
    public function index(Request $request)
    {
        $query = Product::with('category')->available();

        if ($categoryId = $request->query('category_id')) {
            $query->where('category_id', $categoryId);
        }

        if ($request->boolean('bestseller')) {
            $query->where('is_bestseller', true);
        }

        if ($search = $request->query('q')) {
            $query->search($search);
        }

        $products = $query->orderBy('is_bestseller', 'desc')->get();

        return response()->json([
            'success' => true,
            'products' => $products,
        ]);
    }

    /**
     * GET /api/products/{id}
     */
    public function show(Product $product)
    {
        $product->load('category');

        return response()->json([
            'success' => true,
            'product' => $product,
        ]);
    }
}
