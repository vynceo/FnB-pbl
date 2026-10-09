# Dadar Beredar — Android App

Aplikasi Android untuk franchise **Telur Dadar Krispy, Sambal Rempah Legendaris**
(Franchise by Babe Cabita & King Abdi). Mengonsumsi Laravel API dari backend project ini.

UI mengikuti gaya aplikasi F&B modern seperti **Tomoro Coffee / Kopi Kenangan**:
warm brown tones, hero banner, kategori, bestseller horizontal scroll, bottom navigation.

- **Package**: `com.dadarberedar.app`
- **Versi**: `1.0.0` (versionCode 1)
- **Min SDK**: 26 (Android 8.0) — **Target/Compile SDK**: 35

---

## Daftar Isi

1. [Tech Stack](#tech-stack)
2. [Struktur Project](#struktur-project)
3. [Arsitektur](#arsitektur)
4. [Screen & Navigasi](#screen--navigasi)
5. [Design System](#design-system)
6. [Integrasi API](#integrasi-api)
7. [Autentikasi](#autentikasi)
8. [Alur Keranjang & Checkout](#alur-keranjang--checkout)
9. [Konfigurasi](#konfigurasi)
10. [Cara Build & Menjalankan](#cara-build--menjalankan)
11. [Catatan Penting](#catatan-penting)

---

## Tech Stack

| Teknologi | Versi | Fungsi |
|---|---|---|
| Kotlin | 2.0.21 | Bahasa pemrograman |
| Jetpack Compose | BOM 2024.12.01 | Declarative UI |
| Material 3 | via Compose BOM | Komponen desain |
| Navigation Compose | 2.8.5 | Navigasi antar screen |
| Hilt | 2.52 (KSP 2.0.21-1.0.28) | Dependency Injection |
| Retrofit + Gson | 2.11.0 | HTTP client + JSON parsing |
| OkHttp Logging | 4.12.0 | Logging request/response |
| Coil Compose | 2.7.0 | Load gambar menu |
| DataStore Preferences | 1.1.1 | Simpan token & data user |
| AGP | 8.7.3 | Android Gradle Plugin |
| Gradle | 9.3.1 (wrapper) | Build system |
| JVM Target | 17 | Compatibility |

---

## Struktur Project

```
android/
├── build.gradle.kts              # Root build (versi plugin)
├── settings.gradle.kts           # rootProject "Dadar Beredar"
├── gradle.properties
├── gradle/wrapper/
├── app/
│   ├── build.gradle.kts          # Config aplikasi + dependencies
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml   # INTERNET permission, cleartext traffic
│       ├── res/                  # strings, themes, launcher icons
│       └── java/com/dadarberedar/app/
│           ├── DadarBeredarApp.kt        # @HiltAndroidApp Application
│           ├── MainActivity.kt           # Entry point, sets Compose content
│           ├── AppConfig.kt              # BASE_URL konstanta
│           ├── data/
│           │   ├── model/                # DTO + domain model (Gson)
│           │   │   ├── User.kt           # User, AuthResponse, Login/RegisterRequest
│           │   │   ├── Product.kt        # Category, Product + resolvedImageUrl ext
│           │   │   ├── Outlet.kt         # Outlet + delivery platform URLs
│           │   │   ├── Order.kt          # Order, OrderItem, CheckoutRequest, Review
│           │   │   └── ApiResponse.kt    # Wrapper response per endpoint
│           │   └── remote/
│           │       ├── ApiService.kt     # Retrofit interface (semua endpoint)
│           │       └── AuthManager.kt    # Login/register/logout + token DataStore
│           ├── di/
│           │   └── NetworkModule.kt      # Hilt module: DataStore, OkHttp, Retrofit
│           ├── navigation/
│           │   └── NavGraph.kt           # MainApp + NavHost + Screen routes
│           └── ui/
│               ├── splash/SplashScreen.kt
│               ├── components/BottomNavBar.kt
│               ├── home/        HomeScreen.kt, HomeViewModel.kt
│               ├── menu/        MenuScreen.kt, MenuViewModel.kt
│               ├── cart/        CartScreen.kt, CartViewModel.kt, CheckoutScreen.kt
│               ├── orders/      MyOrdersScreen.kt, MyOrdersViewModel.kt
│               ├── outlet/      OutletDetailScreen.kt, OutletDetailViewModel.kt
│               ├── profile/     ProfileScreen.kt (berisi ProfileViewModel)
│               ├── search/      SearchScreen.kt (berisi SearchViewModel)
│               ├── tracking/    OrderTrackingScreen.kt, OrderTrackingViewModel.kt
│               ├── auth/        AuthScreen.kt (Login + Register + AuthViewModel)
│               └── theme/       Color.kt, Theme.kt, Type.kt
```

---

## Arsitektur

Pola **MVVM** dengan Hilt-injected ViewModel:

```
UI (Compose Screen)
   ↓ observes StateFlow
ViewModel (@HiltViewModel)
   ↓ calls suspend functions
ApiService (Retrofit)  /  AuthManager (DataStore)
   ↓
Laravel API (Sanctum)
```

- **State**: `StateFlow` per ViewModel, di-observe via `collectAsState()`
- **Dependency Injection**: Hilt, semua ViewModel `@HiltViewModel`
- **DataStore**: token Sanctum + nama/email user
- **Model API**: Gson deserialize ke data class Kotlin
- **Kartu keranjang**: state in-memory di `CartViewModel` (satu instance per NavBackStack —
  keranjang hidup selama sesi, hilang saat app close)

---

## Screen & Navigasi

### Route (`Screen` sealed class di `NavGraph.kt`)

| Route | Screen | Bottom Nav |
|---|---|---|
| `splash` | SplashScreen (animated logo) | — |
| `home` | HomeScreen (hero, promo, kategori, bestseller, outlet) | ✅ Beranda |
| `menu` | MenuScreen (full menu + filter kategori) | ✅ Menu |
| `cart` | CartScreen (item, qty, subtotal/pajak/total) | ✅ Keranjang |
| `my_orders` | MyOrdersScreen (riwayat pesanan) | ✅ Pesanan |
| `profile` | ProfileScreen (akun, pengaturan, logout) | ✅ Profil |
| `outlet/{outletId}` | OutletDetailScreen (info, menu, ulasan, link delivery) | — |
| `checkout` | CheckoutScreen (jenis pesanan, data diri, pembayaran) | — |
| `tracking/{orderNumber}` | OrderTrackingScreen (status + simulasi demo) | — |
| `login` / `register` | AuthScreen | — |
| `search` | SearchScreen (debounce 500ms) | — |

### Alur navigasi

```
Splash → Home
Home ⇄ (bottom nav) Menu / Cart / Orders / Profile
Home → Search → OutletDetail → Cart → Checkout → Tracking
Profile → Login/Register → back ke Home
OutletDetail → Cart
Orders → Tracking
```

- Bottom nav memakai `popUpTo(Home) { saveState } + restoreState` sehingga state tiap tab
  dipertahankan (standard Material bottom-nav behavior).
- Transisi antar screen: fade 300ms.
- `BottomNavBar` menampilkan **badge counter** jumlah item keranjang (warna gold).

---

## Design System

### Warna (gaya Tomoro/Kopi Kenangan — warm brown)

| Token | Hex | Pemakaian |
|---|---|---|
| `PrimaryBrown` | `#5D4037` | Warna utama (top bar, tombol, aksen) |
| `PrimaryBrownDark` | `#3E2723` | Splash background, hero gradient |
| `PrimaryBrownLight` | `#8D6E63` | Ikon sekunder, dark-scheme primary |
| `AccentGold` | `#FFB300` | Badge, bestseller, aksen emas |
| `AccentOrange` | `#FF8F00` | Promo, ikon GoFood |
| `BackgroundCream` | `#FAF7F2` | Background aplikasi (light) |
| `SurfaceWhite` | `#FFFFFF` | Card, surface |
| `TextPrimary` | `#3E2723` | Teks utama |
| `TextSecondary` | `#8D6E63` | Teks sekunder |
| `TextTertiary` | `#BCAAA4` | Teks placeholder/tertiary |
| `SuccessGreen` | `#43A047` | Status delivered/outlet buka |
| `ErrorRed` | `#E53935` | Error, status cancelled, logout |

Dark theme tersedia (`Theme.kt`): background `#1C1410`, surface `#2A1F18`.

### Tipografi

`Typography` lengkap dari `displayLarge` (32sp Bold) hingga `labelSmall` (10sp Medium),
FontFamily default, letter-spacing negatif di display untuk kesan premium.

### Komponen khas

- **Hero banner**: gradient `PrimaryBrownDark → PrimaryBrown`, badge "FRANCHISE BY..."
- **Product card**: image + badge (Bestseller/Signature/Krispy) + level pedas 🌶 + tombol
  tambah bulat
- **Kategori**: icon chip 56dp rounded-16, warna brown tint
- **Status order**: badge berwarna (Diterima=Brown, Dimasak=Gold, Siap/Selesai=Green,
  Dibatalkan=Red)

---

## Integrasi API

### Endpoint yang dipakai (`ApiService.kt`)

| Method | Path | Response wrapper | Model |
|---|---|---|---|
| POST | `api/register` | `{message, token, user}` | `AuthResponse` |
| POST | `api/login` | `{message, token, user}` | `AuthResponse` |
| POST | `api/logout` | `{message}` | — |
| GET | `api/user` | `User` object | `User` |
| GET | `api/categories` | `{success, categories[]}` | `CategoriesResponse` |
| GET | `api/outlets` | `{success, outlets[]}` | `OutletsResponse` |
| GET | `api/outlets/{id}` | `{success, outlet}` | `OutletResponse` |
| GET | `api/outlets/{id}/products` | `{success, outlet_id, products[]}` | `OutletProductsResponse` |
| GET | `api/outlets/{id}/reviews` | `{success, outlet_id, reviews[]}` | `OutletReviewsResponse` |
| GET | `api/products` | `{success, products[]}` | `ProductsResponse` |
| GET | `api/reviews` | `{success, reviews[]}` | `ReviewsResponse` |
| GET | `api/search?q=` | `{success, products[], outlets[]}` | `SearchResponse` |
| POST | `api/orders` | `{success, message, order}` | `OrderCreateResponse` |
| GET | `api/orders/{orderNumber}` | `{success, order}` | `OrderTrackingResponse` |
| POST | `api/orders/{orderNumber}/simulate-payment` | `{success, message, order}` | `OrderActionResponse` |
| POST | `api/orders/{orderNumber}/simulate-status` | `{success, order}` | `OrderActionResponse` |
| GET | `api/orders` (auth) | `{success, orders[]}` | `OrdersResponse` |

### Format wrapper (penting)

Backend Laravel mengembalikan **wrapper object**, bukan array langsung:

```json
{ "success": true, "products": [ ... ] }
```

Semua endpoint yang mengembalikan list/detail harus di-parse via class wrapper di
`data/model/ApiResponse.kt`, **bukan** `Response<List<Product>>` — jika salah, Gson gagal
parse dan data tampil kosong.

Header default: `Accept: application/json` (interceptor di `NetworkModule`).

### Gambar produk

API mengembalikan `image_url` relatif (mis. `/images/menu/slug.jpg`). Aplikasi menyelesaikan
ke URL absolut via extension property:

```kotlin
val Product.resolvedImageUrl: String?
```

Relatif di-prefix dengan `AppConfig.BASE_URL`. Dipakai di HomeScreen & MenuScreen.

---

## Autentikasi

- **Backend**: Laravel Sanctum bearer token.
- **Penyimpanan**: DataStore Preferences (`dadar_beredar`) — key `auth_token`, `user_name`,
  `user_email`.
- **Login/Register** (`AuthScreen.kt`): sukses → token & user disimpan → kembali ke Home.
- **Checkout guest** didukung (tanpa token order tetap dibuat).
- **Order history** (`MyOrdersScreen`) membutuhkan login; request memakai
  `Authorization: Bearer <token>`.
- **Logout** (`ProfileScreen`): revoke token via API + clear DataStore.

---

## Alur Keranjang & Checkout

1. **Tambah ke keranjang** dari Home (bestseller), Menu, atau OutletDetail →
   `CartViewModel.addToCart(product, outletId)`.
2. Keranjang **per-outlet**: ganti outlet → keranjang lama dikosongkan.
3. **CartScreen**: qty +/-, hitung subtotal, pajak 10%, total.
4. **CheckoutScreen**:
   - Jenis pesanan: `takeaway` / `dine_in` / `express_delivery` (ongkir Rp 12.000)
   - Data pemesan: nama*, telepon*, email, alamat* (untuk non-dine_in), catatan
   - Pembayaran: QRIS / GoPay / Transfer BCA / COD
   - Submit → POST `api/orders`
5. **OrderTrackingScreen**: status order + pembayaran, daftar item, tombol simulasi demo
   (`simulate-payment`, `simulate-status` dengan `order_status`:
   cooking/ready/delivered).

Nomor order format: `DAR-YYYYMMDD-XXXXX`.

---

## Konfigurasi

### Base URL (`AppConfig.kt`)

```kotlin
object AppConfig {
    const val BASE_URL = "http://10.0.2.2:8000/"
}
```

| Environment | BASE_URL |
|---|---|
| Android Emulator | `http://10.0.2.2:8000/` (localhost mesin host) |
| Device fisik (satu jaringan WiFi) | `http://<IP-komputer>:8000/` |

`NetworkModule.kt` meng-`import` konstanta ini — ubah cukup di satu tempat.

### Cleartext HTTP

`AndroidManifest.xml` menyetel `android:usesCleartextTraffic="true"` karena backend dev
memakai HTTP (bukan HTTPS). Untuk production, wajib HTTPS lalu hapus flag ini.

### Permissions

- `INTERNET`
- `ACCESS_NETWORK_STATE`

---

## Cara Build & Menjalankan

### Prasyarat

- Android Studio (Ladybug / AGP 8.7.x) atau JDK 17 + Android SDK 35
- Backend Laravel jalan: `php artisan serve` (port 8000)

### Build

```bash
cd android
./gradlew assembleDebug        # Windows: gradlew.bat assembleDebug
```

APK hasil: `app/build/outputs/apk/debug/app-debug.apk`

### Jalankan

Buka project `android/` di Android Studio → Run pada emulator/device.

**Checklist sebelum run:**

1. Backend jalan: `php artisan serve` di root project Laravel
2. Database terisi: `php artisan migrate --seed` (9 outlet, 9 kategori, 50+ produk)
3. Untuk device fisik: ganti `BASE_URL` ke IP komputer, pastikan satu jaringan
4. Cleartext HTTP sudah aktif (dev)

### Debug logging

OkHttp logging interceptor level `BODY` — lihat log Android Studio (tag `OkHttp`)
untuk melihat request/response JSON.

---

## Catatan Penting

### Simulasi order (demo)

Backend menyediakan endpoint mock (tanpa payment gateway nyata):

- `POST /api/orders/{orderNumber}/simulate-payment` — ubah `payment_status=paid`,
  `order_status=cooking`
- `POST /api/orders/{orderNumber}/simulate-status` — body `{"order_status": "cooking|ready|delivered|cancelled"}`

Tombolnya ada di OrderTrackingScreen.

### Keputusan desain saat ini

- Keranjang in-memory (per sesi) — belum persist ke backend sebelum checkout.
- Search: debounce 500ms, minimal 2 karakter, hasil produk + outlet.
- Order history hanya untuk user login (guest tetap bisa checkout + tracking via
  order number).
- Icon launcher default (adaptive icon XML), belum ada aset gambar custom.

### Troubleshooting

| Gejala | Penyebab | Solusi |
|---|---|---|
| Menu/outlet kosong semua | Response API wrapper belum di-parse | Pastikan ViewModel pakai `body()?.products` / `body()?.outlets` (lihat `ApiResponse.kt`) |
| Gambar tidak muncul | `image_url` relatif, Coil butuh absolut | Pakai `product.resolvedImageUrl` |
| Data kosong + log `ConnectException` | Base URL salah / backend mati | Cek `AppConfig.BASE_URL`, jalankan `php artisan serve` |
| Device fisik gagal koneksi | `10.0.2.2` hanya untuk emulator | Ganti ke IP LAN komputer |
| Order history kosong / 401 | Belum login atau token expired | Login ulang via Profile |
| Simulasi status 422 | Salah key body | Kirim `order_status` (bukan `status`) |
