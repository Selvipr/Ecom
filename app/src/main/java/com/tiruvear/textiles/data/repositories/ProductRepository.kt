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
            Result.failure(e)
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
            Result.failure(e)
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
            Result.failure(e)
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
                .map { mapToProductCategory(it) }
                
            Result.success(categories)
        } catch (e: Exception) {
            Result.failure(e)
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
            createdAt = data["created_at"] as java.util.Date,
            updatedAt = data["updated_at"] as java.util.Date
        )
    }
} 