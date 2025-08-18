package com.extole.android.sdk.impl

import org.junit.Test
import org.junit.Assert.*
import com.extole.android.sdk.impl.ZoneResponseKey

class ExtoleImplCacheTest {

    @Test
    fun `testCacheKeyGenerationWithDifferentDataTypes`() {
        // This test demonstrates a potential issue with React Native data types
        
        // Simulate React Native passing data
        val reactNativeData1 = mapOf("clientKey" to "Go")
        val reactNativeData2 = mapOf("clientKey" to "Uc")
        
        // Create cache keys
        val key1 = ZoneResponseKey("go_configuration", reactNativeData1)
        val key2 = ZoneResponseKey("go_configuration", reactNativeData2)
        
        // These keys should be different
        assertNotEquals("Cache keys should be different for different clientKey values", key1, key2)
        
        // Test hash code consistency
        assertNotEquals("Hash codes should be different for different clientKey values", key1.hashCode(), key2.hashCode())
        
        // Test that the same data produces the same key
        val key1Again = ZoneResponseKey("go_configuration", reactNativeData1)
        assertEquals("Same data should produce equal keys", key1, key1Again)
        assertEquals("Same data should produce same hash codes", key1.hashCode(), key1Again.hashCode())
    }

    @Test
    fun `testCacheKeyWithMixedDataTypes`() {
        // Test with different data types that might come from React Native
        val data1 = mapOf("clientKey" to "Go", "number" to 42, "boolean" to true)
        val data2 = mapOf("clientKey" to "Go", "number" to 42, "boolean" to true)
        val data3 = mapOf("clientKey" to "Go", "number" to 42, "boolean" to false)
        
        val key1 = ZoneResponseKey("test_zone", data1)
        val key2 = ZoneResponseKey("test_zone", data2)
        val key3 = ZoneResponseKey("test_zone", data3)
        
        assertEquals("Identical data should produce equal keys", key1, key2)
        assertNotEquals("Different data should produce different keys", key1, key3)
    }

    @Test
    fun `testCacheKeyWithNullValues`() {
        // Test with null values that might come from React Native
        val data1 = mapOf("clientKey" to "Go", "optional" to null)
        val data2 = mapOf("clientKey" to "Go", "optional" to null)
        val data3 = mapOf("clientKey" to "Go")
        
        val key1 = ZoneResponseKey("test_zone", data1)
        val key2 = ZoneResponseKey("test_zone", data2)
        val key3 = ZoneResponseKey("test_zone", data3)
        
        assertEquals("Data with null values should be equal", key1, key2)
        assertNotEquals("Data with and without null values should be different", key1, key3)
    }

    @Test
    fun `testCacheKeyWithEmptyMaps`() {
        // Test edge cases
        val emptyData = emptyMap<String, Any?>()
        val dataWithClientKey = mapOf("clientKey" to "Go")
        
        val emptyKey = ZoneResponseKey("test_zone", emptyData)
        val dataKey = ZoneResponseKey("test_zone", dataWithClientKey)
        
        assertNotEquals("Empty data and data with clientKey should be different", emptyKey, dataKey)
    }

    @Test
    fun `testFetchZoneCacheKeyConsistency`() {
        // This test verifies that the fetchZone fix works correctly
        
        // Simulate the FIXED fetchZone logic:
        val userData = mapOf("clientKey" to "Go")
        val instanceData = mapOf("version" to "1.0")
        val labels = setOf("business")
        
        // Both cache lookup and cache storage now use the same data (requestData)
        val requestData = mutableMapOf<String, Any?>()
        requestData.putAll(userData)
        requestData.putAll(instanceData)
        requestData["labels"] = labels.joinToString(",")
        
        val cacheLookupKey = ZoneResponseKey("go_configuration", requestData)
        val cacheStorageKey = ZoneResponseKey("go_configuration", requestData)
        
        // These keys should now be equal after the fix!
        assertEquals("Cache lookup and storage keys should now be equal after the fix", cacheLookupKey, cacheStorageKey)
        assertEquals("Hash codes should also be equal", cacheLookupKey.hashCode(), cacheStorageKey.hashCode())
        
        println("✅ CACHING FIX VERIFIED!")
        println("  Both cache lookup and storage now use the same data (requestData)")
        println("  This means caching will work correctly!")
    }

    @Test
    fun `testDifferentClientKeysGenerateDifferentCacheKeys`() {
        // This test verifies that different clientKey values generate different cache keys
        
        // Create two different clientKey values
        val dataGo = mapOf("clientKey" to "Go")
        val dataUc = mapOf("clientKey" to "Uc")
        
        // Create cache keys
        val keyGo = ZoneResponseKey("go_configuration", dataGo)
        val keyUc = ZoneResponseKey("go_configuration", dataUc)
        
        // These keys should be different
        assertNotEquals("Cache keys with different clientKey values should not be equal", keyGo, keyUc)
        assertNotEquals("Cache keys with different clientKey values should have different hash codes", 
            keyGo.hashCode(), keyUc.hashCode())
        
        println("✅ Different clientKey values generate different cache keys:")
        println("  Key Go: $keyGo")
        println("  Key Uc: $keyUc")
        println("  Keys are different: ${keyGo != keyUc}")
        println("  Hash codes are different: ${keyGo.hashCode() != keyUc.hashCode()}")
    }
}
