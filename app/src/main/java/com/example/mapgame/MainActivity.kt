package com.example.mapgame

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PointF
import android.graphics.drawable.ColorDrawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.directions.driving.DrivingRoute
import com.yandex.mapkit.directions.driving.DrivingRouterType
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.map.RotationType
import com.yandex.mapkit.map.TextStyle
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.navigation.automotive.NavigationFactory
import com.yandex.mapkit.navigation.automotive.NavigationListener
import com.yandex.runtime.Error
import com.yandex.runtime.image.ImageProvider


class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null
    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private val r = FloatArray(9)
    private val i = FloatArray(9)
    private var azimuth: Float = 0f




    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mapView: MapView
    private lateinit var locationNow: Point
    var uri: Uri =
        Uri.parse("yandexnavi://build_route_on_map?lat_from=57.732670&lon_from=40.912260&lat_to=55.76&lon_to=37.64")


    private val topLeft = Point(57.731909, 40.914427)  // Пример координат для Москвы (верхний левый угол зоны)
    private val bottomRight = Point(57.731334, 40.915704)  // Пример координат для Москвы (нижний правый угол зоны)


    private lateinit var video: VideoView
    private var isFirstResume = true
    lateinit var btnSkip: Button
    private lateinit var collection: MapObjectCollection


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.setApiKey("204ee658-86b7-453b-90db-dd6a00cdc770")
        MapKitFactory.initialize(this)
        setContentView(R.layout.activity_main)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLastKnownLocation()
        mapView = findViewById(R.id.mapview)
        mapView.map.isTiltGesturesEnabled = false
        mapView.map.isZoomGesturesEnabled = false
        video = findViewById(R.id.videoCom)
        btnSkip = findViewById(R.id.btnSkip)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }


//
//        findViewById<Button>(R.id.button_focus_polygon).apply {
//            setOnClickListener {
//                val geometry = Geometry.fromPolygon(polygonMapObject.geometry)
//                // Focus camera on polygon
//                map.move(map.cameraPosition(geometry))
//            }
//        }
//
//        findViewById<Button>(R.id.button_geometry).apply {
//            setOnClickListener {
//                // Turned off/on visibility
//                isShowGeometryOnMap = !isShowGeometryOnMap
//                text = "${if (isShowGeometryOnMap) "Off" else "On"} geometry"
//                collection.traverse(geometryVisibilityVisitor)
//            }
//        }
        startLocationUpdates()

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        allIcon()

    }
    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView.onStart()

    }

    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }



    @SuppressLint("ClickableViewAccessibility")
    private val iconTapListen = MapObjectTapListener { _, _ ->
        Toast.makeText(this, "Оно работает", Toast.LENGTH_SHORT).show()
        val dialogBinding = layoutInflater.inflate(R.layout.dialog_enemy, null)
        val myDialog = Dialog(this)
        myDialog.setContentView(dialogBinding)
        myDialog.setCancelable(true)
        myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        myDialog.show()

        val btnClose = findViewById<ImageView>(R.id.btnClose)
        btnClose.setOnClickListener {
            video.stopPlayback()
            video.visibility = View.INVISIBLE
            btnClose.visibility = View.INVISIBLE
        }

        val btnBattle = dialogBinding.findViewById<Button>(R.id.btnBattle)
        btnBattle.setOnClickListener {
            myDialog.dismiss()
            video.visibility = View.VISIBLE
            video.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.video1)

            video.setOnPreparedListener { mp ->
                val videoWidth = mp.videoWidth
                val videoHeight = mp.videoHeight

                val videoViewWidth = video.width
                val videoViewHeight = video.height

                val layoutParams = video.layoutParams

                // Вычислите коэффициенты масштабирования
                val widthRatio = videoViewWidth.toFloat() / videoWidth
                val heightRatio = videoViewHeight.toFloat() / videoHeight

                // Примените минимальный коэффициент масштабирования для масштабирования видео
                val scale = widthRatio.coerceAtMost(heightRatio)

                val d = findViewById<View>(R.id.lay)
                layoutParams.width = d.layoutParams.width
                layoutParams.height = d.layoutParams.height

                video.layoutParams = layoutParams
            }

            video.setOnCompletionListener {
                video.stopPlayback()
                video.visibility = View.INVISIBLE
                btnClose.visibility = View.INVISIBLE
            }

            video.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    if (btnClose.visibility == View.INVISIBLE)
                        btnClose.visibility = View.VISIBLE
                    else
                        btnClose.visibility = View.INVISIBLE
                }
                true
            }

            video.start()
        }
        true
    }

    fun allIcon() {
        val map = mapView.mapWindow.map
        collection = map.mapObjects.addCollection()
        val placemark = collection.addPlacemark().apply {
            geometry = Point(57.736260, 40.921054)
            addTapListener(iconTapListen)
            // Set text near the placemark with the custom TextStyle

            setText(
                "Смэрть",
                TextStyle().apply {
                    size = 10f
                    placement = TextStyle.Placement.TOP
                    offset = 1f
                },
            )
        }
        placemark.useCompositeIcon().apply {
            setIcon(
                "pin",
                ImageProvider.fromResource(this@MainActivity, R.drawable.animad),
                IconStyle().apply {
                    anchor = PointF(0.5f, 1.0f)
                    scale = 0.3f
                }
            )
        }
    }

    private fun putUri() {
        val navigation = NavigationFactory.createNavigation(DrivingRouterType.ONLINE)
        navigation.resolveUri(uri.toString())
        Toast.makeText(this,navigation.routes.toString(), Toast.LENGTH_SHORT).show()
        val nl = object : NavigationListener {



            override fun onRoutesRequested(p0: MutableList<RequestPoint>) {
                TODO("Not yet implemented")
            }

            override fun onAlternativesRequested(p0: DrivingRoute) {
                TODO("Not yet implemented")
            }

            override fun onUriResolvingRequested(p0: String) {
                TODO("Not yet implemented")
            }

            override fun onRoutesBuilt() {
                TODO("Not yet implemented")
            }

            override fun onRoutesRequestError(p0: Error) {
                TODO("Not yet implemented")
            }

            override fun onResetRoutes() {
                TODO("Not yet implemented")
            }

        }
    }

    override fun onResume() {
        super.onResume()

        accelerometer?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
        magnetometer?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }

        if (isFirstResume) {
            isFirstResume = false
            return
        }
    }

    private fun put() {
        val navigation = NavigationFactory.createNavigation(DrivingRouterType.COMBINED)
        val requestPoints = listOf(
            RequestPoint(Point(57.732712, 40.912036), RequestPointType.WAYPOINT, null, null),
            RequestPoint(Point(57.731272, 40.922436), RequestPointType.WAYPOINT, null, null),
            RequestPoint(Point(25.189279, 55.282246), RequestPointType.VIAPOINT, null, null),
            RequestPoint(Point(25.196605, 55.280940), RequestPointType.WAYPOINT, null, null),
        )
        navigation.requestRoutes(
            requestPoints,
            navigation.guidance.location?.heading,
            1,
        )
//        Toast.makeText(this, navigation.routes.toString(), Toast.LENGTH_SHORT).show()

        val navigationListener = object : NavigationListener {
            override fun onRoutesRequested(p0: MutableList<RequestPoint>) {
                TODO("Not yet implemented")
            }

            override fun onAlternativesRequested(p0: DrivingRoute) {
                TODO("Not yet implemented")
            }

            override fun onUriResolvingRequested(p0: String) {
                TODO("Not yet implemented")
            }

            override fun onRoutesBuilt() {
                val routes = navigation.routes
                val fastestRoute = routes[0]
                Log.d(TAG, "Маршрут " + fastestRoute.toString())
                // Routes received successfully ...
            }

            override fun onRoutesRequestError(p0: com.yandex.runtime.Error) {
                return


                // Логируем ошибку для отладки

            }

            override fun onResetRoutes() {
                TODO("Not yet implemented")
            }

            // Override other listener's methods
        }

// You have to subscribe to Navigation before calling the route/alternative request
        navigation.addListener(navigationListener)
        isFirstResume = false
    }

    @SuppressLint("ClickableViewAccessibility")
    fun click(view: View) {
////        getLastKnownLocation()
////        put()
//        video.visibility = View.VISIBLE
//        video.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.video1)
////        val media : MediaController = MediaController(this)
////        media.setAnchorView(video)
////        video.setMediaController(media)
//        video.setOnPreparedListener { mp ->
//            val videoWidth = mp.videoWidth
//            val videoHeight = mp.videoHeight
//
//            val videoViewWidth = video.width
//            val videoViewHeight = video.height
//
//            val layoutParams = video.layoutParams
//
//            // Вычислите коэффициенты масштабирования
//            val widthRatio = videoViewWidth.toFloat() / videoWidth
//            val heightRatio = videoViewHeight.toFloat() / videoHeight
//
//            // Примените минимальный коэффициент масштабирования для масштабирования видео
//            val scale = widthRatio.coerceAtMost(heightRatio)
//
//            layoutParams.width = (videoWidth * scale).toInt()
//            layoutParams.height = (videoHeight * scale).toInt()
//
//            video.layoutParams = layoutParams
//        }
//
//        video.setOnCompletionListener {
//            video.stopPlayback()
//            video.visibility = View.INVISIBLE
//            btnSkip.visibility = View.INVISIBLE
//        }
//
//        video.setOnTouchListener { _, event ->
//            if (event.action == MotionEvent.ACTION_DOWN) {
////                    video.stopPlayback()
////                    video.visibility = View.INVISIBL
//                if (btnSkip.visibility == View.INVISIBLE)
//                    btnSkip.visibility = View.VISIBLE
//                else
//                    btnSkip.visibility = View.INVISIBLE
//            }
//            true
//        }
//
//        video.start()
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 3000 // 10 секунд
            fastestInterval = 5000 // 5 секунд
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    updateLocationOnMap(location)
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }
    private lateinit var userLocationMarker: PlacemarkMapObject
    private var isMarkerInitialized = false

    @SuppressLint("SuspiciousIndentation")
    private fun updateLocationOnMap(location: Location) {
        val userLocation = Point(location.latitude, location.longitude)

        if (!isMarkerInitialized) {
            //userLocationMarker = mapView.map.mapObjects.addPlacemark(userLocation)
            val map = mapView.mapWindow.map
            collection = map.mapObjects.addCollection()
            userLocationMarker = collection.addPlacemark().apply {
                geometry = locationNow
                setIcon(
                    ImageProvider.fromResource(this@MainActivity, R.drawable.tree),
                    IconStyle().apply {
                        anchor = PointF(0.5f, 0.5f)
                        rotationType = RotationType.ROTATE
                        flat = true
                        scale = 0.6f

                    }
                )
            }
            isMarkerInitialized = true
        } else {
            userLocationMarker.geometry = locationNow
        }

        mapView.map.move(
            CameraPosition(locationNow, 17.0f, azimuth, 100f),
            Animation(Animation.Type.SMOOTH, 1f),
            null
        )
    }

    private fun getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        CameraPosition()
        fusedLocationClient.lastLocation
            .addOnSuccessListener(this) { location ->
                if (location != null) {
                    Log.d(TAG, "Latitude: ${location.latitude}, Longitude: ${location.longitude}")
                    locationNow = Point(location.latitude, location.longitude)
                    mapView.map.move(
                        CameraPosition(
                            locationNow,
                            17.0f,
                            azimuth,
                            100.0f),
                        Animation(Animation.Type.SMOOTH, 3f), null)

                    put()
                } else {
                    Log.d(TAG, "Last known location is null.")
                }
            }
            .addOnFailureListener(this) { e ->
                Log.e(TAG, "Error getting last known location", e)
            }
    }



    fun isUserInZone(userLocation: Point, topLeft: Point, bottomRight: Point): Boolean {
        return userLocation.latitude in bottomRight.latitude..topLeft.latitude &&
                userLocation.longitude in topLeft.longitude..bottomRight.longitude
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission granted, proceed with location retrieval
                    getLastKnownLocation()
                } else {
                    // Permission denied, handle the case
                    Log.d(TAG, "Location permission denied")
                }
                return
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 100
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                System.arraycopy(event.values, 0, gravity, 0, event.values.size)
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                System.arraycopy(event.values, 0, geomagnetic, 0, event.values.size)
            }
        }

        if (SensorManager.getRotationMatrix(r, i, gravity, geomagnetic)) {
            val orientation = FloatArray(3)
            SensorManager.getOrientation(r, orientation)
            azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
            azimuth = (azimuth + 360) % 360
            Log.d("Azimuth", "Current azimuth: $azimuth")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Implement if sensor accuracy changes are to be handled
    }


}