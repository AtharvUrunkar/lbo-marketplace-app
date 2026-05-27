package com.example.lbo_marketplace.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import com.google.android.gms.location.LocationServices
import java.util.Locale

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
