package com.pandora.core.ai.context

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton
import java.util.*

/**
 * Location Awareness System
 * Provides intelligent location-based context understanding
 */
@Singleton
class LocationAwareness @Inject constructor(
    private val context: Context
) {
    
    companion object {
        private const val TAG = "LocationAwareness"
        private const val LOCATION_UPDATE_INTERVAL = 300000L // 5 minutes
        private const val MAX_LOCATION_HISTORY = 100
    }
    
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val locationHistory = mutableListOf<LocationRecord>()
    private val knownLocations = mutableMapOf<String, KnownLocation>()
    
    /**
     * Get enhanced location context
     */
    fun getEnhancedLocationContext(): Flow<EnhancedLocationContext> = flow {
        try {
            val currentLocation = getCurrentLocation()
            val locationType = determineLocationType(currentLocation)
            val locationContext = getLocationContext(currentLocation, locationType)
            val nearbyPlaces = getNearbyPlaces(currentLocation)
            val locationPatterns = analyzeLocationPatterns()
            val locationBasedSuggestions = getLocationBasedSuggestions(locationType, currentLocation)
            
            val enhancedContext = EnhancedLocationContext(
                currentLocation = currentLocation,
                locationType = locationType,
                locationContext = locationContext,
                nearbyPlaces = nearbyPlaces,
                locationPatterns = locationPatterns,
                locationBasedSuggestions = locationBasedSuggestions,
                isAtHome = isAtHome(currentLocation),
                isAtWork = isAtWork(currentLocation),
                isTraveling = isTraveling(currentLocation),
                isInTransit = isInTransit(currentLocation),
                locationAccuracy = getLocationAccuracy(currentLocation),
                lastLocationUpdate = getLastLocationUpdate(),
                locationHistory = locationHistory.takeLast(10)
            )
            
            emit(enhancedContext)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error getting enhanced location context", e)
            emit(EnhancedLocationContext.createEmpty())
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Get current location
     */
    private fun getCurrentLocation(): Location? {
        return try {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) 
                == PackageManager.PERMISSION_GRANTED) {
                
                @Suppress("MissingPermission")
                val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (lastKnownLocation != null) {
                    // Record location in history
                    recordLocation(lastKnownLocation)
                    lastKnownLocation
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error getting current location", e)
            null
        }
    }
    
    /**
     * Determine location type
     */
    private fun determineLocationType(location: Location?): LocationType {
        if (location == null) return LocationType.UNKNOWN
        
        // Check against known locations
        knownLocations.forEach { (name, knownLocation) ->
            if (isLocationNearby(location, knownLocation)) {
                return knownLocation.type
            }
        }
        
        // Default classification based on time and patterns
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour in 9..17 -> LocationType.WORK
            hour in 22..6 -> LocationType.HOME
            else -> LocationType.UNKNOWN
        }
    }
    
    /**
     * Get location context
     */
    private fun getLocationContext(location: Location?, locationType: LocationType): LocationContext {
        if (location == null) return LocationContext.createUnknown()
        
        return LocationContext(
            latitude = location.latitude,
            longitude = location.longitude,
            accuracy = location.accuracy,
            locationType = locationType,
            isMoving = false, // Would need additional logic to determine movement
            speed = location.speed
        )
    }
    
    /**
     * Get nearby places
     */
    private fun getNearbyPlaces(location: Location?): List<NearbyPlace> {
        if (location == null) return emptyList()
        
        // This would typically use Places API
        // For now, return placeholder places
        return listOf(
            NearbyPlace(
                name = "Coffee Shop",
                type = "restaurant",
                distance = 100.0,
                rating = 4.5f,
                isOpen = true
            ),
            NearbyPlace(
                name = "Gas Station",
                type = "gas_station",
                distance = 200.0,
                rating = 4.0f,
                isOpen = true
            ),
            NearbyPlace(
                name = "Pharmacy",
                type = "pharmacy",
                distance = 300.0,
                rating = 4.2f,
                isOpen = true
            )
        )
    }
    
    /**
     * Analyze location patterns
     */
    private fun analyzeLocationPatterns(): LocationPatterns {
        val recentLocations = locationHistory.takeLast(50)
        val homeLocations = recentLocations.filter { isAtHome(it.location) }
        val workLocations = recentLocations.filter { isAtWork(it.location) }
        
        return LocationPatterns(
            mostFrequentLocation = getMostFrequentLocation(recentLocations),
            homeFrequency = homeLocations.size.toFloat() / recentLocations.size,
            workFrequency = workLocations.size.toFloat() / recentLocations.size,
            averageTravelDistance = calculateAverageTravelDistance(recentLocations),
            travelPatterns = analyzeTravelPatterns(recentLocations),
            locationDiversity = calculateLocationDiversity(recentLocations)
        )
    }
    
    /**
     * Get location-based suggestions
     */
    private fun getLocationBasedSuggestions(locationType: LocationType, location: Location?): List<LocationBasedSuggestion> {
        val suggestions = mutableListOf<LocationBasedSuggestion>()
        
        when (locationType) {
            LocationType.HOME -> {
                suggestions.add(LocationBasedSuggestion(
                    "You're at home - time to relax and unwind",
                    "home_comfort",
                    0.8f
                ))
            }
            LocationType.WORK -> {
                suggestions.add(LocationBasedSuggestion(
                    "You're at work - focus on your tasks",
                    "work_focus",
                    0.9f
                ))
            }
            LocationType.TRAVEL -> {
                suggestions.add(LocationBasedSuggestion(
                    "You're traveling - check your route and schedule",
                    "travel_assistance",
                    0.7f
                ))
            }
            LocationType.UNKNOWN -> {
                suggestions.add(LocationBasedSuggestion(
                    "You're in a new location - explore safely",
                    "new_location",
                    0.5f
                ))
            }
        }
        
        // Add time-based suggestions
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 7..9 -> {
                suggestions.add(LocationBasedSuggestion(
                    "Morning commute - check traffic conditions",
                    "morning_commute",
                    0.6f
                ))
            }
            in 17..19 -> {
                suggestions.add(LocationBasedSuggestion(
                    "Evening commute - plan your route home",
                    "evening_commute",
                    0.6f
                ))
            }
        }
        
        return suggestions
    }
    
    /**
     * Check if at home
     */
    private fun isAtHome(location: Location?): Boolean {
        if (location == null) return false
        
        val homeLocation = knownLocations["home"]
        return homeLocation != null && isLocationNearby(location, homeLocation)
    }
    
    /**
     * Check if at work
     */
    private fun isAtWork(location: Location?): Boolean {
        if (location == null) return false
        
        val workLocation = knownLocations["work"]
        return workLocation != null && isLocationNearby(location, workLocation)
    }
    
    /**
     * Check if traveling
     */
    private fun isTraveling(location: Location?): Boolean {
        if (location == null) return false
        
        val recentLocations = locationHistory.takeLast(5)
        if (recentLocations.size < 3) return false
        
        val totalDistance = recentLocations.zipWithNext { current, next ->
            current.location.distanceTo(next.location)
        }.sum()
        
        return totalDistance > 1000.0 // More than 1km in recent locations
    }
    
    /**
     * Check if in transit
     */
    private fun isInTransit(location: Location?): Boolean {
        if (location == null) return false
        
        val speed = location.speed
        return speed > 5.0 && speed < 50.0 // Walking to driving speed
    }
    
    /**
     * Get location accuracy
     */
    private fun getLocationAccuracy(location: Location?): Float {
        return location?.accuracy ?: 0f
    }
    
    /**
     * Get last location update
     */
    private fun getLastLocationUpdate(): Long {
        return locationHistory.lastOrNull()?.timestamp ?: 0L
    }
    
    /**
     * Record location
     */
    private fun recordLocation(location: Location) {
        val record = LocationRecord(
            location = location,
            timestamp = System.currentTimeMillis()
        )
        
        locationHistory.add(record)
        if (locationHistory.size > MAX_LOCATION_HISTORY) {
            locationHistory.removeAt(0)
        }
    }
    
    /**
     * Check if location is nearby
     */
    private fun isLocationNearby(location: Location, knownLocation: KnownLocation): Boolean {
        val distance = location.distanceTo(knownLocation.location)
        return distance < knownLocation.radius
    }
    
    /**
     * Get most frequent location
     */
    private fun getMostFrequentLocation(locations: List<LocationRecord>): String {
        // This would typically cluster locations and find the most frequent
        return "home"
    }
    
    /**
     * Calculate average travel distance
     */
    private fun calculateAverageTravelDistance(locations: List<LocationRecord>): Double {
        if (locations.size < 2) return 0.0
        
        val totalDistance = locations.zipWithNext { current, next ->
            current.location.distanceTo(next.location)
        }.sum()
        
        return totalDistance / (locations.size - 1).toDouble()
    }
    
    /**
     * Analyze travel patterns
     */
    private fun analyzeTravelPatterns(locations: List<LocationRecord>): List<TravelPattern> {
        // This would analyze travel patterns
        return emptyList()
    }
    
    /**
     * Calculate location diversity
     */
    private fun calculateLocationDiversity(locations: List<LocationRecord>): Float {
        // This would calculate how diverse the locations are
        return 0.5f
    }
    
    /**
     * Add known location
     */
    fun addKnownLocation(name: String, location: Location, type: LocationType, radius: Float = 100f) {
        knownLocations[name] = KnownLocation(
            name = name,
            location = location,
            type = type,
            radius = radius
        )
    }
    
    /**
     * Remove known location
     */
    fun removeKnownLocation(name: String) {
        knownLocations.remove(name)
    }
    
    /**
     * Get known locations
     */
    fun getKnownLocations(): Map<String, KnownLocation> {
        return knownLocations.toMap()
    }
}

/**
 * Enhanced location context
 */
data class EnhancedLocationContext(
    val currentLocation: Location?,
    val locationType: LocationType,
    val locationContext: LocationContext,
    val nearbyPlaces: List<NearbyPlace>,
    val locationPatterns: LocationPatterns,
    val locationBasedSuggestions: List<LocationBasedSuggestion>,
    val isAtHome: Boolean,
    val isAtWork: Boolean,
    val isTraveling: Boolean,
    val isInTransit: Boolean,
    val locationAccuracy: Float,
    val lastLocationUpdate: Long,
    val locationHistory: List<LocationRecord>
) {
    companion object {
        fun createEmpty() = EnhancedLocationContext(
            currentLocation = null,
            locationType = LocationType.UNKNOWN,
            locationContext = LocationContext.createUnknown(),
            nearbyPlaces = emptyList(),
            locationPatterns = LocationPatterns.createEmpty(),
            locationBasedSuggestions = emptyList(),
            isAtHome = false,
            isAtWork = false,
            isTraveling = false,
            isInTransit = false,
            locationAccuracy = 0f,
            lastLocationUpdate = 0L,
            locationHistory = emptyList()
        )
    }
}

/**
 * Location record
 */
data class LocationRecord(
    val location: Location,
    val timestamp: Long
)

/**
 * Known location
 */
data class KnownLocation(
    val name: String,
    val location: Location,
    val type: LocationType,
    val radius: Float
)

/**
 * Nearby place
 */
data class NearbyPlace(
    val name: String,
    val type: String,
    val distance: Double,
    val rating: Float,
    val isOpen: Boolean
)

/**
 * Location patterns
 */
data class LocationPatterns(
    val mostFrequentLocation: String,
    val homeFrequency: Float,
    val workFrequency: Float,
    val averageTravelDistance: Double,
    val travelPatterns: List<TravelPattern>,
    val locationDiversity: Float
) {
    companion object {
        fun createEmpty() = LocationPatterns(
            mostFrequentLocation = "home",
            homeFrequency = 0.5f,
            workFrequency = 0.3f,
            averageTravelDistance = 0.0,
            travelPatterns = emptyList(),
            locationDiversity = 0.5f
        )
    }
}

/**
 * Travel pattern
 */
data class TravelPattern(
    val from: String,
    val to: String,
    val frequency: Int,
    val averageDuration: Long
)

/**
 * Location-based suggestion
 */
data class LocationBasedSuggestion(
    val suggestion: String,
    val category: String,
    val confidence: Float
)
