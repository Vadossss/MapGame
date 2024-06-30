package com.example.mapgame

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PointF
import android.graphics.drawable.ColorDrawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
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
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.CameraUpdateReason
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.map.RotationType
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.navigation.automotive.NavigationFactory
import com.yandex.mapkit.navigation.automotive.NavigationListener
import com.yandex.runtime.Error
import com.yandex.runtime.image.ImageProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader


class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null
    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private val r = FloatArray(9)
    private val i = FloatArray(9)
    private var azimuth: Float = 0f
    private lateinit var quest1_1: NewQuest
    private lateinit var quest1_2: NewQuest
    private lateinit var quest2_1: NewQuest
    private lateinit var quest2_2: NewQuest
    private lateinit var quest1: Quest
    private lateinit var quest2: Quest


    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mapView: MapView
    private lateinit var locationNow: Point
    private var uri: Uri =
        Uri.parse("yandexnavi://build_route_on_map?lat_from=57.732670&lon_from=40.912260&lat_to=55.76&lon_to=37.64")


    private val topLeft =
        Point(57.731909, 40.914427)  // Пример координат для Москвы (верхний левый угол зоны)
    private val bottomRight =
        Point(57.731334, 40.915704)  // Пример координат для Москвы (нижний правый угол зоны)


    private lateinit var video: VideoView
    private var isFirstResume = true
    private lateinit var btnSkip: Button
    private lateinit var collection: MapObjectCollection
    private lateinit var btnPositionNow: Button

    private var isCheckCameraPosition: Boolean = true

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var cameraList : CameraListener

    private val preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        if (key == getString(R.string.quest1)) {
            updatePlacemarkVisibility(placemark, sharedPreferences.getBoolean(key, false))
            updatePlacemarkVisibility(placemark1, sharedPreferences.getBoolean(key, false))
        }
    }

    private fun updatePlacemarkVisibility(placemark: PlacemarkMapObject?, isVisible: Boolean) {
        placemark?.isVisible = isVisible
    }

    private fun parseJsonInString(name : String): String {
        val stringBuilder = StringBuilder()
        val inputStream = this.assets.open(name)
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))

        bufferedReader.use { reader ->
            var line = reader.readLine()
            while (line != null) {
                stringBuilder.append(line)
                line = reader.readLine()
            }
        }
        Log.d(TAG, stringBuilder.toString())
        return stringBuilder.toString()
    }

    private var checkCompliteGPS : Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.setApiKey("204ee658-86b7-453b-90db-dd6a00cdc770")
        MapKitFactory.initialize(this)
        setContentView(R.layout.activity_main)


        mapView = findViewById(R.id.mapview)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mapView.map.isTiltGesturesEnabled = false
        mapView.map.isZoomGesturesEnabled = false



        val jsonStr = parseJsonInString("customization.json")
        mapView.map.isNightModeEnabled = true
        mapView.map.setMapStyle(jsonStr)

        mapView.map.move(CameraPosition(
            bottomRight, 17.0f, azimuth, 100f
        ))








        //getLastKnownLocation()


        video = findViewById(R.id.videoCom)
        btnSkip = findViewById(R.id.btnSkip)


        btnPositionNow = findViewById(R.id.btnPositionNow)
        btnPositionNow.setOnClickListener {
            isCheckCameraPosition = true
            Log.d(TAG, "")
            updateLocationOnMap(locationNow)
        }




        sharedPreferences = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)


        //checkPreferences()
        val map = mapView.mapWindow.map
//        quest1_2 = NewQuest(this, map, R.array.quest1_2)
//        quest1_2.createNewQuest()
//        quest1_1 = NewQuest(this, map, R.array.quest1_1, quest1_2.placemark)
//        quest1_1.createNewQuest()
//
//        quest2_2 = NewQuest(this, map, R.array.quest2_2)
//        quest2_2.createNewQuest()
//
//        quest2_1 = NewQuest(this, map, R.array.quest2_1, quest2_2.placemark)
//        quest2_1.createNewQuest()

        quest1 = Quest(this, map, R.array.all_quest)
        quest1.initQuest()


//        quest2 = Quest(this, map, R.array.quest2)
//        quest2.createQuest()




        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        allIcon()


        var cameraPositionJob : Job? = null
        val coroutineScope = CoroutineScope(Dispatchers.Main)


        cameraList = object : CameraListener {
            override fun onCameraPositionChanged(
                p0: Map,
                p1: CameraPosition,
                p2: CameraUpdateReason,
                p3: Boolean
            ) {
                if (p2 == CameraUpdateReason.GESTURES) {
                    if (p3) {
                    Log.d(TAG, "CheckCameraPositionfasssssss")
                    if (isCheckCameraPosition)
                        isCheckCameraPosition = false

                    cameraPositionJob?.cancel()
                    cameraPositionJob = coroutineScope.launch {
                        delay(10000)
                        isCheckCameraPosition = true
//                        Log.d(TAG, "CheckCameraPosition")
                    }
                    Log.d(TAG, "CheckCameraPosition: $isCheckCameraPosition")
                    } else {
                        isCheckCameraPosition = false
//                        Log.d(TAG, "CheckCameraPositionfasssssss: $isCheckCameraPosition")
                    }
                }
            }
        }

        mapView.map.addCameraListener(cameraList)

        //sharedPreferences = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        //sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }


    }


    private fun checkPreferences() {
        var questInfo = resources.getStringArray(R.array.quest1_1)
        if (!sharedPreferences.contains(questInfo[0])) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(questInfo[0], true) // Установить изначальное значение для quest1
            editor.apply()
        }
        questInfo = resources.getStringArray(R.array.quest1_2)
        if (!sharedPreferences.contains(questInfo[0])) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(questInfo[0], false) // Установить изначальное значение для quest1
            editor.apply()
        }
        questInfo = resources.getStringArray(R.array.quest2_1)
        if (!sharedPreferences.contains(questInfo[0])) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(questInfo[0], true) // Установить изначальное значение для quest1
            editor.apply()
        }
        questInfo = resources.getStringArray(R.array.quest2_2)
        if (!sharedPreferences.contains(questInfo[0])) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(questInfo[0], false) // Установить изначальное значение для quest1
            editor.apply()
        }

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

    private fun newDialog(nameQuest: Int, place: PlacemarkMapObject?) {
        var questInfo = resources.getStringArray(nameQuest)
        val dialogBinding = layoutInflater.inflate(R.layout.dialog_enemy, null)
        val myDialog = Dialog(this)
        val textQuest = dialogBinding.findViewById<TextView>(R.id.textQuest)
        myDialog.setContentView(dialogBinding)
        myDialog.setCancelable(true)
        myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        textQuest.text = questInfo[1] + "\n" + questInfo[2]
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
            video.setVideoPath("android.resource://" + packageName + "/" + R.raw.video1)

            video.setOnPreparedListener {
                val layoutParams = video.layoutParams
                val d = findViewById<View>(R.id.lay)
                layoutParams.width = d.layoutParams.width
                layoutParams.height = d.layoutParams.height
                video.layoutParams = layoutParams
            }
            video.start()
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

            val editor = sharedPreferences.edit()
            var currentState = sharedPreferences.getBoolean(questInfo[0], false)
            editor.putBoolean(questInfo[0], !currentState)
            editor.apply()
            updatePlacemarkVisibility(place, sharedPreferences.getBoolean(questInfo[0], false))
            Log.d(TAG, "QUEST1 BOOL: " + sharedPreferences.getBoolean(questInfo[0], false).toString())

            if (questInfo.size > 3) {
                currentState = sharedPreferences.getBoolean(questInfo[3], false)
                editor.putBoolean(getString(R.string.quest2), !currentState)
                editor.apply()
                updatePlacemarkVisibility(placemark1, sharedPreferences.getBoolean(questInfo[3], false))
                Log.d(TAG, "QUEST2 BOOL: " + sharedPreferences.getBoolean(questInfo[3], false).toString())
            }

//            currentState = sharedPreferences.getBoolean(getString(R.string.quest2), false)
//            editor.putBoolean(getString(R.string.quest2), !currentState)
//            editor.apply()
//            updatePlacemarkVisibility(placemark1, sharedPreferences.getBoolean(getString(R.string.quest2), false))
//            Log.d(TAG, "QUEST2 BOOL: " + sharedPreferences.getBoolean(getString(R.string.quest2), false).toString())


        }
    }


    @SuppressLint("ClickableViewAccessibility", "InflateParams")
    private val iconTapListen = MapObjectTapListener { _, _ ->
        newDialog(R.array.quest1_1, placemark)
        true
    }

    private val iconTapListen2 = MapObjectTapListener { _, _ ->
        newDialog(R.array.quest1_2, placemark1)
        true
    }
    private var placemark : PlacemarkMapObject? = null
    private var placemark1 : PlacemarkMapObject? = null

    private fun allIcon() {
        val map = mapView.mapWindow.map
        collection = map.mapObjects.addCollection()

//        placemark = collection.addPlacemark().apply {
//            geometry = Point(57.736260, 40.921054)
//            addTapListener(iconTapListen)
//            setText(
//                "Ларец",
//                TextStyle().apply {
//                    size = 15f
//                    placement = TextStyle.Placement.TOP
//                    color = Color.WHITE
//                },
//            )
//            useCompositeIcon().apply {
//                setIcon(
//                    "pin",
//                    ImageProvider.fromResource(this@MainActivity, R.drawable.animad),
//                    IconStyle().apply {
//                        anchor = PointF(0.5f, 1.0f)
//                        scale = 0.3f
//                    }
//                )
//            }
//            isVisible = sharedPreferences.getBoolean(getString(R.string.quest1), false)
//        }
//        placemark1 = collection.addPlacemark().apply {
//            geometry = Point(57.735604, 40.914032)
//            addTapListener(iconTapListen2)
//
//            setText(
//                "Кощей",
//                TextStyle().apply {
//                    size = 15f
//                    placement = TextStyle.Placement.TOP
//                    color = Color.WHITE
//                    outlineColor = Color.BLACK
//                },
//            )
//            useCompositeIcon().apply {
//                setIcon(
//                    ImageProvider.fromResource(this@MainActivity, R.drawable.animad),
//                    IconStyle().apply {
//                        anchor = PointF(0.5f, 1.0f)
//                        scale = 0.3f
//                    }
//                )
//            }
//            isVisible = sharedPreferences.getBoolean(getString(R.string.quest2), false)
//        }

    }

    private fun putUri() {
        val navigation = NavigationFactory.createNavigation(DrivingRouterType.ONLINE)
        navigation.resolveUri(uri.toString())
        Toast.makeText(this, navigation.routes.toString(), Toast.LENGTH_SHORT).show()
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

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && !checkCompliteGPS
        ) {
            startLocationUpdates()
            checkCompliteGPS = true
        }




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
                Log.d(TAG, "Маршрут $fastestRoute")
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

        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
        quest1.resetQuests()
//        updatePlacemarkVisibility(placemark, sharedPreferences.getBoolean(getString(R.string.quest1), false))
//        updatePlacemarkVisibility(placemark1, sharedPreferences.getBoolean(getString(R.string.quest2), false))
//        val s = placemark1?.isVisible
//        if (s != null) {
//            updatePlacemarkVisibility(placemark1, !s)
//        }
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
//        val locationRequest = LocationRequest.create().apply {
//            interval = 1000 // 10 секунд
//            fastestInterval = 1000 // 5 секунд
//            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
//        }
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

        val locationRequest = LocationRequest.create().apply {
            interval = 2000 // 10 секунд
            fastestInterval = 2000 // 5 секунд
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        Log.d(TAG, "startLocationUpdates")
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    locationNow = Point(location.latitude, location.longitude)
                    updateLocationOnMap(locationNow)
                    Log.d(TAG, "startLocationUpdates222222")
                }
            }
        }


        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private lateinit var userLocationMarker: PlacemarkMapObject
    private var isMarkerInitialized = false
    private var oldAzimuth: Float = 0.0f

    @SuppressLint("SuspiciousIndentation")
    private fun updateLocationOnMap(location: Point) {
        val userLocation = Point(location.latitude, location.longitude)
        if (!isMarkerInitialized) {
            val map = mapView.mapWindow.map
            collection = map.mapObjects.addCollection()
            userLocationMarker = collection.addPlacemark().apply {
                geometry = userLocation
                direction = azimuth
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
            userLocationMarker.geometry = userLocation
            userLocationMarker.direction = azimuth
        }
        if (isCheckCameraPosition) {
            mapView.map.move(
                CameraPosition(locationNow, 17.0f, azimuth, 100f),
                Animation(Animation.Type.SMOOTH, 1f),
                null
            )
//            if ((oldAzimuth - azimuth) > 20 && azimuth > 20 && azimuth < 340) {
//                mapView.map.move(
//                    CameraPosition(locationNow, 17.0f, oldAzimuth+20, 100f),
//                    Animation(Animation.Type.SMOOTH, 1f),
//                    null
//                )
//            }
//            else if ((oldAzimuth - azimuth) < -20 && azimuth > 20 && azimuth < 340) {
//                mapView.map.move(
//                    CameraPosition(locationNow, 17.0f, oldAzimuth-20, 100f),
//                    Animation(Animation.Type.SMOOTH, 1f),
//                    null
//                )
//            }
//            else {
//                mapView.map.move(
//                    CameraPosition(locationNow, 17.0f, azimuth, 100f),
//                    Animation(Animation.Type.SMOOTH, 1f),
//                    null
//                )
//            }
            Log.d(TAG, "MoveCamera")
        }
        oldAzimuth = azimuth
    }

    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation() {

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
                            100.0f
                        ),
                        //Animation(Animation.Type.SMOOTH, 3f), null
                    )
//
//                    put()
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
            //Log.d("Azimuth", "Current azimuth: $azimuth")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Implement if sensor accuracy changes are to be handled
    }


}