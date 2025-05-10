-- Security Policies for Tiruvear Textiles App

-- Policies for profiles table
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view their own profile" 
    ON public.profiles FOR SELECT 
    USING (auth.uid() = id);

CREATE POLICY "Users can update their own profile" 
    ON public.profiles FOR UPDATE 
    USING (auth.uid() = id);

-- Policies for product_categories table
ALTER TABLE public.product_categories ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Everyone can view product categories" 
    ON public.product_categories FOR SELECT 
    USING (true);

-- Policies for products table
ALTER TABLE public.products ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Everyone can view products" 
    ON public.products FOR SELECT 
    USING (true);

-- Policies for product_images table
ALTER TABLE public.product_images ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Everyone can view product images" 
    ON public.product_images FOR SELECT 
    USING (true);

-- Policies for product_variants table
ALTER TABLE public.product_variants ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Everyone can view product variants" 
    ON public.product_variants FOR SELECT 
    USING (true);

-- Policies for addresses table
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

-- Policies for carts table
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

-- Policies for cart_items table
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

-- Policies for orders table
ALTER TABLE public.orders ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view their own orders" 
    ON public.orders FOR SELECT 
    USING (auth.uid() = user_id);

CREATE POLICY "Users can insert their own orders" 
    ON public.orders FOR INSERT 
    WITH CHECK (auth.uid() = user_id);

-- Policies for order_items table
ALTER TABLE public.order_items ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view items in their own orders" 
    ON public.order_items FOR SELECT 
    USING (EXISTS (
        SELECT 1 FROM public.orders 
        WHERE public.orders.id = order_id 
        AND auth.uid() = public.orders.user_id
    ));

-- Policies for reviews table
ALTER TABLE public.reviews ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Everyone can view reviews" 
    ON public.reviews FOR SELECT 
    USING (true);

CREATE POLICY "Users can create their own reviews" 
    ON public.reviews FOR INSERT 
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update their own reviews" 
    ON public.reviews FOR UPDATE 
    USING (auth.uid() = user_id);

CREATE POLICY "Users can delete their own reviews" 
    ON public.reviews FOR DELETE 
    USING (auth.uid() = user_id); 