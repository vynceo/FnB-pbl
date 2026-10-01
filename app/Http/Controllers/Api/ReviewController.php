<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Review;
use Illuminate\Http\Request;

class ReviewController extends Controller
{
    /**
     * GET /api/reviews?featured=1
     */
    public function index(Request $request)
    {
        $query = Review::with('outlet')->orderByDesc('created_at');

        if ($request->boolean('featured')) {
            $query->featured();
        }

        return response()->json([
            'success' => true,
            'reviews' => $query->get(),
        ]);
    }
}
