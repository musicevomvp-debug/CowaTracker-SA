package com.cowatracker.app

import android.Manifest
import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private var totalDistance = 0.0
    private var lastLocation: Location? = null
    private var isTracking = false

    companion object {
        const val CHANNEL_ID = "LocationServiceChannel"
        const val ACTION_START_TRACKING = "START_TRACKING"
        const val ACTION_STOP_TRACKING = "STOP_TRACKING"

        private var currentDistance = 0.0

        fun getCurrentDistance(): Double = currentDistance
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
        setupLocationCallback()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TRACKING -> startTracking()
            ACTION_STOP_TRACKING -> stopTracking()
        }
        return START_STICKY
    }

    private fun startTracking() {
        if (isTracking) return

        isTracking = true
        totalDistance = 0.0
        currentDistance = 0.0
        lastLocation = null

        startForeground(1, createNotification())

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000
        ).apply {
            setMinUpdateIntervalMillis(5000)
            setWaitForAccurateLocation(true)
        }.build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    private fun stopTracking() {
        isTracking = false
        fusedLocationClient.removeLocationUpdates(locationCallback)
        stopForeground(true)
        stopSelf()
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    updateDistance(location)
                    updateNotification()
                }
            }
        }
    }

    private fun updateDistance(newLocation: Location) {
        lastLocation?.let { last ->
            val distance = last.distanceTo(newLocation)

            if (distance > 10 && distance < 500) {
                totalDistance += distance
                currentDistance = totalDistance / 1000.0
            }
        }
        lastLocation = newLocation
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Location Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Tracks your delivery distance"
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("CowaTracker - Tracking Active")
            .setContentText(String.format("Distance: %.2f km", currentDistance))
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification() {
        val notification = createNotification()
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(1, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}