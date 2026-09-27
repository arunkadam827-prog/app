# 🌾 Kisan Connect - Farmer Marketplace & Agri-Commerce System

[![Android](https://img.shields.io/badge/Platform-Android-green.svg?logo=android)](https://developer.android.com)
[![Java](https://img.shields.io/badge/Java-21-orange.svg?logo=openjdk)](https://openjdk.org)
[![Spring Boot](https://img.shields.io/badge/Backend-Spring%20Boot%203-brightgreen.svg?logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/Database-PostgreSQL-blue.svg?logo=postgresql)](https://www.postgresql.org)
[![License](https://img.shields.io/badge/License-MIT-lightgrey.svg)](LICENSE)

A full-stack agricultural marketplace platform connecting farmers directly with buyers and local consumers. The system consists of a native **Android application** and a robust **Spring Boot REST backend** backed by **PostgreSQL**.

---

## 📱 Features

### 🛒 Buyer Marketplace
- **Produce Discovery**: Explore farm-fresh items across categories like Vegetables 🥬, Fruits 🍎, Grains 🌾, and Dairy 🥛.
- **Search & Sort**: Live real-time search with multi-mode sorting (Price: Low to High, High to Low, Relevance).
- **Cart Management**: Add items to cart with quantity controls and real-time total computation.

### 💳 Realistic Multi-Method Checkout & Payments
- **UPI (GPay / PhonePe / Paytm / BHIM)**:
  - Company UPI ID: `9356601104@upi` with 1-tap copy.
  - Quick UPI number: `9356601104`.
  - Custom buyer UPI ID input.
- **Debit / Credit Cards**: 16-digit card input, cardholder name, expiry (MM/YY), and CVV.
- **Direct Company Bank Account Transfer**:
  - **Beneficiary**: Kisan Connect Agro Services Pvt. Ltd.
  - **Account Number**: `9356601104`
  - **IFSC**: `HDFC0001234` (*HDFC Bank, Commercial Agri Branch*)
  - 1-tap copy button for all beneficiary details.
  - 12-digit UTR / Payment reference number submission.
- **Cash on Delivery (COD)**: Option to pay cash directly to delivery personnel.
- **Order Confirmation**: Order popup with unique Order ID and instant tracking.

### 👨‍🌾 Farmer Dual Role (Selling & Buying)
- **Product Listing**: Upload fresh produce with product title, description, category, price, quantity, and image URL.
- **Farmer Dashboard**: Real-time sales metrics, revenue stats, product inventory, and incoming order updates.
- **Dual Marketplace Access**: Farmers can both sell their harvests and purchase farm supplies or produce from other farmers using their single existing login account.

### 📦 Order Tracking & Interactive Filters
- **Live Summary Cards**: Real-time count badges for **All**, **Pending**, **Delivered**, and **Cancelled** orders.
- **Status Filtering**: Tap any summary card to filter orders by status on the fly.
- **Delivery Details**: Full tracking info with delivery addresses and selected payment methods.

### 👤 Profile & Session Management
- Live profile synchronization with PostgreSQL database.
- Dynamic statistics displaying total orders placed and active products.
- Frictionless login without role lockout — user credentials automatically authenticate and route to the correct role dashboard.

---

## 🏗️ Project Architecture

```
farmer/
├── app/                               # Native Android Client
│   ├── src/main/java/com/example/farmer/
│   │   ├── activities/                # Login, Register, Checkout, Dashboard, Products
│   │   ├── adapters/                  # CartAdapter, OrderAdapter, ProductAdapter
│   │   ├── fragments/                 # HomeFragment, OrdersFragment, ProfileFragment, CartFragment
│   │   ├── models/                    # Product, Order, User, CartItem
│   │   ├── services/                  # ApiService, RetrofitClient
│   │   └── utils/                     # SessionManager
│   └── src/main/res/                  # Layouts, Drawables, Styles, Colors
│
└── backend/                           # Spring Boot REST API
    ├── src/main/java/com/example/farmerbackend/
    │   ├── config/                    # CORS, Security configs
    │   ├── controller/                # ProductController, OrderController, UserController, FarmerController, CartController
    │   ├── dto/                       # Requests and responses (Cart, Order, Login, Product)
    │   ├── entity/                    # User, Product, CustomerOrder, CartItem, OrderItem
    │   ├── repository/                # Spring Data JPA Repositories
    │   └── service/                   # Business logic implementations
    └── src/main/resources/
        └── application.properties     # PostgreSQL & server configs
```

---

## 🔌 API Endpoints Summary

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/users/login` | Authenticate user (Farmer/Buyer) |
| `POST` | `/api/users/register` | Register new user account |
| `GET` | `/api/users/{userId}` | Get user profile details |
| `GET` | `/api/products` | Retrieve all marketplace products |
| `POST` | `/api/products/add` | Add new farm product (Farmer) |
| `GET` | `/api/products/category/{category}` | Filter products by category |
| `GET` | `/api/cart/{userId}` | Fetch user shopping cart |
| `POST` | `/api/cart/add` | Add product to cart |
| `DELETE` | `/api/cart/{cartItemId}` | Remove product from cart |
| `POST` | `/api/orders/create` | Place new order with payment details |
| `GET` | `/api/orders/user/{userId}` | Fetch orders for a specific user |
| `GET` | `/api/farmers/{farmerId}/stats` | Get seller metrics (sales, revenue, orders) |

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio** (Koala / Ladybug or newer)
- **JDK 21**
- **PostgreSQL 14+**

### 1. Database Setup
Create a PostgreSQL database:
```sql
CREATE DATABASE farmer_db;
```
Ensure your `backend/src/main/resources/application.properties` credentials match:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/farmer_db
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
```

### 2. Run the Backend
```powershell
cd backend
.\gradlew.bat bootRun
```
The API server starts on `http://localhost:8080`.

### 3. Build & Run the Android App
Open the root project in Android Studio, or build via command line:
```powershell
.\gradlew.bat :app:assembleDebug
```
The debug APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 💳 Company Payment Details
- **Beneficiary**: Kisan Connect Agro Services Pvt. Ltd.
- **Account Number**: `9356601104`
- **UPI ID**: `9356601104@oksbi`
- **Phone / GPay / PhonePe**: `9356601104`
- **IFSC**: `HDFC0001234`
- **Bank**: HDFC Bank, Commercial Agri Branch

---

## 📄 License
This project is open-source and available under the [MIT License](LICENSE).
