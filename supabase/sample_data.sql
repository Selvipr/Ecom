-- Sample data for Tiruvear Textiles App

-- Insert product categories
INSERT INTO public.product_categories (name, description, image_url, display_order)
VALUES
  ('Sarees', 'Traditional Indian sarees in various fabrics and designs', 'https://images.unsplash.com/photo-1610189420318-709d8b5b1b49?q=80&w=3000', 1),
  ('Shirts', 'Formal and casual shirts for men', 'https://images.unsplash.com/photo-1596755094514-f87e34085b2c?q=80&w=3000', 2),
  ('Dresses', 'Women''s dresses in various styles', 'https://images.unsplash.com/photo-1572804013427-4d7ca7268217?q=80&w=3000', 3),
  ('Ethnic Wear', 'Traditional ethnic clothing for all occasions', 'https://images.unsplash.com/photo-1610189420318-709d8b5b1b49?q=80&w=3000', 4),
  ('Children''s Wear', 'Clothing for children of all ages', 'https://images.unsplash.com/photo-1622290291468-a28f7a7dc6a8?q=80&w=3000', 5),
  ('Home Textiles', 'Textiles for home decor and furnishing', 'https://images.unsplash.com/photo-1586023492125-27b2c045efd7?q=80&w=3000', 6);

-- Insert sample products
INSERT INTO public.products (name, description, base_price, sale_price, category_id, stock_quantity, is_active)
SELECT 
  'Silk Saree', 
  'Handcrafted silk saree with rich design and premium quality', 
  12999.00, 
  9999.00, 
  id, 
  25, 
  true
FROM public.product_categories WHERE name = 'Sarees'
LIMIT 1;

INSERT INTO public.products (name, description, base_price, sale_price, category_id, stock_quantity, is_active)
SELECT 
  'Cotton Shirt', 
  'Premium cotton formal shirt for men', 
  1499.00, 
  1299.00, 
  id, 
  50, 
  true
FROM public.product_categories WHERE name = 'Shirts'
LIMIT 1;

INSERT INTO public.products (name, description, base_price, sale_price, category_id, stock_quantity, is_active)
SELECT 
  'Casual Dress', 
  'Comfortable casual dress for everyday wear', 
  2499.00, 
  1999.00, 
  id, 
  30, 
  true
FROM public.product_categories WHERE name = 'Dresses'
LIMIT 1;

INSERT INTO public.products (name, description, base_price, sale_price, category_id, stock_quantity, is_active)
SELECT 
  'Kurta Set', 
  'Traditional kurta set with detailed embroidery', 
  3499.00, 
  2999.00, 
  id, 
  20, 
  true
FROM public.product_categories WHERE name = 'Ethnic Wear'
LIMIT 1;

INSERT INTO public.products (name, description, base_price, sale_price, category_id, stock_quantity, is_active)
SELECT 
  'Kids T-shirt', 
  'Colorful t-shirt for children with fun designs', 
  699.00, 
  599.00, 
  id, 
  100, 
  true
FROM public.product_categories WHERE name = 'Children''s Wear'
LIMIT 1;

INSERT INTO public.products (name, description, base_price, sale_price, category_id, stock_quantity, is_active)
SELECT 
  'Bed Sheet Set', 
  'Premium quality bed sheet set with pillow covers', 
  2999.00, 
  2499.00, 
  id, 
  40, 
  true
FROM public.product_categories WHERE name = 'Home Textiles'
LIMIT 1;

-- Add product images
INSERT INTO public.product_images (product_id, image_url, is_primary)
SELECT 
  id, 
  'https://images.unsplash.com/photo-1610189420318-709d8b5b1b49?q=80&w=3000', 
  true
FROM public.products
WHERE name = 'Silk Saree';

INSERT INTO public.product_images (product_id, image_url, is_primary)
SELECT 
  id, 
  'https://images.unsplash.com/photo-1596755094514-f87e34085b2c?q=80&w=3000', 
  true
FROM public.products
WHERE name = 'Cotton Shirt';

INSERT INTO public.product_images (product_id, image_url, is_primary)
SELECT 
  id, 
  'https://images.unsplash.com/photo-1623609163859-ca93c959b5b8?q=80&w=3000', 
  true
FROM public.products
WHERE name = 'Casual Dress';

INSERT INTO public.product_images (product_id, image_url, is_primary)
SELECT 
  id, 
  'https://images.unsplash.com/photo-1610189420318-709d8b5b1b49?q=80&w=3000', 
  true
FROM public.products
WHERE name = 'Kurta Set';

INSERT INTO public.product_images (product_id, image_url, is_primary)
SELECT 
  id, 
  'https://images.unsplash.com/photo-1622290291468-a28f7a7dc6a8?q=80&w=3000', 
  true
FROM public.products
WHERE name = 'Kids T-shirt';

INSERT INTO public.product_images (product_id, image_url, is_primary)
SELECT 
  id, 
  'https://images.unsplash.com/photo-1586023492125-27b2c045efd7?q=80&w=3000', 
  true
FROM public.products
WHERE name = 'Bed Sheet Set'; 