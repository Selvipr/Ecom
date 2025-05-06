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

### Database Setup

Before using the app, you need to set up the database tables in Supabase:

1. Log in to your Supabase dashboard
2. Go to the SQL Editor
3. Copy and paste the contents of the `users_table.sql` file
4. Run the SQL script to create the necessary tables and policies

### Troubleshooting Registration Issues

If you encounter "Failed to create user" errors during registration, check the following:

1. Make sure the Supabase URL and API key in `TiruvearApp.kt` are correct
2. Verify that the users table exists in your Supabase database
3. Check that RLS (Row Level Security) policies are properly set up
4. Ensure your app has a network connection to Supabase

### Common Supabase Issues

1. **Rate limiting**: Supabase has built-in rate limiting. Wait at least 1 minute between registration attempts.
2. **User table not found**: Make sure you've run the SQL script to create the users table.
3. **Permission errors**: Ensure the RLS policies are correctly set up.

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

## App Architecture

The app follows an MVVM architecture:

- **Models**: Data classes representing entities like User, Product, etc.
- **Repositories**: Handle data operations and communicate with Supabase
- **ViewModels**: Manage UI-related data and communicate with repositories
- **Views**: UI components that display data to the user 