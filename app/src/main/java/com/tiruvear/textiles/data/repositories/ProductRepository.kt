package com.tiruvear.textiles.data.repositories

import com.tiruvear.textiles.TiruvearApp
import com.tiruvear.textiles.data.models.Product
import com.tiruvear.textiles.data.models.ProductCategory
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.FilterOperator
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface ProductRepository {
    suspend fun getAllProducts(page: Int, limit: Int): Result<List<Product>>
    suspend fun getProductById(productId: String): Result<Product>
    suspend fun getProductsByCategory(categoryId: String, page: Int, limit: Int): Result<List<Product>>
    suspend fun getFeaturedProducts(limit: Int): Result<List<Product>>
    suspend fun getNewArrivals(limit: Int): Result<List<Product>>
    suspend fun searchProducts(query: String, page: Int, limit: Int): Result<List<Product>>
    suspend fun getAllCategories(): Result<List<ProductCategory>>
    suspend fun getCategoryById(categoryId: String): Result<ProductCategory>
}

class ProductRepositoryImpl : ProductRepository {

    private val supabase = TiruvearApp.supabaseClient
    
    override suspend fun getAllProducts(page: Int, limit: Int): Result<List<Product>> = withContext(Dispatchers.IO) {
        try {
            val offset = (page - 1) * limit
            
            val products = supabase.postgrest["products"]
                .select {
                    filter("is_active", FilterOperator.EQ, true)
                    range(offset.toLong(), (offset + limit - 1).toLong())
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<Map<String, Any>>()
            
            // Load additional data for each product
            val productsWithData = products.map { product ->
                val productId = product["id"] as String
                
                // Get images
                val images = supabase.postgrest["product_images"]
                    .select {
                        filter("product_id", FilterOperator.EQ, productId)
                    }
                    .decodeList<Map<String, Any>>()
                
                // Get variants
                val variants = supabase.postgrest["product_variants"]
                    .select {
                        filter("product_id", FilterOperator.EQ, productId)
                    }
                    .decodeList<Map<String, Any>>()
                
                val fullProductData = product.toMutableMap()
                fullProductData["product_images"] = images
                fullProductData["product_variants"] = variants
                
                mapToProduct(fullProductData)
            }
                
            Result.success(productsWithData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getProductById(productId: String): Result<Product> = withContext(Dispatchers.IO) {
        try {
            val productData = supabase.postgrest["products"]
                .select {
                    filter("id", FilterOperator.EQ, productId)
                }
                .decodeSingle<Map<String, Any>>()
            
            // Get images
            val images = supabase.postgrest["product_images"]
                .select {
                    filter("product_id", FilterOperator.EQ, productId)
                }
                .decodeList<Map<String, Any>>()
            
            // Get category
            val categoryId = productData["category_id"] as String
            val categories = supabase.postgrest["product_categories"]
                .select {
                    filter("id", FilterOperator.EQ, categoryId)
                }
                .decodeList<Map<String, Any>>()
            
            // Get variants
            val variants = supabase.postgrest["product_variants"]
                .select {
                    filter("product_id", FilterOperator.EQ, productId)
                }
                .decodeList<Map<String, Any>>()
            
            val fullProductData = productData.toMutableMap()
            fullProductData["product_images"] = images
            fullProductData["product_categories"] = categories
            fullProductData["product_variants"] = variants
                
            Result.success(mapToProduct(fullProductData))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getProductsByCategory(categoryId: String, page: Int, limit: Int): Result<List<Product>> = withContext(Dispatchers.IO) {
        try {
            val offset = (page - 1) * limit
            
            val products = supabase.postgrest["products"]
                .select {
                    filter("category_id", FilterOperator.EQ, categoryId)
                    filter("is_active", FilterOperator.EQ, true)
                    range(offset.toLong(), (offset + limit - 1).toLong())
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<Map<String, Any>>()
            
            // Load additional data for each product
            val productsWithData = products.map { product ->
                val productId = product["id"] as String
                
                // Get images
                val images = supabase.postgrest["product_images"]
                    .select {
                        filter("product_id", FilterOperator.EQ, productId)
                    }
                    .decodeList<Map<String, Any>>()
                
                // Get variants
                val variants = supabase.postgrest["product_variants"]
                    .select {
                        filter("product_id", FilterOperator.EQ, productId)
                    }
                    .decodeList<Map<String, Any>>()
                
                val fullProductData = product.toMutableMap()
                fullProductData["product_images"] = images
                fullProductData["product_variants"] = variants
                
                mapToProduct(fullProductData)
            }
                
            Result.success(productsWithData)
        } catch (e: Exception) {
            // Return mock data when network request fails
            val mockProducts = createMockProductsForCategory(categoryId)
            Result.success(mockProducts)
        }
    }
    
    override suspend fun getFeaturedProducts(limit: Int): Result<List<Product>> = withContext(Dispatchers.IO) {
        try {
            // Assuming there's a "is_featured" column or similar
            val products = supabase.postgrest["products"]
                .select {
                    filter("is_active", FilterOperator.EQ, true)
                    filter("is_featured", FilterOperator.EQ, true)
                    limit(limit.toLong())
                }
                .decodeList<Map<String, Any>>()
            
            // Load additional data for each product
            val productsWithData = products.map { product ->
                val productId = product["id"] as String
                
                // Get images
                val images = supabase.postgrest["product_images"]
                    .select {
                        filter("product_id", FilterOperator.EQ, productId)
                    }
                    .decodeList<Map<String, Any>>()
                
                // Get variants
                val variants = supabase.postgrest["product_variants"]
                    .select {
                        filter("product_id", FilterOperator.EQ, productId)
                    }
                    .decodeList<Map<String, Any>>()
                
                val fullProductData = product.toMutableMap()
                fullProductData["product_images"] = images
                fullProductData["product_variants"] = variants
                
                mapToProduct(fullProductData)
            }
                
            Result.success(productsWithData)
        } catch (e: Exception) {
            // Return mock featured products
            val now = java.util.Date()
            val mockProducts = mutableListOf<Product>()
            
            // Create featured products from different categories
            for (i in 1..limit) {
                val categoryId = ((i % 6) + 1).toString()
                val categoryName = when (categoryId) {
                    "1" -> "Sarees"
                    "2" -> "Dhotis"
                    "3" -> "Blouses"
                    "4" -> "Shirts"
                    "5" -> "Pants"
                    "6" -> "Kids Wear"
                    else -> "Products"
                }
                
                val basePrice = when (categoryId) {
                    "1" -> 1499.0 + (i * 100) // Sarees
                    "2" -> 899.0 + (i * 50)   // Dhotis
                    "3" -> 599.0 + (i * 30)   // Blouses
                    "4" -> 799.0 + (i * 40)   // Shirts
                    "5" -> 999.0 + (i * 50)   // Pants
                    "6" -> 699.0 + (i * 30)   // Kids Wear
                    else -> 999.0 + (i * 50)
                }
                
                // Apply sale price to some products
                val salePrice = if (i % 2 == 0) basePrice * 0.8 else null
                
                mockProducts.add(
                    Product(
                        id = "featured-$i",
                        name = "Featured $categoryName - Premium $i",
                        description = "This is a featured high-quality $categoryName made with finest materials. Perfect for all occasions.",
                        basePrice = basePrice,
                        salePrice = salePrice,
                        categoryId = categoryId,
                        stockQuantity = 15 + i,
                        isActive = true,
                        createdAt = now,
                        updatedAt = now
                    )
                )
            }
            
            Result.success(mockProducts)
        }
    }
    
    override suspend fun getNewArrivals(limit: Int): Result<List<Product>> = withContext(Dispatchers.IO) {
        try {
            val products = supabase.postgrest["products"]
                .select {
                    filter("is_active", FilterOperator.EQ, true)
                    order("created_at", Order.DESCENDING)
                    limit(limit.toLong())
                }
                .decodeList<Map<String, Any>>()
            
            // Load additional data for each product
            val productsWithData = products.map { product ->
                val productId = product["id"] as String
                
                // Get images
                val images = supabase.postgrest["product_images"]
                    .select {
                        filter("product_id", FilterOperator.EQ, productId)
                    }
                    .decodeList<Map<String, Any>>()
                
                // Get variants
                val variants = supabase.postgrest["product_variants"]
                    .select {
                        filter("product_id", FilterOperator.EQ, productId)
                    }
                    .decodeList<Map<String, Any>>()
                
                val fullProductData = product.toMutableMap()
                fullProductData["product_images"] = images
                fullProductData["product_variants"] = variants
                
                mapToProduct(fullProductData)
            }
                
            Result.success(productsWithData)
        } catch (e: Exception) {
            // Return mock new arrivals products
            val now = java.util.Date()
            val mockProducts = mutableListOf<Product>()
            
            // Create new arrivals from different categories
            for (i in 1..limit) {
                val categoryId = ((i % 6) + 1).toString()
                val categoryName = when (categoryId) {
                    "1" -> "Sarees"
                    "2" -> "Dhotis"
                    "3" -> "Blouses"
                    "4" -> "Shirts"
                    "5" -> "Pants"
                    "6" -> "Kids Wear"
                    else -> "Products"
                }
                
                val basePrice = when (categoryId) {
                    "1" -> 1799.0 + (i * 100) // Sarees
                    "2" -> 1099.0 + (i * 50)  // Dhotis
                    "3" -> 699.0 + (i * 30)   // Blouses
                    "4" -> 899.0 + (i * 40)   // Shirts
                    "5" -> 1199.0 + (i * 50)  // Pants
                    "6" -> 799.0 + (i * 30)   // Kids Wear
                    else -> 1099.0 + (i * 50)
                }
                
                // Apply sale price to some products
                val salePrice = if (i % 3 == 0) basePrice * 0.85 else null
                
                mockProducts.add(
                    Product(
                        id = "new-arrival-$i",
                        name = "New $categoryName - Collection $i",
                        description = "Brand new $categoryName from our latest collection. Fresh designs with premium quality materials.",
                        basePrice = basePrice,
                        salePrice = salePrice,
                        categoryId = categoryId,
                        stockQuantity = 20 + i,
                        isActive = true,
                        createdAt = now,
                        updatedAt = now
                    )
                )
            }
            
            Result.success(mockProducts)
        }
    }
    
    override suspend fun searchProducts(query: String, page: Int, limit: Int): Result<List<Product>> = withContext(Dispatchers.IO) {
        try {
            val offset = (page - 1) * limit
            
            val products = supabase.postgrest["products"]
                .select {
                    filter("is_active", FilterOperator.EQ, true)
                    filter("name", FilterOperator.ILIKE, "%$query%")
                    range(offset.toLong(), (offset + limit - 1).toLong())
                }
                .decodeList<Map<String, Any>>()
            
            // Load additional data for each product
            val productsWithData = products.map { product ->
                val productId = product["id"] as String
                
                // Get images
                val images = supabase.postgrest["product_images"]
                    .select {
                        filter("product_id", FilterOperator.EQ, productId)
                    }
                    .decodeList<Map<String, Any>>()
                
                // Get variants
                val variants = supabase.postgrest["product_variants"]
                    .select {
                        filter("product_id", FilterOperator.EQ, productId)
                    }
                    .decodeList<Map<String, Any>>()
                
                val fullProductData = product.toMutableMap()
                fullProductData["product_images"] = images
                fullProductData["product_variants"] = variants
                
                mapToProduct(fullProductData)
            }
                
            Result.success(productsWithData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getAllCategories(): Result<List<ProductCategory>> = withContext(Dispatchers.IO) {
        try {
            val categories = supabase.postgrest["product_categories"]
                .select()
                .decodeList<Map<String, Any>>()
            
            val mappedCategories = categories.map { category ->
                ProductCategory(
                    id = category["id"] as String,
                    name = category["name"] as String,
                    description = category["description"] as? String ?: "",
                    imageUrl = category["image_url"] as? String ?: "https://placekitten.com/200/200",
                    displayOrder = (category["display_order"] as? Number)?.toInt() ?: 0
                )
            }
            
            Result.success(mappedCategories)
        } catch (e: Exception) {
            // Fallback to mock data if fetching real data fails
            val mockCategories = listOf(
                ProductCategory(
                    id = "1",
                    name = "Sarees",
                    description = "Traditional Indian sarees",
                    imageUrl = "https://placekitten.com/200/200",
                    displayOrder = 1
                ),
                ProductCategory(
                    id = "2",
                    name = "Dhotis",
                    description = "Traditional men's wear",
                    imageUrl = "https://placekitten.com/201/201",
                    displayOrder = 2
                ),
                ProductCategory(
                    id = "3",
                    name = "Blouses",
                    description = "Blouse pieces for sarees",
                    imageUrl = "https://placekitten.com/202/202",
                    displayOrder = 3
                ),
                ProductCategory(
                    id = "4",
                    name = "Shirts",
                    description = "Men's shirts",
                    imageUrl = "https://placekitten.com/203/203",
                    displayOrder = 4
                ),
                ProductCategory(
                    id = "5",
                    name = "Pants",
                    description = "Men's pants",
                    imageUrl = "https://placekitten.com/204/204",
                    displayOrder = 5
                ),
                ProductCategory(
                    id = "6",
                    name = "Kids Wear",
                    description = "Clothing for children",
                    imageUrl = "https://placekitten.com/205/205",
                    displayOrder = 6
                )
            )
            Result.success(mockCategories)
        }
    }
    
    override suspend fun getCategoryById(categoryId: String): Result<ProductCategory> = withContext(Dispatchers.IO) {
        try {
            val categoryData = supabase.postgrest["product_categories"]
                .select {
                    filter("id", FilterOperator.EQ, categoryId)
                }
                .decodeSingle<Map<String, Any>>()
                
            Result.success(mapToProductCategory(categoryData))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Helper methods to map response data to model objects
    @Suppress("UNCHECKED_CAST")
    private fun mapToProduct(data: Map<String, Any>): Product {
        // TODO: Map relationships like images, variants, etc.
        // This implementation depends on your Product model structure
        return Product(
            id = data["id"] as String,
            name = data["name"] as String,
            description = data["description"] as String,
            basePrice = (data["base_price"] as Number).toDouble(),
            salePrice = (data["sale_price"] as? Number)?.toDouble(),
            categoryId = data["category_id"] as String,
            stockQuantity = (data["stock_quantity"] as Number).toInt(),
            isActive = data["is_active"] as Boolean,
            createdAt = data["created_at"] as java.util.Date,
            updatedAt = data["updated_at"] as java.util.Date
            // Note: Relationships are handled separately and would need more complex mapping
        )
    }
    
    private fun mapToProductCategory(data: Map<String, Any>): ProductCategory {
        return ProductCategory(
            id = data["id"] as String,
            name = data["name"] as String,
            description = data["description"] as? String,
            imageUrl = data["image_url"] as? String,
            parentCategoryId = data["parent_category_id"] as? String,
            displayOrder = (data["display_order"] as? Number)?.toInt() ?: 0,
            createdAt = data["created_at"] as? java.util.Date ?: java.util.Date(),
            updatedAt = data["updated_at"] as? java.util.Date ?: java.util.Date()
        )
    }
    
    // Helper method to create mock products for a specific category
    private fun createMockProductsForCategory(categoryId: String): List<Product> {
        val now = java.util.Date()
        val mockProducts = mutableListOf<Product>()
        val categoryName = when (categoryId) {
            "1" -> "Sarees"
            "2" -> "Dhotis"
            "3" -> "Blouses"
            "4" -> "Shirts"
            "5" -> "Pants"
            "6" -> "Kids Wear"
            else -> "Products"
        }
        
        // Create 10 mock products for the category
        for (i in 1..10) {
            val productId = "mock-$categoryId-$i"
            val basePrice = when (categoryId) {
                "1" -> 1499.0 + (i * 100) // Sarees
                "2" -> 899.0 + (i * 50)   // Dhotis
                "3" -> 599.0 + (i * 30)   // Blouses
                "4" -> 799.0 + (i * 40)   // Shirts
                "5" -> 999.0 + (i * 50)   // Pants
                "6" -> 699.0 + (i * 30)   // Kids Wear
                else -> 999.0 + (i * 50)
            }
            
            // Apply sale price to some products
            val salePrice = if (i % 3 == 0) basePrice * 0.9 else null
            
            mockProducts.add(
                Product(
                    id = productId,
                    name = "$categoryName - Premium Quality $i",
                    description = "This is a high-quality $categoryName made with finest materials. Perfect for all occasions.",
                    basePrice = basePrice,
                    salePrice = salePrice,
                    categoryId = categoryId,
                    stockQuantity = 10 + i,
                    isActive = true,
                    createdAt = now,
                    updatedAt = now
                )
            )
        }
        
        return mockProducts
    }
} 