# Tiruvear Textiles E-Commerce App

A modern E-Commerce Android application for Tiruvear Textiles with Supabase integration.

## Features

- User authentication (login, register, forgot password)
- Guest browsing
- Product browsing by categories
- Featured products
- New arrivals
- Product search
- Shopping cart
- Order management
- User profile management

## Technical Stack

- **Language**: Kotlin
- **Min SDK**: 26 (Android 8.0 Oreo)
- **Architecture**: MVVM
- **Backend**: Supabase
- **UI**: Material Design Components
- **Image Loading**: Glide
- **Asynchronous Programming**: Coroutines
- **Navigation**: Jetpack Navigation Component

## Project Structure

- **app/src/main/java/com/tiruvear/textiles/**
  - **data/**
    - **models/**: Data classes representing database entities
    - **repositories/**: Repository interfaces and implementations
  - **ui/**
    - **auth/**: Authentication screens (login, register)
    - **main/**: Main app screens (home, categories, cart, orders, profile)
    - **admin/**: Admin dashboard for inventory management
  - **TiruvearApp.kt**: Application class with Supabase initialization

## Setup Instructions

1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle dependencies
4. Build and run the app

## Supabase Integration

The app uses Supabase for backend services:

- **Authentication**: User login, registration, and password reset
- **Database**: Product catalog, user data, carts, orders
- **Storage**: Product images and assets

## Database Schema

- **users**: User profiles
- **products**: Product information
- **product_categories**: Product categories
- **product_images**: Product images
- **product_variants**: Product variants (size, color, etc.)
- **carts**: Shopping carts
- **cart_items**: Items in shopping carts
- **orders**: Order information
- **order_items**: Items in orders
- **addresses**: User shipping addresses
- **reviews**: Product reviews

## License

This project is proprietary and confidential.

## Credits

Developed for Tiruvear Textiles. 