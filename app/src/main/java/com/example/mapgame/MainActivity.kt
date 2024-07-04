package com.example.mapgame

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog.show
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
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.size
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.map.RotationType
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.navigation.automotive.NavigationFactory
import com.yandex.mapkit.navigation.automotive.NavigationListener
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
    private lateinit var quest: Quest
    private var iconIndex: Int = 10

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mapView: MapView
    private lateinit var locationNow: Point

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




    private fun parseJsonInString(name: String): String {
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

    fun clickSoundbtn(view: View) {
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
//        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
//        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, )
//        if(isChecked){
//            val audioManager =            (AudioManager)getSystemService(Context.AUDIO_SERVICE);
//            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
//        }else{
//            audioManager =    (AudioManager)getSystemService(Context.AUDIO_SERVICE);
//            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
//        }
    }

    private fun openAchievementsWindow() {
        val achievementWindow = LayoutInflater.from(this).inflate(R.layout.achievement_list, null)
        val myDialog = Dialog(this)
        //val textQuest = achievementWindow.findViewById<TextView>(R.id.textQuest)
        myDialog.setContentView(achievementWindow)
        myDialog.setCancelable(true)
        myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        myDialog.window?.setWindowAnimations(R.style.dialog_animation_fade)
        val achieveList = achievementWindow.findViewById<RecyclerView>(R.id.list)
        achieveList.layoutManager = LinearLayoutManager(this)
        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.item_spacing)
        achieveList.addItemDecoration(SpaceItemDecoration(spacingInPixels))
        val achievements : MutableList<Achievement> = mutableListOf()
        val info = resources.obtainTypedArray(R.array.all_quest)
        for (i in 0 until info.length()) {
            Log.d(TAG, "Имя: "+ info.length())
            val index = info.getResourceId(i, -1)
            val questArray = resources.obtainTypedArray(index)
            for (j in 0 until questArray.length()) {
                val nameQuest = questArray.getResourceId(j, -1)
                val questInfo = resources.getStringArray(nameQuest)
                val icons = resources.obtainTypedArray(nameQuest)
                val selectedIconResId = icons.getResourceId(iconIndex, -1)
                Log.d(TAG, "Имя: " + questInfo[0])
                if (sharedPreferences.getBoolean(questInfo[0], false)) {

                    achievements.add(Achievement(questInfo[8], questInfo[9], selectedIconResId))
                    Log.d(TAG,
                        "Проверка размерности: " + achievements.size
                            .toString()
                    )
                }
            }
        }
        if (achievements.size == 4) {
            achievements.add(Achievement("Спаситель слободы", "Освободить все достопримечательности", R.drawable.spasitel))
        }

        // Пример списка достижений
        info.recycle()

        achieveList.adapter = AchievementAdapter(achievements) { achievement ->
            // Обработка клика по элементу достижения
            val achievementImage = LayoutInflater.from(this).inflate(R.layout.achievement_image, null)
            val dialog = Dialog(this)
            //val textQuest = achievementWindow.findViewById<TextView>(R.id.textQuest)
            dialog.setContentView(achievementImage)
            dialog.setCancelable(true)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window?.setWindowAnimations(R.style.dialog_animation_fade)
            val image = achievementImage.findViewById<ImageView>(R.id.imageView)
            image.setImageResource(achievement.imageResId)
            dialog.show()
        }
        //textQuest.text = questText
        myDialog.show()
    }

    fun clickBtnAchieve(view: View) {
        openAchievementsWindow()
    }

    fun clickSettings(view: View) {
        openSettingsWindow()
    }

    private fun openSettingsWindow() {
        val achievementWindow = LayoutInflater.from(this).inflate(R.layout.settings_layout, null)
        val myDialog = Dialog(this)
        val btnNewGame = achievementWindow.findViewById<Button>(R.id.btnNewGame)

        val btnTheme = achievementWindow.findViewById<Button>(R.id.btn_theme)

        btnTheme.setOnClickListener {
            mapView.map.isNightModeEnabled = !sharedPreferences.getBoolean("nightTheme", false)

            val editor = sharedPreferences.edit()
            editor.putBoolean("nightTheme", !sharedPreferences.getBoolean("nightTheme", false))
            editor.apply()
            Log.d(TAG, "NightMode: " + sharedPreferences.getBoolean("nightTheme", false))
            quest.changeIconTheme()
        }

        btnNewGame.setOnClickListener {
            AlertDialog.Builder(this).apply {
                setTitle("Начать новую игру")
                setMessage("Вы уверены, что хотите начать новую игру? Прогресс текущего прохождения будет утерян.")
                setPositiveButton("Да") { dialog, _ ->
                    // Очищаем данные и сбрасываем квесты при подтверждении
                    val flag = sharedPreferences.getBoolean("nightTheme", false)
                    val editor = sharedPreferences.edit()
                    editor.clear()
                    editor.apply()
                    quest.resetQuests()
                    dialog.dismiss()
                    editor.putBoolean("nightTheme", flag)
                    editor.apply()
                }
                setNegativeButton("Нет") { dialog, _ ->
                    // Закрываем диалог при отказе
                    dialog.dismiss()
                }
                create()
                show()
            }
        }
        //val textQuest = achievementWindow.findViewById<TextView>(R.id.textQuest)
        myDialog.setContentView(achievementWindow)
        myDialog.setCancelable(true)
        myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        myDialog.window?.setWindowAnimations(R.style.dialog_animation_fade)
        //textQuest.text = questText
        myDialog.show()
    }

    private var checkCompliteGPS : Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.setApiKey("204ee658-86b7-453b-90db-dd6a00cdc770")
        MapKitFactory.initialize(this)
        setContentView(R.layout.activity_main)
        sharedPreferences = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)

        mapView = findViewById(R.id.mapview)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mapView.map.isTiltGesturesEnabled = false


        val jsonStr = parseJsonInString("customization.json")
        mapView.map.setMapStyle(jsonStr)


        mapView.map.move(CameraPosition(
            bottomRight, 17.0f, azimuth, 100f
        ))


        video = findViewById(R.id.videoCom)
        btnSkip = findViewById(R.id.btnSkip)


        btnPositionNow = findViewById(R.id.btnPositionNow)
        btnPositionNow.setOnClickListener {
//            if (ActivityCompat.checkSelfPermission(
//                    this,
//                    Manifest.permission.ACCESS_FINE_LOCATION
//                ) != PackageManager.PERMISSION_GRANTED &&
//                ActivityCompat.checkSelfPermission(
//                    this,
//                    Manifest.permission.ACCESS_COARSE_LOCATION
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                ActivityCompat.requestPermissions(
//                    this,
//                    arrayOf(
//                        Manifest.permission.ACCESS_FINE_LOCATION,
//                        Manifest.permission.ACCESS_COARSE_LOCATION
//                    ),
//                    LOCATION_PERMISSION_REQUEST_CODE
//                )
//            }
            isCheckCameraPosition = true
            Log.d(TAG, "")
            if (::locationNow.isInitialized) {
                updateLocationOnMap(locationNow)
            }

        }


        if (!sharedPreferences.getBoolean("nightTheme", false)) {
            val editor = sharedPreferences.edit()
            editor.putBoolean("nightTheme", false)
            editor.apply()
        }
        mapView.map.isNightModeEnabled = sharedPreferences.getBoolean("nightTheme", false)


        val map = mapView.mapWindow.map
        quest = Quest(this, this, map, R.array.all_quest)
        quest.initQuest()
        //quest.changeIconTheme()
        Log.d(TAG, "NightMode: " + sharedPreferences.getBoolean("nightTheme", false))


        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)


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

//                    cameraPositionJob?.cancel()
//                    cameraPositionJob = coroutineScope.launch {
//                        delay(10000)
//                        isCheckCameraPosition = true
////                        Log.d(TAG, "CheckCameraPosition")
//                    }
                    Log.d(TAG, "CheckCameraPosition: $isCheckCameraPosition")
                    } else {
                        isCheckCameraPosition = false
                    }
                }
            }
        }

        mapView.map.addCameraListener(cameraList)

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

    private fun requestLocationPermissions() {
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
        } else {
            // Разрешения уже предоставлены, выполняем необходимые действия
            isCheckCameraPosition = true
            Log.d(TAG, "Location permissions already granted")
            if (::locationNow.isInitialized) {
                updateLocationOnMap(locationNow)
            }
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

    override fun onResume() {
        super.onResume()



        accelerometer?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
        magnetometer?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }

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



    }

    @SuppressLint("ClickableViewAccessibility")
    fun click(view: View) {
        userLocationMarker.geometry = Point(57.736260, 40.921054)
    }

    private fun startLocationUpdates() {
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
            interval = 1000 // 10 секунд
            fastestInterval = 1000 // 5 секунд
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
        locationNow = Point(location.latitude, location.longitude)
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
            quest.checkPositionQuest(userLocation)
            userLocationMarker.geometry = userLocation

//            for (i in oldAzimuth.toInt() until azimuth.toInt()) {
//                Log.d("UpdateLocationOnMap", "Loop iteration: $i")
//                userLocationMarker.direction = i.toFloat()
//                if (isCheckCameraPosition) {
//                    mapView.map.move(
//                        CameraPosition(locationNow, 17.0f, i.toFloat(), 100f),
//                        Animation(Animation.Type.SMOOTH, 1f),
//                        null
//                    )
//                }
//            }
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
                    )
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
//        val coroutineScope = CoroutineScope(Dispatchers.Main)
//        coroutineScope.launch {
//
//
//            if (isMarkerInitialized) {
//                Log.d("UpdateLocationOnMap", "Loop iteration: " + event.values[2])
//                userLocationMarker.direction = azimuth
//                if (isCheckCameraPosition) {
//                    mapView.map.move(
//                        CameraPosition(locationNow, 17.0f, azimuth, 100f),
//                        Animation(Animation.Type.SMOOTH, 1f),
//                        null
//                    )
//                }
//            }
//            delay(10000)
//        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Implement if sensor accuracy changes are to be handled



    }
}
