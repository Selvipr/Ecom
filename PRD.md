# Product Requirements Document (PRD)
# Tiruvear Textiles E-Commerce Android Application

## Document Information
- **Document Version:** 1.0
- **Date:** May 3, 2025
- **Status:** Draft

## Executive Summary
The Tiruvear Textiles E-Commerce Android Application will provide a digital platform for a local textile company based in Tiruppur to showcase and sell their textile products directly to consumers. The application will include both customer-facing features for browsing and purchasing products, as well as admin features for managing product listings, inventory, and orders. The application will use Supabase as its backend database solution.

## Business Objectives
1. Establish a digital presence for Tiruvear, a local textile company with no previous online footprint
2. Create a direct-to-consumer sales channel to increase revenue and customer reach
3. Build brand awareness and customer loyalty through a dedicated mobile application
4. Provide detailed analytics on product performance and customer behavior
5. Streamline inventory and order management processes

## Target Audience
1. **Primary:** Retail customers interested in local textile products from Tiruppur
2. **Secondary:** Wholesale buyers looking to purchase Tiruvear products in bulk
3. **Internal:** Company administrators managing product listings and orders

## User Personas

### Customer Persona: Retail Buyer
- **Name:** Priya
- **Age:** 25-45
- **Behavior:** Shops online for quality textiles, values local craftsmanship, price-conscious
- **Goals:** Find authentic local textile products, compare prices, make secure purchases
- **Pain Points:** Difficulty finding authentic local products online, concerns about product quality when purchasing online

### Admin Persona: Store Manager
- **Name:** Rajesh
- **Age:** 30-50
- **Behavior:** Manages store inventory, updates product listings, processes orders
- **Goals:** Easily update product information, track inventory, manage orders efficiently
- **Pain Points:** Limited technical knowledge, needs a simple interface to manage digital store

## Product Features

### Customer Side Features

#### 1. User Authentication & Management
- **User Registration & Login**
  - Registration with email, phone number, and password
  - Login with credentials or OTP verification
  - Password reset functionality
  - Guest browsing option
- **User Profile Management**
  - View and edit personal information
  - View order history and status
  - Save favorite products
  - Manage delivery addresses

#### 2. Product Discovery
- **Browse Products**
  - Browse by categories (clothing, home textiles, etc.)
  - Featured products section on home screen
  - New arrivals section
  - Trending/popular products
- **Search Functionality**
  - Text-based search with autocomplete
  - Filter products by price, category, color, etc.
  - Sort products by relevance, price, newest, etc.

#### 3. Product Details
- **Product Information**
  - High-quality product images with zoom functionality
  - Detailed product descriptions
  - Product specifications (material, dimensions, etc.)
  - Price information
  - Availability status
  - Color/size variants if applicable
- **Customer Reviews & Ratings**
  - View product ratings and reviews
  - Submit ratings and reviews for purchased products
  - View aggregate rating statistics

#### 4. Shopping Cart & Checkout
- **Cart Management**
  - Add products to cart
  - View cart contents
  - Update quantities or remove items
  - Save cart for later
- **Checkout Process**
  - Multiple payment options (UPI, cards, COD)
  - Address selection or addition
  - Order summary review
  - Promo code/discount application
  - Order confirmation with details

#### 5. Order Management
- **Order Tracking**
  - View order status and estimated delivery date
  - Track shipment in real-time (if applicable)
  - Receive push notifications for order updates
- **Order History**
  - View past orders with details
  - Reorder functionality
  - Download/share invoice

#### 6. Customer Support
- **Help Center**
  - FAQ section
  - Return and refund policy
  - Contact information
- **Support Requests**
  - Chat with support
  - Submit support tickets
  - Call support directly

### Admin Side Features

#### 1. Admin Authentication
- **Secure Login**
  - Admin credentials (username/password)
  - Two-factor authentication option
  - Session management
  - Password reset functionality

#### 2. Dashboard
- **Overview**
  - Sales summary and trends
  - Order status distribution
  - Low stock alerts
  - Recent customer reviews
  - Daily/weekly/monthly reports

#### 3. Product Management
- **Product Catalog**
  - Add new products with details
  - Upload multiple product images
  - Edit existing product information
  - Set pricing and discounts
  - Manage product categories and tags
  - Enable/disable product listings
- **Inventory Management**
  - Update stock levels
  - Set low stock thresholds
  - View inventory reports
  - Manage product variants (sizes, colors)

#### 4. Order Management
- **Order Processing**
  - View and filter orders
  - Update order status
  - Generate invoices
  - Process refunds
  - Cancel orders
- **Shipping Management**
  - Generate shipping labels
  - Update tracking information
  - Manage shipping providers

#### 5. Customer Management
- **Customer Database**
  - View customer profiles
  - Check purchase history
  - Filter customers by various parameters
  - Export customer data

#### 6. Promotions & Marketing
- **Discount Management**
  - Create and manage promo codes
  - Set up sales and discounts
  - Schedule promotional activities
- **Push Notifications**
  - Send promotional notifications
  - Announce new products/sales
  - Target specific customer segments

#### 7. Analytics & Reporting
- **Sales Reports**
  - Daily/weekly/monthly sales reports
  - Product performance metrics
  - Revenue analytics
- **User Behavior**
  - Most viewed products
  - Cart abandonment analysis
  - User engagement metrics

## Technical Requirements

### Platform & Compatibility
- **Platform:** Native Android application
- **Android Version Support:** Android 8.0 (Oreo) and above
- **Screen Sizes:** Support for various smartphone and tablet screen sizes
- **Orientation:** Primary portrait mode with responsive design

### Database & Backend
- **Database:** Supabase (PostgreSQL)
- **Storage:** Supabase Storage for product images and assets
- **Authentication:** Supabase Auth for user and admin authentication
- **API:** RESTful APIs using Supabase functions

### Performance Requirements
- **Load Time:** App should load within 3 seconds on average networks
- **Response Time:** Actions should respond within 1 second
- **Offline Support:** Basic browsing capability in offline mode
- **Battery Usage:** Optimize for minimal battery consumption

### Security Requirements
- **Data Encryption:** All sensitive data must be encrypted in transit and at rest
- **Authentication:** Secure login with session management
- **Authorization:** Role-based access control for admin features
- **Payment Security:** PCI DSS compliance for payment processing
- **Data Privacy:** Compliance with data protection regulations

### Integrations
- **Payment Gateways:** Integration with popular payment services (Razorpay, PayTM, UPI)
- **Shipping Providers:** Integration with local shipping services
- **Analytics:** Firebase Analytics for user behavior tracking
- **Notification Service:** Firebase Cloud Messaging for push notifications

## User Flows

### Customer User Flow

1. **App Install & Onboarding**
   - Download and install app
   - View onboarding screens highlighting key features
   - Register or continue as guest

2. **Product Discovery & Selection**
   - Browse categories or search for products
   - View product listings
   - Filter and sort results
   - Select product to view details
   - Add product to cart or wishlist

3. **Purchase Flow**
   - Review cart contents
   - Proceed to checkout
   - Select/add delivery address
   - Choose payment method
   - Apply promo code (if available)
   - Complete payment
   - Receive order confirmation

4. **Post-Purchase**
   - Track order status
   - Receive delivery notification
   - Leave product review
   - Contact support (if needed)

### Admin User Flow

1. **Admin Login**
   - Access admin login screen
   - Enter credentials
   - Complete 2FA (if enabled)

2. **Product Management**
   - Access product management section
   - Add new product with details and images
   - Set pricing and inventory
   - Publish product to store

3. **Order Processing**
   - View incoming orders
   - Update order status
   - Generate shipping information
   - Mark order as complete

4. **Inventory Management**
   - Check low stock alerts
   - Update inventory levels
   - Generate inventory reports

## UI/UX Requirements

### Design Guidelines
- **Brand Identity:** Incorporate Tiruvear's brand colors and identity
- **Design System:** Material Design principles
- **Accessibility:** Support for screen readers and accessibility features
- **Localization:** Support for multiple languages (primarily Tamil and English)

### Key Screens (Customer)
1. **Splash & Login Screen**
   - App logo and splash animation
   - Login/Register options
   - Guest browsing option

2. **Home Screen**
   - Featured products carousel
   - Category navigation
   - Promotional banners
   - New arrivals section
   - Search bar

3. **Product Listing Screen**
   - Grid/list view of products
   - Filter and sort options
   - Quick add to cart
   - Wishlist toggle

4. **Product Detail Screen**
   - Product image gallery
   - Product description and specifications
   - Price and availability
   - Size/variant selection
   - Add to cart button
   - Related products

5. **Cart & Checkout Screens**
   - Cart items with images and price
   - Quantity adjustment
   - Price summary
   - Checkout button
   - Address selection
   - Payment method selection

6. **Order Tracking Screen**
   - Order summary
   - Status timeline
   - Delivery estimate
   - Shipping information

### Key Screens (Admin)
1. **Admin Login Screen**
   - Secure login form
   - Password reset option

2. **Admin Dashboard**
   - Key metrics and charts
   - Recent orders
   - Low stock alerts
   - Quick action buttons

3. **Product Management Screen**
   - Product listing with search and filters
   - Add/edit product form
   - Image upload interface

4. **Order Management Screen**
   - Order list with status
   - Order detail view
   - Status update controls
   - Invoice generation

5. **Inventory Management Screen**
   - Stock level indicators
   - Adjustment controls
   - Alert threshold settings

## Implementation Plan

### Phase 1: MVP (8 weeks)
- Basic customer app with core shopping features
- Essential admin functionality for product management
- Integration with Supabase for database and authentication
- Basic payment integration

### Phase 2: Enhanced Features (6 weeks)
- Advanced search and filtering
- Reviews and ratings system
- Improved admin dashboard with analytics
- Multiple payment options
- Push notifications

### Phase 3: Advanced Features (4 weeks)
- Personalized recommendations
- Loyalty program
- Advanced reporting for admins
- Marketing tools and campaign management
- Performance optimizations

## Testing Requirements
- **Unit Testing:** For all critical functionality
- **Integration Testing:** For all system integrations
- **User Acceptance Testing (UAT):** With actual admin users and potential customers
- **Performance Testing:** Load testing for concurrent users
- **Security Testing:** Vulnerability assessment and penetration testing

## Success Metrics
- Monthly Active Users (MAU)
- Conversion rate (browsing to purchase)
- Average order value
- Customer retention rate
- App store rating
- Administrative time savings
- Inventory accuracy

## Assumptions & Dependencies
- Reliable internet connectivity for users
- Availability of product information and images from Tiruvear
- Integration capabilities with selected payment gateways
- Supabase service availability and performance

## Risks & Mitigations
| Risk | Probability | Impact | Mitigation |
|------|------------|--------|------------|
| Low user adoption | Medium | High | Promotional campaign, user-friendly design |
| Payment gateway issues | Low | High | Multiple payment options, fallback mechanisms |
| Product data quality | Medium | Medium | Data validation, admin training |
| Server performance | Low | High | Proper Supabase configuration, caching strategies |
| Security breaches | Low | High | Regular security audits, encryption, secure coding |

## Appendix

### Supabase Database Schema

#### Tables
1. **users**
   - id (PK)
   - email
   - phone
   - password_hash
   - first_name
   - last_name
   - created_at
   - updated_at

2. **addresses**
   - id (PK)
   - user_id (FK)
   - address_line1
   - address_line2
   - city
   - state
   - pincode
   - is_default
   - created_at
   - updated_at

3. **product_categories**
   - id (PK)
   - name
   - description
   - image_url
   - parent_category_id (FK, self-referential)
   - created_at
   - updated_at

4. **products**
   - id (PK)
   - name
   - description
   - base_price
   - sale_price
   - category_id (FK)
   - stock_quantity
   - is_active
   - created_at
   - updated_at

5. **product_images**
   - id (PK)
   - product_id (FK)
   - image_url
   - is_primary
   - display_order
   - created_at

6. **product_variants**
   - id (PK)
   - product_id (FK)
   - variant_type (e.g., color, size)
   - variant_value
   - price_adjustment
   - stock_quantity
   - created_at
   - updated_at

7. **carts**
   - id (PK)
   - user_id (FK)
   - created_at
   - updated_at

8. **cart_items**
   - id (PK)
   - cart_id (FK)
   - product_id (FK)
   - variant_id (FK)
   - quantity
   - created_at
   - updated_at

9. **orders**
   - id (PK)
   - user_id (FK)
   - address_id (FK)
   - status
   - total_amount
   - payment_method
   - payment_status
   - shipping_charge
   - discount_amount
   - created_at
   - updated_at

10. **order_items**
    - id (PK)
    - order_id (FK)
    - product_id (FK)
    - variant_id (FK)
    - quantity
    - unit_price
    - created_at

11. **reviews**
    - id (PK)
    - product_id (FK)
    - user_id (FK)
    - rating
    - comment
    - created_at
    - updated_at

12. **admins**
    - id (PK)
    - username
    - password_hash
    - email
    - role
    - is_active
    - created_at
    - updated_at

### API Endpoints

#### Customer APIs
- `GET /products` - List products with filtering
- `GET /products/:id` - Get product details
- `GET /categories` - List categories
- `POST /users` - Register new user
- `POST /auth/login` - User login
- `GET /users/:id` - Get user profile
- `PUT /users/:id` - Update user profile
- `POST /cart/items` - Add item to cart
- `GET /cart` - Get cart contents
- `PUT /cart/items/:id` - Update cart item
- `DELETE /cart/items/:id` - Remove cart item
- `POST /orders` - Create new order
- `GET /orders` - List user orders
- `GET /orders/:id` - Get order details
- `POST /reviews` - Create product review

#### Admin APIs
- `POST /admin/auth/login` - Admin login
- `POST /admin/products` - Create product
- `PUT /admin/products/:id` - Update product
- `DELETE /admin/products/:id` - Delete product
- `GET /admin/orders` - List all orders
- `PUT /admin/orders/:id/status` - Update order status
- `GET /admin/inventory` - Get inventory report
- `PUT /admin/inventory` - Update inventory
- `GET /admin/dashboard/stats` - Get dashboard statistics