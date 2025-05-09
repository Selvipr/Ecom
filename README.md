# Tiruvear Textiles - E-commerce App

## Features Implemented

### Product Display and Navigation
- Product List screen showing all products in a grid layout
- Product Detail screen with detailed information
- Category-based browsing
- Navigation between screens (Home → Categories → Products → Product Detail)

### Shopping Cart Functionality
- Add to Cart button works from both Product List and Product Detail screens
- Quantity selection in Product Detail screen
- Cart badge updates with item count
- Cart screen shows all added items
- Ability to change quantities in cart
- Ability to remove items from cart

### Checkout Flow
- Complete buying flow implemented
- Cart totals calculation with subtotal, shipping, and optional discounts
- Coupon code application
- Checkout process with order confirmation
- Order summary and details

### UI Enhancements
- Visual feedback for actions (loading indicators, dialogs)
- Confirmation dialogs for important actions
- Stock quantity display and validation

## How to Use

1. Browse products from the home screen or categories tab
2. Click on a product to view details
3. Adjust quantity and click "Add to Cart" or "Buy Now"
4. In Cart screen, review items and modify quantities if needed
5. Apply coupon codes (try WELCOME10, TIRUVEAR20, or FREESHIP)
6. Click Checkout to complete the purchase
7. View order confirmation with order details

## Technical Implementation Details

The app implements a complete e-commerce buying flow with:
- Kotlin coroutines for async operations
- Repository pattern for data management
- MVVM architecture
- Navigation component for screen navigation
- Material Design UI components

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
4. Set up Supabase backend (see [Supabase Setup](#supabase-setup) below)
5. Build and run the app

## Supabase Setup

This app uses Supabase as its backend service. To set up Supabase:

1. Create a Supabase account at [https://supabase.com](https://supabase.com)
2. Create a new project
3. Navigate to the SQL Editor and run the database setup script in `supabase/database_setup.sql`
4. Update the Supabase URL and API Key in `TiruvearApp.kt`

For detailed Supabase setup instructions, see `supabase/setup_instructions.md`.

### Database Schema

The app uses the following tables in Supabase:

- **profiles**: User profiles linked to auth.users
- **product_categories**: Product categories
- **products**: Product information
- **product_images**: Product images
- **product_variants**: Product variants (size, color, etc.)
- **carts**: Shopping carts
- **cart_items**: Items in shopping carts
- **orders**: Order information
- **order_items**: Items in orders
- **addresses**: User shipping addresses
- **reviews**: Product reviews

### Data Storage

All data is persistently stored in Supabase:

- **User data**: Authentication and profiles table
- **Products**: Products and related tables
- **Cart**: Carts and cart_items tables
- **Orders**: Orders and order_items tables

### Row Level Security (RLS)

Supabase RLS policies are used to secure data:

- Product and category data is readable by everyone
- Cart data is only accessible by the cart owner
- Order data is only accessible by the order owner
- User data is only accessible by the user themselves

## Troubleshooting

### Supabase Connection Issues

If you encounter issues connecting to Supabase:

1. Make sure the Supabase URL and API key in `TiruvearApp.kt` are correct
2. Check that you've run the database setup script
3. Verify your internet connection
4. Check the Supabase dashboard for service status

### Common Errors

- **"Failed to create user"**: Check Supabase authentication settings
- **"Failed to load products"**: Check database setup and RLS policies
- **"Failed to add to cart"**: Check cart table schema and RLS policies

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