<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Outlet;
use Illuminate\Http\Request;

class OutletController extends Controller
{
    /**
     * GET /api/outlets
     */
    public function index(Request $request)
    {
        $outlets = Outlet::active()
            ->search($request->query('q'))
            ->orderBy('name')
            ->get();

        return response()->json([
            'success' => true,
            'outlets' => $outlets,
        ]);
    }

    /**
     * GET /api/outlets/nearby?lat=&lng=
     * Response shape kept identical to the existing web endpoint
     * consumed by resources/views/home.blade.php.
     */
    public function nearby(Request $request)
    {
        $lat = (float) $request->query('lat');
        $lng = (float) $request->query('lng');

        if (! $lat || ! $lng) {
            $outlets = Outlet::where('is_active', true)->get();

            return response()->json([
                'success' => true,
                'outlets' => $outlets,
            ]);
        }

        $outlets = Outlet::where('is_active', true)->get()->map(function ($outlet) use ($lat, $lng) {
            $outlet->distance_km = $outlet->calculateDistance($lat, $lng);

            return $outlet;
        })->sortBy('distance_km')->values();

        return response()->json([
            'success' => true,
            'user_location' => ['lat' => $lat, 'lng' => $lng],
            'closest' => $outlets->first(),
            'outlets' => $outlets,
        ]);
    }

    /**
     * GET /api/outlets/{id}
     */
    public function show(Outlet $outlet)
    {
        return response()->json([
            'success' => true,
            'outlet' => $outlet,
        ]);
    }

    /**
     * GET /api/outlets/{id}/products
     * Menu catalog for an outlet, grouped implicitly by category via ?category_id=
     */
    public function products(Request $request, Outlet $outlet)
    {
        $query = \App\Models\Product::with('category')
            ->available()
            ->where('outlet_id', $outlet->id);

        if ($categoryId = $request->query('category_id')) {
            $query->where('category_id', $categoryId);
        }

        if ($search = $request->query('q')) {
            $query->search($search);
        }

        $products = $query->orderBy('is_bestseller', 'desc')->get();

        return response()->json([
            'success' => true,
            'outlet_id' => $outlet->id,
            'products' => $products,
        ]);
    }

    /**
     * GET /api/outlets/{id}/reviews
     */
    public function reviews(Outlet $outlet)
    {
        $reviews = $outlet->reviews()->orderByDesc('created_at')->get();

        return response()->json([
            'success' => true,
            'outlet_id' => $outlet->id,
            'reviews' => $reviews,
        ]);
    }
}
