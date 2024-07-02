package com.example.mapgame

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import androidx.core.app.ActivityCompat

class LocationTracker(private val context: Context, private val locationListener: LocationListener) {
    private var locationManager: LocationManager? = null

    init {
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    @SuppressLint("MissingPermission")
    fun startTracking() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Запросите разрешения у пользователя
            return
        }
        locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10f, locationListener)
    }

    fun stopTracking() {
        locationManager?.removeUpdates(locationListener)
    }
}