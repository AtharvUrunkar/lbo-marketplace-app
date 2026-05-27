package com.example.lbo_marketplace.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import com.google.android.gms.location.LocationServices
import java.util.Locale
import com.example.lbo_marketplace.auth.AuthViewModel

/**
 * =========================================================
 * 🔥 LOCATION DATA MODEL
 * =========================================================
 */
data class LocationData(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val city: String = "",
    val area: String = "",
    val fullAddress: String = ""
)

/**
 * =========================================================
 * 🔥 GET ADDRESS FROM LAT LNG
 * =========================================================
 */
fun getAddressFromLocation(
    context: Context,
    latitude: Double,
    longitude: Double
): LocationData {
    return try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)

        if (!addresses.isNullOrEmpty()) {
            val address = addresses[0]
            LocationData(
                latitude = latitude,
                longitude = longitude,
                city = address.locality ?: "",
                area = address.subLocality ?: "",
                fullAddress = address.getAddressLine(0) ?: ""
            )
        } else {
            LocationData(latitude = latitude, longitude = longitude)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        LocationData(latitude = latitude, longitude = longitude)
    }
}

/**
 * =========================================================
 * 🔥 GET LAT LNG FROM ADDRESS (FALLBACK GEOCODING)
 * =========================================================
 * 
 * Resolves a visual text address (e.g., city, area, pincode) into GPS coordinates.
 */
fun getCoordinatesFromAddress(
    context: Context,
    addressString: String
): Pair<Double, Double>? {
    if (addressString.isBlank()) return null
    return try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocationName(addressString, 1)
        if (!addresses.isNullOrEmpty()) {
            val address = addresses[0]
            Pair(address.latitude, address.longitude)
        } else {
            null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * =========================================================
 * 🔥 FALLBACK TO FIRESTORE / ADDRESS LOCATION
 * =========================================================
 * 
 * Fetches user profile coordinates, falling back to geocoding if coordinates are 0.0.
 */
fun fallbackToAddressLocation(
    userId: String?,
    authViewModel: AuthViewModel,
    context: Context,
    onResult: (Double, Double) -> Unit
) {
    if (userId == null) {
        onResult(0.0, 0.0)
        return
    }
    authViewModel.fetchUserLocation(userId) { lat, lng, city, area, address ->
        if (lat != 0.0 && lng != 0.0) {
            onResult(lat, lng)
        } else {
            val addressParts = listOfNotNull(
                address.trim().ifBlank { null },
                area.trim().ifBlank { null },
                city.trim().ifBlank { null }
            )
            val addressQuery = addressParts.joinToString(", ")
            
            if (addressQuery.isNotEmpty()) {
                val coords = getCoordinatesFromAddress(context, addressQuery)
                if (coords != null) {
                    onResult(coords.first, coords.second)
                } else {
                    onResult(0.0, 0.0)
                }
            } else {
                onResult(0.0, 0.0)
            }
        }
    }
}

/**
 * =========================================================
 * 🔥 CALCULATE DISTANCE
 * =========================================================
 */
fun calculateDistance(
    startLat: Double,
    startLng: Double,
    endLat: Double,
    endLng: Double
): Float {
    val results = FloatArray(1)
    Location.distanceBetween(startLat, startLng, endLat, endLng, results)
    return results[0]
}

/**
 * =========================================================
 * 🔥 FORMAT DISTANCE
 * =========================================================
 */
fun formatDistance(distanceInMeters: Float): String {
    return if (distanceInMeters < 1000) {
        "${distanceInMeters.toInt()} m"
    } else {
        String.format(Locale.US, "%.1f km", distanceInMeters / 1000f)
    }
}

/**
 * =========================================================
 * 🔥 FETCH LOCATION
 * =========================================================
 */
@SuppressLint("MissingPermission")
fun fetchProviderLocation(
    context: Context,
    onResult: (Double, Double) -> Unit
) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    fusedLocationClient.lastLocation
        .addOnSuccessListener { location: Location? ->
            if (location != null) {
                onResult(location.latitude, location.longitude)
            } else {
                onResult(0.0, 0.0)
            }
        }
        .addOnFailureListener {
            onResult(0.0, 0.0)
        }
}
