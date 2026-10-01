<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Order;
use App\Models\Product;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Str;

class OrderController extends Controller
{
    /**
     * POST /api/orders
     * Guest checkout is allowed. If a valid Sanctum bearer token is sent,
     * the order is linked to that user automatically (for order history).
     */
    public function store(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'outlet_id' => ['required', 'exists:outlets,id'],
            'customer_name' => ['required', 'string', 'max:100'],
            'customer_phone' => ['required', 'string', 'max:25'],
            'customer_email' => ['nullable', 'email', 'max:100'],
            'delivery_type' => ['required', 'in:takeaway,express_delivery,dine_in'],
            'delivery_address' => ['nullable', 'string', 'max:500'],
            'delivery_notes' => ['nullable', 'string', 'max:500'],
            'payment_method' => ['required', 'in:qris,midtrans_gopay,midtrans_bca,cod'],
            'items' => ['required', 'array', 'min:1'],
            'items.*.product_id' => ['required', 'exists:products,id'],
            'items.*.quantity' => ['required', 'integer', 'min:1'],
            'items.*.spice_level' => ['nullable', 'integer', 'min:0', 'max:5'],
            'items.*.notes' => ['nullable', 'string', 'max:255'],
        ], [
            'outlet_id.required' => 'Pilih cabang outlet Dadar Beredar terdekat.',
            'customer_name.required' => 'Nama lengkap wajib diisi.',
            'customer_phone.required' => 'Nomor WhatsApp wajib diisi untuk konfirmasi pesanan.',
            'items.required' => 'Keranjang pesanan masih kosong.',
            'items.min' => 'Pilih minimal satu menu untuk dipesan.',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'message' => 'Validasi gagal',
                'errors' => $validator->errors(),
            ], 422);
        }

        $validated = $validator->validated();
        $user = $request->user('sanctum');

        $order = DB::transaction(function () use ($validated, $user) {
            $subtotal = 0;
            $itemsData = [];

            foreach ($validated['items'] as $item) {
                $product = Product::findOrFail($item['product_id']);
                $itemSubtotal = $product->price * $item['quantity'];
                $subtotal += $itemSubtotal;

                $itemsData[] = [
                    'product_id' => $product->id,
                    'product_name' => $product->name,
                    'price' => $product->price,
                    'quantity' => $item['quantity'],
                    'spice_level' => $item['spice_level'] ?? 0,
                    'notes' => $item['notes'] ?? null,
                    'subtotal' => $itemSubtotal,
                ];
            }

            $deliveryFee = match ($validated['delivery_type']) {
                'express_delivery' => 12000,
                default => 0,
            };

            $taxAmount = round($subtotal * 0.10);
            $totalAmount = $subtotal + $taxAmount + $deliveryFee;

            $orderNumber = 'DAR-'.date('Ymd').'-'.strtoupper(Str::random(5));

            $order = Order::create([
                'order_number' => $orderNumber,
                'user_id' => $user?->id,
                'outlet_id' => $validated['outlet_id'],
                'customer_name' => $validated['customer_name'],
                'customer_phone' => $validated['customer_phone'],
                'customer_email' => $validated['customer_email'] ?? null,
                'delivery_type' => $validated['delivery_type'],
                'delivery_address' => $validated['delivery_address'] ?? null,
                'delivery_notes' => $validated['delivery_notes'] ?? null,
                'subtotal' => $subtotal,
                'delivery_fee' => $deliveryFee,
                'tax_amount' => $taxAmount,
                'total_amount' => $totalAmount,
                'payment_method' => $validated['payment_method'],
                'payment_status' => 'pending',
                'order_status' => 'received',
                'payment_reference' => 'REF-'.strtoupper(Str::random(10)),
                'snap_token' => 'SNAP-'.Str::uuid(),
            ]);

            foreach ($itemsData as $itemData) {
                $order->items()->create($itemData);
            }

            return $order;
        });

        $order->load(['outlet', 'items.product']);

        return response()->json([
            'success' => true,
            'message' => 'Pesanan berhasil dibuat',
            'order' => $order,
        ], 201);
    }

    /**
     * GET /api/orders/{orderNumber}
     */
    public function show(string $orderNumber)
    {
        $order = Order::with(['outlet', 'items.product'])
            ->where('order_number', $orderNumber)
            ->firstOrFail();

        return response()->json([
            'success' => true,
            'order' => $order,
        ]);
    }

    /**
     * GET /api/orders
     * Order history for the authenticated user.
     */
    public function myOrders(Request $request)
    {
        $orders = Order::with(['outlet', 'items'])
            ->where('user_id', $request->user()->id)
            ->orderByDesc('created_at')
            ->get();

        return response()->json([
            'success' => true,
            'orders' => $orders,
        ]);
    }

    /**
     * POST /api/orders/{orderNumber}/simulate-payment
     * Dev/demo helper mirroring the web mock-payment flow (no real gateway).
     */
    public function simulatePayment(Request $request, string $orderNumber)
    {
        $order = Order::where('order_number', $orderNumber)->firstOrFail();
        $status = $request->input('status', 'paid');

        if ($status === 'paid') {
            $order->update([
                'payment_status' => 'paid',
                'order_status' => 'cooking',
            ]);
            $message = 'Pembayaran berhasil! Pesanan masuk antrean penggorengan.';
        } else {
            $order->update(['payment_status' => 'failed']);
            $message = 'Pembayaran ditolak/gagal.';
        }

        $order->load(['outlet', 'items.product']);

        return response()->json([
            'success' => true,
            'message' => $message,
            'order' => $order,
        ]);
    }

    /**
     * POST /api/orders/{orderNumber}/simulate-status
     */
    public function simulateStatus(Request $request, string $orderNumber)
    {
        $validator = Validator::make($request->all(), [
            'order_status' => ['required', 'in:received,cooking,ready,delivered,cancelled'],
        ]);

        if ($validator->fails()) {
            return response()->json([
                'message' => 'Status tidak valid',
                'errors' => $validator->errors(),
            ], 422);
        }

        $order = Order::where('order_number', $orderNumber)->firstOrFail();
        $nextStatus = $request->input('order_status', 'ready');

        $order->update(['order_status' => $nextStatus]);
        $order->load(['outlet', 'items.product']);

        return response()->json([
            'success' => true,
            'order' => $order,
        ]);
    }
}
