-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create users table (public.profiles for storing user profiles)
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT UNIQUE NOT NULL,
    first_name TEXT,
    last_name TEXT,
    phone_number TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- Create RLS policies for profiles
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view their own profile" 
    ON public.profiles FOR SELECT 
    USING (auth.uid() = id);

CREATE POLICY "Users can update their own profile" 
    ON public.profiles FOR UPDATE 
    USING (auth.uid() = id);

-- Create product categories table
CREATE TABLE IF NOT EXISTS public.product_categories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    description TEXT,
    parent_id UUID REFERENCES public.product_categories(id),
    image_url TEXT,
    display_order INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- Create RLS policies for product_categories
ALTER TABLE public.product_categories ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Everyone can view product categories" 
    ON public.product_categories FOR SELECT 
    USING (true);

-- Create products table
CREATE TABLE IF NOT EXISTS public.products (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    description TEXT,
    base_price DECIMAL(12, 2) NOT NULL,
    sale_price DECIMAL(12, 2),
    category_id UUID REFERENCES public.product_categories(id),
    stock_quantity INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- Create RLS policies for products
ALTER TABLE public.products ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Everyone can view products" 
    ON public.products FOR SELECT 
    USING (true);

-- Create product images table
CREATE TABLE IF NOT EXISTS public.product_images (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id UUID NOT NULL REFERENCES public.products(id) ON DELETE CASCADE,
    image_url TEXT NOT NULL,
    is_primary BOOLEAN DEFAULT false,
    display_order INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- Create RLS policies for product_images
ALTER TABLE public.product_images ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Everyone can view product images" 
    ON public.product_images FOR SELECT 
    USING (true);

-- Create product variants table
CREATE TABLE IF NOT EXISTS public.product_variants (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id UUID NOT NULL REFERENCES public.products(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    price_adjustment DECIMAL(12, 2) DEFAULT 0,
    stock_quantity INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- Create RLS policies for product_variants
ALTER TABLE public.product_variants ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Everyone can view product variants" 
    ON public.product_variants FOR SELECT 
    USING (true);

-- Create user addresses table
CREATE TABLE IF NOT EXISTS public.addresses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    name TEXT,
    address_line1 TEXT NOT NULL,
    address_line2 TEXT,
    city TEXT NOT NULL,
    state TEXT NOT NULL,
    postal_code TEXT NOT NULL,
    country TEXT NOT NULL DEFAULT 'India',
    phone TEXT NOT NULL,
    is_default BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- Create RLS policies for addresses
ALTER TABLE public.addresses ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view their own addresses" 
    ON public.addresses FOR SELECT 
    USING (auth.uid() = user_id);

CREATE POLICY "Users can insert their own addresses" 
    ON public.addresses FOR INSERT 
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update their own addresses" 
    ON public.addresses FOR UPDATE 
    USING (auth.uid() = user_id);

CREATE POLICY "Users can delete their own addresses" 
    ON public.addresses FOR DELETE 
    USING (auth.uid() = user_id);

-- Create carts table
CREATE TABLE IF NOT EXISTS public.carts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id TEXT NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- Create RLS policies for carts
ALTER TABLE public.carts ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view their own carts" 
    ON public.carts FOR SELECT 
    USING (auth.uid()::text = user_id OR user_id LIKE 'guest_user%');

CREATE POLICY "Users can insert their own carts" 
    ON public.carts FOR INSERT 
    WITH CHECK (auth.uid()::text = user_id OR user_id LIKE 'guest_user%');

CREATE POLICY "Users can update their own carts" 
    ON public.carts FOR UPDATE 
    USING (auth.uid()::text = user_id OR user_id LIKE 'guest_user%');

CREATE POLICY "Users can delete their own carts" 
    ON public.carts FOR DELETE 
    USING (auth.uid()::text = user_id OR user_id LIKE 'guest_user%');

-- Create cart items table
CREATE TABLE IF NOT EXISTS public.cart_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    cart_id UUID NOT NULL REFERENCES public.carts(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES public.products(id),
    variant_id UUID REFERENCES public.product_variants(id),
    quantity INTEGER NOT NULL DEFAULT 1,
    price DECIMAL(12, 2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- Create RLS policies for cart_items
ALTER TABLE public.cart_items ENABLE ROW LEVEL SECURITY;

-- Users can view their own cart items through cart ownership
CREATE POLICY "Users can view cart items in their carts" 
    ON public.cart_items FOR SELECT 
    USING (EXISTS (
        SELECT 1 FROM public.carts 
        WHERE public.carts.id = cart_id 
        AND (auth.uid()::text = public.carts.user_id OR public.carts.user_id LIKE 'guest_user%')
    ));

CREATE POLICY "Users can insert cart items to their carts" 
    ON public.cart_items FOR INSERT 
    WITH CHECK (EXISTS (
        SELECT 1 FROM public.carts 
        WHERE public.carts.id = cart_id 
        AND (auth.uid()::text = public.carts.user_id OR public.carts.user_id LIKE 'guest_user%')
    ));

CREATE POLICY "Users can update cart items in their carts" 
    ON public.cart_items FOR UPDATE 
    USING (EXISTS (
        SELECT 1 FROM public.carts 
        WHERE public.carts.id = cart_id 
        AND (auth.uid()::text = public.carts.user_id OR public.carts.user_id LIKE 'guest_user%')
    ));

CREATE POLICY "Users can delete cart items from their carts" 
    ON public.cart_items FOR DELETE 
    USING (EXISTS (
        SELECT 1 FROM public.carts 
        WHERE public.carts.id = cart_id 
        AND (auth.uid()::text = public.carts.user_id OR public.carts.user_id LIKE 'guest_user%')
    ));

-- Create orders table
CREATE TABLE IF NOT EXISTS public.orders (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES auth.users(id),
    address_id UUID NOT NULL REFERENCES public.addresses(id),
    payment_method TEXT NOT NULL,
    payment_status TEXT NOT NULL DEFAULT 'pending',
    order_status TEXT NOT NULL DEFAULT 'placed',
    subtotal DECIMAL(12, 2) NOT NULL,
    shipping_charge DECIMAL(12, 2) NOT NULL DEFAULT 0,
    discount_amount DECIMAL(12, 2) NOT NULL DEFAULT 0,
    tax_amount DECIMAL(12, 2) NOT NULL DEFAULT 0,
    total_amount DECIMAL(12, 2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- Create RLS policies for orders
ALTER TABLE public.orders ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view their own orders" 
    ON public.orders FOR SELECT 
    USING (auth.uid() = user_id);

CREATE POLICY "Users can insert their own orders" 
    ON public.orders FOR INSERT 
    WITH CHECK (auth.uid() = user_id);

-- Create order items table
CREATE TABLE IF NOT EXISTS public.order_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id UUID NOT NULL REFERENCES public.orders(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES public.products(id),
    variant_id UUID REFERENCES public.product_variants(id),
    quantity INTEGER NOT NULL DEFAULT 1,
    price DECIMAL(12, 2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- Create RLS policies for order_items
ALTER TABLE public.order_items ENABLE ROW LEVEL SECURITY;

-- Users can view their own order items through order ownership
CREATE POLICY "Users can view their own order items" 
    ON public.order_items FOR SELECT 
    USING (EXISTS (
        SELECT 1 FROM public.orders 
        WHERE public.orders.id = order_id 
        AND auth.uid() = public.orders.user_id
    ));

-- Create reviews table
CREATE TABLE IF NOT EXISTS public.reviews (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES public.products(id) ON DELETE CASCADE,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    review_text TEXT,
    is_verified_purchase BOOLEAN DEFAULT false,
    is_approved BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- Create RLS policies for reviews
ALTER TABLE public.reviews ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Everyone can view approved reviews" 
    ON public.reviews FOR SELECT 
    USING (is_approved = true OR auth.uid() = user_id);

CREATE POLICY "Users can insert their own reviews" 
    ON public.reviews FOR INSERT 
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update their own reviews" 
    ON public.reviews FOR UPDATE 
    USING (auth.uid() = user_id);

-- Insert some sample categories
DO $$
DECLARE
    saree_id UUID;
    dhoti_id UUID;
    blouse_id UUID;
    shirt_id UUID;
    
    cotton_saree_id UUID;
    silk_dhoti_id UUID;
    designer_blouse_id UUID;
    formal_shirt_id UUID;
BEGIN
    -- Insert categories and save their IDs
    INSERT INTO public.product_categories (name, description, display_order)
    VALUES ('Sarees', 'Traditional Indian women''s garments', 1)
    RETURNING id INTO saree_id;
    
    INSERT INTO public.product_categories (name, description, display_order)
    VALUES ('Dhotis', 'Traditional men''s wear', 2)
    RETURNING id INTO dhoti_id;
    
    INSERT INTO public.product_categories (name, description, display_order)
    VALUES ('Blouses', 'Women''s blouses and tops', 3)
    RETURNING id INTO blouse_id;
    
    INSERT INTO public.product_categories (name, description, display_order)
    VALUES ('Shirts', 'Men''s casual and formal shirts', 4)
    RETURNING id INTO shirt_id;
    
    -- Insert products and save their IDs
    INSERT INTO public.products (name, description, base_price, sale_price, category_id, stock_quantity, is_active)
    VALUES ('Cotton Saree', 'Handwoven cotton saree with traditional design', 1499.00, 1299.00, saree_id, 10, true)
    RETURNING id INTO cotton_saree_id;
    
    INSERT INTO public.products (name, description, base_price, sale_price, category_id, stock_quantity, is_active)
    VALUES ('Silk Dhoti', 'Premium silk dhoti for special occasions', 899.00, NULL, dhoti_id, 15, true)
    RETURNING id INTO silk_dhoti_id;
    
    INSERT INTO public.products (name, description, base_price, sale_price, category_id, stock_quantity, is_active)
    VALUES ('Designer Blouse', 'Stylish designer blouse with embroidery', 799.00, 649.00, blouse_id, 8, true)
    RETURNING id INTO designer_blouse_id;
    
    INSERT INTO public.products (name, description, base_price, sale_price, category_id, stock_quantity, is_active)
    VALUES ('Formal Cotton Shirt', 'Premium cotton formal shirt for men', 1199.00, 999.00, shirt_id, 20, true)
    RETURNING id INTO formal_shirt_id;
    
    -- Insert product images using the saved product IDs
    INSERT INTO public.product_images (product_id, image_url, is_primary, display_order)
    VALUES 
        (cotton_saree_id, 'https://placekitten.com/500/700', true, 1),
        (silk_dhoti_id, 'https://placekitten.com/501/701', true, 1),
        (designer_blouse_id, 'https://placekitten.com/502/702', true, 1),
        (formal_shirt_id, 'https://placekitten.com/503/703', true, 1);
END $$; 