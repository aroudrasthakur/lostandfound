package com.uta.lostfound.utils

import android.content.Context
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.uta.lostfound.data.model.Item
import com.uta.lostfound.data.model.ItemCategory
import com.uta.lostfound.data.model.ItemStatus
import kotlinx.coroutines.tasks.await
import java.util.*

/**
 * Utility class to seed mock data into Firebase Firestore for testing
 */
object FirebaseDataSeeder {
    private const val TAG = "FirebaseDataSeeder"
    private val db = FirebaseFirestore.getInstance()
    
    /**
     * Add mock lost and found items to Firestore
     */
    suspend fun seedMockData(): Result<String> {
        return try {
            Log.d(TAG, "Starting to seed mock data...")
            
            val lostItems = createMockLostItems()
            val foundItems = createMockFoundItems()
            
            var lostCount = 0
            var foundCount = 0
            
            // Add lost items
            lostItems.forEach { item ->
                db.collection("lost_items")
                    .document(item.id)
                    .set(item.toMap())
                    .await()
                lostCount++
                Log.d(TAG, "Added lost item: ${item.title}")
            }
            
            // Add found items
            foundItems.forEach { item ->
                db.collection("found_items")
                    .document(item.id)
                    .set(item.toMap())
                    .await()
                foundCount++
                Log.d(TAG, "Added found item: ${item.title}")
            }
            
            val message = "✓ Successfully added $lostCount lost items and $foundCount found items"
            Log.d(TAG, message)
            Result.success(message)
            
        } catch (e: Exception) {
            val error = "✗ Failed to seed data: ${e.message}"
            Log.e(TAG, error, e)
            Result.failure(e)
        }
    }
    
    /**
     * Clear all mock data from Firestore
     */
    suspend fun clearMockData(): Result<String> {
        return try {
            Log.d(TAG, "Clearing mock data...")
            
            // Delete lost items
            val lostSnapshot = db.collection("lost_items").get().await()
            lostSnapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }
            
            // Delete found items
            val foundSnapshot = db.collection("found_items").get().await()
            foundSnapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }
            
            val message = "✓ Cleared ${lostSnapshot.size()} lost items and ${foundSnapshot.size()} found items"
            Log.d(TAG, message)
            Result.success(message)
            
        } catch (e: Exception) {
            val error = "✗ Failed to clear data: ${e.message}"
            Log.e(TAG, error, e)
            Result.failure(e)
        }
    }
    
    private fun createMockLostItems(): List<Item> {
        val baseTime = System.currentTimeMillis()
        
        return listOf(
            Item(
                id = "lost_001",
                title = "Black Backpack",
                description = "Black Nike backpack with laptop compartment. Contains textbooks and a calculator.",
                category = ItemCategory.BAGS.name,
                location = "Central Library, 2nd Floor",
                date = baseTime - (2 * 24 * 60 * 60 * 1000), // 2 days ago
                imageUrl = "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=400&h=400&fit=crop",
                userId = "mock_user_1",
                userName = "John Smith",
                status = ItemStatus.LOST,
                isActive = true,
                createdAt = baseTime - (2 * 24 * 60 * 60 * 1000),
                updatedAt = baseTime - (2 * 24 * 60 * 60 * 1000)
            ),
            Item(
                id = "lost_002",
                title = "iPhone 13 Pro",
                description = "Space Gray iPhone 13 Pro with a clear case. Has a small crack on the bottom right.",
                category = ItemCategory.ELECTRONICS.name,
                location = "Engineering Research Building, Room 301",
                date = baseTime - (1 * 24 * 60 * 60 * 1000), // 1 day ago
                imageUrl = "https://images.unsplash.com/photo-1592286927505-ab7b0c7e5e84?w=400&h=400&fit=crop",
                userId = "mock_user_2",
                userName = "Sarah Johnson",
                status = ItemStatus.LOST,
                isActive = true,
                createdAt = baseTime - (1 * 24 * 60 * 60 * 1000),
                updatedAt = baseTime - (1 * 24 * 60 * 60 * 1000)
            ),
            Item(
                id = "lost_003",
                title = "Car Keys with Honda Keychain",
                description = "Honda car keys with a blue lanyard and a UTA student ID card holder attached.",
                category = ItemCategory.KEYS.name,
                location = "University Center, Food Court",
                date = baseTime - (3 * 24 * 60 * 60 * 1000), // 3 days ago
                imageUrl = "https://images.unsplash.com/photo-1582139329536-e7284fece509?w=400&h=400&fit=crop",
                userId = "mock_user_3",
                userName = "Michael Chen",
                status = ItemStatus.LOST,
                isActive = true,
                createdAt = baseTime - (3 * 24 * 60 * 60 * 1000),
                updatedAt = baseTime - (3 * 24 * 60 * 60 * 1000)
            ),
            Item(
                id = "lost_004",
                title = "Blue Denim Jacket",
                description = "Light blue denim jacket, size Medium. Has a small pin with a smiley face on the collar.",
                category = ItemCategory.CLOTHING.name,
                location = "Science Hall, Lecture Room 104",
                date = baseTime - (5 * 24 * 60 * 60 * 1000), // 5 days ago
                imageUrl = "https://images.unsplash.com/photo-1576995853123-5a10305d93c0?w=400&h=400&fit=crop",
                userId = "mock_user_4",
                userName = "Emily Davis",
                status = ItemStatus.LOST,
                isActive = true,
                createdAt = baseTime - (5 * 24 * 60 * 60 * 1000),
                updatedAt = baseTime - (5 * 24 * 60 * 60 * 1000)
            ),
            Item(
                id = "lost_005",
                title = "Calculus Textbook",
                description = "Calculus: Early Transcendentals, 8th Edition. Name 'Alex Martinez' written inside cover.",
                category = ItemCategory.BOOKS.name,
                location = "Nedderman Hall, 3rd Floor Study Area",
                date = baseTime - (4 * 24 * 60 * 60 * 1000), // 4 days ago
                imageUrl = "https://images.unsplash.com/photo-1532012197267-da84d127e765?w=400&h=400&fit=crop",
                userId = "mock_user_5",
                userName = "Alex Martinez",
                status = ItemStatus.LOST,
                isActive = true,
                createdAt = baseTime - (4 * 24 * 60 * 60 * 1000),
                updatedAt = baseTime - (4 * 24 * 60 * 60 * 1000)
            ),
            Item(
                id = "lost_006",
                title = "AirPods Pro",
                description = "White AirPods Pro in charging case. Case has a few scratches on the back.",
                category = ItemCategory.ELECTRONICS.name,
                location = "Student Recreation Center, Gym Area",
                date = baseTime - (6 * 60 * 60 * 1000), // 6 hours ago
                imageUrl = "https://images.unsplash.com/photo-1606841837239-c5a1a4a07af7?w=400&h=400&fit=crop",
                userId = "mock_user_6",
                userName = "Jessica Brown",
                status = ItemStatus.LOST,
                isActive = true,
                createdAt = baseTime - (6 * 60 * 60 * 1000),
                updatedAt = baseTime - (6 * 60 * 60 * 1000)
            ),
            Item(
                id = "lost_007",
                title = "Student ID Card",
                description = "UTA student ID for Robert Wilson. ID number starting with 1002.",
                category = ItemCategory.DOCUMENTS.name,
                location = "College Park Center, Main Entrance",
                date = baseTime - (12 * 60 * 60 * 1000), // 12 hours ago
                imageUrl = "https://images.unsplash.com/photo-1589395937726-f6097dfa2222?w=400&h=400&fit=crop",
                userId = "mock_user_7",
                userName = "Robert Wilson",
                status = ItemStatus.LOST,
                isActive = true,
                createdAt = baseTime - (12 * 60 * 60 * 1000),
                updatedAt = baseTime - (12 * 60 * 60 * 1000)
            ),
            Item(
                id = "lost_008",
                title = "Silver Watch",
                description = "Stainless steel watch with black leather strap. Seiko brand, analog display.",
                category = ItemCategory.ACCESSORIES.name,
                location = "Fine Arts Building, Room 201",
                date = baseTime - (7 * 24 * 60 * 60 * 1000), // 7 days ago
                imageUrl = "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400&h=400&fit=crop",
                userId = "mock_user_8",
                userName = "David Lee",
                status = ItemStatus.LOST,
                isActive = true,
                createdAt = baseTime - (7 * 24 * 60 * 60 * 1000),
                updatedAt = baseTime - (7 * 24 * 60 * 60 * 1000)
            )
        )
    }
    
    private fun createMockFoundItems(): List<Item> {
        val baseTime = System.currentTimeMillis()
        
        return listOf(
            Item(
                id = "found_001",
                title = "Red Water Bottle",
                description = "Stainless steel red water bottle, brand Hydro Flask. Has some stickers on it.",
                category = ItemCategory.OTHER.name,
                location = "Central Library, 1st Floor",
                date = baseTime - (1 * 24 * 60 * 60 * 1000), // 1 day ago
                imageUrl = "https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=400&h=400&fit=crop",
                userId = "mock_user_9",
                userName = "Lisa Anderson",
                status = ItemStatus.FOUND,
                isActive = true,
                createdAt = baseTime - (1 * 24 * 60 * 60 * 1000),
                updatedAt = baseTime - (1 * 24 * 60 * 60 * 1000)
            ),
            Item(
                id = "found_002",
                title = "Wireless Mouse",
                description = "Black Logitech wireless mouse. Found with USB receiver.",
                category = ItemCategory.ELECTRONICS.name,
                location = "Engineering Research Building, Computer Lab",
                date = baseTime - (2 * 24 * 60 * 60 * 1000), // 2 days ago
                imageUrl = "https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=400&h=400&fit=crop",
                userId = "mock_user_10",
                userName = "Kevin Taylor",
                status = ItemStatus.FOUND,
                isActive = true,
                createdAt = baseTime - (2 * 24 * 60 * 60 * 1000),
                updatedAt = baseTime - (2 * 24 * 60 * 60 * 1000)
            ),
            Item(
                id = "found_003",
                title = "Blue Umbrella",
                description = "Navy blue compact umbrella with wooden handle. Found near the entrance.",
                category = ItemCategory.OTHER.name,
                location = "University Center, Main Entrance",
                date = baseTime - (3 * 24 * 60 * 60 * 1000), // 3 days ago
                imageUrl = "https://images.unsplash.com/photo-1528249217396-7e3144f5ddb6?w=400&h=400&fit=crop",
                userId = "mock_user_11",
                userName = "Amanda White",
                status = ItemStatus.FOUND,
                isActive = true,
                createdAt = baseTime - (3 * 24 * 60 * 60 * 1000),
                updatedAt = baseTime - (3 * 24 * 60 * 60 * 1000)
            ),
            Item(
                id = "found_004",
                title = "Black Glasses",
                description = "Black frame prescription glasses in a hard case. Ray-Ban brand.",
                category = ItemCategory.ACCESSORIES.name,
                location = "Science Hall, Lecture Room 210",
                date = baseTime - (4 * 24 * 60 * 60 * 1000), // 4 days ago
                imageUrl = "https://images.unsplash.com/photo-1574258495973-f010dfbb5371?w=400&h=400&fit=crop",
                userId = "mock_user_12",
                userName = "Christopher Moore",
                status = ItemStatus.FOUND,
                isActive = true,
                createdAt = baseTime - (4 * 24 * 60 * 60 * 1000),
                updatedAt = baseTime - (4 * 24 * 60 * 60 * 1000)
            ),
            Item(
                id = "found_005",
                title = "Notebook",
                description = "Spiral bound notebook with biology notes. Has name 'Sarah' on the first page.",
                category = ItemCategory.BOOKS.name,
                location = "Life Science Building, Room 102",
                date = baseTime - (5 * 24 * 60 * 60 * 1000), // 5 days ago
                imageUrl = "https://images.unsplash.com/photo-1517842645767-c639042777db?w=400&h=400&fit=crop",
                userId = "mock_user_13",
                userName = "Daniel Garcia",
                status = ItemStatus.FOUND,
                isActive = true,
                createdAt = baseTime - (5 * 24 * 60 * 60 * 1000),
                updatedAt = baseTime - (5 * 24 * 60 * 60 * 1000)
            ),
            Item(
                id = "found_006",
                title = "Gray Hoodie",
                description = "Gray pullover hoodie, size Large. UTA logo on the front.",
                category = ItemCategory.CLOTHING.name,
                location = "Maverick Activities Center, Lounge",
                date = baseTime - (6 * 24 * 60 * 60 * 1000), // 6 days ago
                imageUrl = "https://images.unsplash.com/photo-1556821840-3a63f95609a7?w=400&h=400&fit=crop",
                userId = "mock_user_14",
                userName = "Rachel Thompson",
                status = ItemStatus.FOUND,
                isActive = true,
                createdAt = baseTime - (6 * 24 * 60 * 60 * 1000),
                updatedAt = baseTime - (6 * 24 * 60 * 60 * 1000)
            ),
            Item(
                id = "found_007",
                title = "Set of Keys",
                description = "Keys with Toyota keychain and gym membership card attached.",
                category = ItemCategory.KEYS.name,
                location = "College Park Center, Parking Lot",
                date = baseTime - (8 * 60 * 60 * 1000), // 8 hours ago
                imageUrl = "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400&h=400&fit=crop",
                userId = "mock_user_15",
                userName = "Brandon Harris",
                status = ItemStatus.FOUND,
                isActive = true,
                createdAt = baseTime - (8 * 60 * 60 * 1000),
                updatedAt = baseTime - (8 * 60 * 60 * 1000)
            ),
            Item(
                id = "found_008",
                title = "Graphing Calculator",
                description = "TI-84 Plus CE graphing calculator. Has 'JM' written on the back in marker.",
                category = ItemCategory.ELECTRONICS.name,
                location = "Nedderman Hall, 2nd Floor",
                date = baseTime - (10 * 60 * 60 * 1000), // 10 hours ago
                imageUrl = "https://images.unsplash.com/photo-1587825140708-dfaf72ae4b04?w=400&h=400&fit=crop",
                userId = "mock_user_16",
                userName = "Nicole Martin",
                status = ItemStatus.FOUND,
                isActive = true,
                createdAt = baseTime - (10 * 60 * 60 * 1000),
                updatedAt = baseTime - (10 * 60 * 60 * 1000)
            ),
            Item(
                id = "found_009",
                title = "Black Wallet",
                description = "Black leather wallet, no ID inside. Contains some cash and credit cards.",
                category = ItemCategory.ACCESSORIES.name,
                location = "Fine Arts Building, Cafeteria",
                date = baseTime - (2 * 60 * 60 * 1000), // 2 hours ago
                imageUrl = "https://images.unsplash.com/photo-1627123424574-724758594e93?w=400&h=400&fit=crop",
                userId = "mock_user_17",
                userName = "Tyler Jackson",
                status = ItemStatus.FOUND,
                isActive = true,
                createdAt = baseTime - (2 * 60 * 60 * 1000),
                updatedAt = baseTime - (2 * 60 * 60 * 1000)
            ),
            Item(
                id = "found_010",
                title = "USB Flash Drive",
                description = "32GB SanDisk USB flash drive. Red color with keychain loop.",
                category = ItemCategory.ELECTRONICS.name,
                location = "Central Library, Computer Area",
                date = baseTime - (4 * 60 * 60 * 1000), // 4 hours ago
                imageUrl = "https://images.unsplash.com/photo-1621328393662-6fcd8ed8339f?w=400&h=400&fit=crop",
                userId = "mock_user_18",
                userName = "Stephanie Clark",
                status = ItemStatus.FOUND,
                isActive = true,
                createdAt = baseTime - (4 * 60 * 60 * 1000),
                updatedAt = baseTime - (4 * 60 * 60 * 1000)
            )
        )
    }
}
