package com.tiruvear.textiles.data.repositories

import com.tiruvear.textiles.TiruvearApp
import com.tiruvear.textiles.data.models.Product
import com.tiruvear.textiles.data.models.ProductCategory
import io.github.jan.supabase.postgrest.from
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
            
            val products = supabase.from("products")
                .select("*, product_images(*), product_variants(*)")
                .eq("is_active", true)
                .range(offset, offset + limit - 1)
                .order("created_at", ascending = false)
                .decodeList<Map<String, Any>>()
                .map { mapToProduct(it) }
                
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getProductById(productId: String): Result<Product> = withContext(Dispatchers.IO) {
        try {
            val productData = supabase.from("products")
                .select("*, product_images(*), product_categories(*), product_variants(*)")
                .eq("id", productId)
                .single()
                .decodeAs<Map<String, Any>>()
                
            Result.success(mapToProduct(productData))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getProductsByCategory(categoryId: String, page: Int, limit: Int): Result<List<Product>> = withContext(Dispatchers.IO) {
        try {
            val offset = (page - 1) * limit
            
            val products = supabase.from("products")
                .select("*, product_images(*), product_variants(*)")
                .eq("category_id", categoryId)
                .eq("is_active", true)
                .range(offset, offset + limit - 1)
                .order("created_at", ascending = false)
                .decodeList<Map<String, Any>>()
                .map { mapToProduct(it) }
                
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getFeaturedProducts(limit: Int): Result<List<Product>> = withContext(Dispatchers.IO) {
        try {
            // Assuming there's a "is_featured" column or similar
            val products = supabase.from("products")
                .select("*, product_images(*), product_variants(*)")
                .eq("is_active", true)
                .eq("is_featured", true)
                .limit(limit.toLong())
                .decodeList<Map<String, Any>>()
                .map { mapToProduct(it) }
                
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getNewArrivals(limit: Int): Result<List<Product>> = withContext(Dispatchers.IO) {
        try {
            val products = supabase.from("products")
                .select("*, product_images(*), product_variants(*)")
                .eq("is_active", true)
                .order("created_at", ascending = false)
                .limit(limit.toLong())
                .decodeList<Map<String, Any>>()
                .map { mapToProduct(it) }
                
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun searchProducts(query: String, page: Int, limit: Int): Result<List<Product>> = withContext(Dispatchers.IO) {
        try {
            val offset = (page - 1) * limit
            
            val products = supabase.from("products")
                .select("*, product_images(*), product_variants(*)")
                .eq("is_active", true)
                .ilike("name", "%$query%")
                .range(offset, offset + limit - 1)
                .decodeList<Map<String, Any>>()
                .map { mapToProduct(it) }
                
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getAllCategories(): Result<List<ProductCategory>> = withContext(Dispatchers.IO) {
        try {
            val categories = supabase.from("product_categories")
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
            val categoryData = supabase.from("product_categories")
                .select()
                .eq("id", categoryId)
                .single()
                .decodeAs<Map<String, Any>>()
                
            Result.success(mapToProductCategory(categoryData))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Helper methods to map response data to model objects
    @Suppress("UNCHECKED_CAST")
    private fun mapToProduct(data: Map<String, Any>): Product {
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