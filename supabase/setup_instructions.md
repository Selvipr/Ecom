# Supabase Setup Instructions for Tiruvear Textiles App

## Introduction
This document provides instructions for setting up the Supabase backend for the Tiruvear Textiles e-commerce app. Supabase is used for authentication, database, and storage functionality.

## Prerequisites
1. A Supabase account (create one at https://supabase.com if you don't have one)
2. A new Supabase project created for this app

## Configuration Steps

### Step 1: Get your Supabase URL and API Key
1. Go to your Supabase project dashboard
2. Navigate to Settings > API
3. Copy the "Project URL" and "anon/public" API key
4. Update these values in the `TiruvearApp.kt` file:
   ```kotlin
   const val SUPABASE_URL = "your_project_url"
   const val SUPABASE_KEY = "your_anon_key"
   ```

### Step 2: Set up the Database
1. Navigate to the SQL Editor in your Supabase dashboard
2. Copy and paste the contents of the `database_setup.sql` file from the `supabase` directory
3. Run the SQL script to create all the necessary tables and policies

### Step 3: Enable Authentication
1. Go to Authentication > Settings
2. Under "Email Auth", make sure it's enabled
3. You may want to disable email confirmations for testing purposes
4. Customize the email templates if needed for your branding

### Step 4: Set up Storage Buckets
1. Go to Storage in your Supabase dashboard
2. Create the following buckets:
   - `product-images` - For storing product images
   - `category-images` - For storing category images
   - `user-avatars` - For storing user profile pictures

3. Set up public access for the product images bucket:
   - Go to the product-images bucket
   - Navigate to "Policies"
   - Add a policy for anonymous access:
     - Policy name: "Public access to product images"
     - Allowed operations: SELECT
     - Policy definition: `true`

### Step 5: Test the Connection
1. Run the app and try to sign up/login to verify authentication works
2. Browse products to verify database connection works
3. Upload a product image to verify storage works

## Troubleshooting

### Authentication Issues
- If users can't register, check that email confirmations are disabled for testing
- Verify the SUPABASE_URL and SUPABASE_KEY are correct

### Database Connection Issues
- Make sure the SQL script executed without errors
- Check if the RLS policies are set up correctly
- Verify that your app has the correct permissions

### Storage Issues
- Ensure the buckets are created with the correct names
- Check that the storage policies are set up correctly for each bucket

## Additional Information

### Database Schema
The database includes the following main tables:
- `profiles` - User profiles
- `products` - Product information  
- `product_categories` - Categories for products
- `product_images` - Images for products
- `carts` - Shopping carts
- `cart_items` - Items in shopping carts
- `orders` - Order information
- `order_items` - Items in orders

### Row Level Security (RLS)
Supabase uses Row Level Security to control access to data. The setup script configures policies that:
- Allow public read access to products and categories
- Restrict cart and order data to only the user who owns them
- Allow admins to access all data

### Production Considerations
For a production environment, consider:
- Enabling email confirmations for security
- Setting up more restrictive RLS policies
- Configuring CORS settings
- Setting up a custom domain
- Enabling multi-factor authentication 