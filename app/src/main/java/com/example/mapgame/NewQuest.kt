package com.example.mapgame

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.PointF
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.VideoView
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.map.TextStyle
import com.yandex.runtime.image.ImageProvider

class NewQuest {
    private var map: Map
    private var nameQuest: Int = 0
    lateinit var placemark: PlacemarkMapObject
    private lateinit var placemarkNext: PlacemarkMapObject
    private var context: Context
    private var sharedPreferences: SharedPreferences
    private var questInfo: Array<String>

    private var questName: String
    private var questLocation: Point
    private var locationName: String
    private var questText: String
    private var questNext: String
    private var icondIndex: Int



    private lateinit var collection: MapObjectCollection

    constructor(context: Context, map: Map, nameQuest: Int) {
        this.map = map
        this.nameQuest = nameQuest
        this.context = context
        sharedPreferences = context.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        questInfo = context.resources.getStringArray(nameQuest)
        questName = questInfo[0]
        questLocation = Point(questInfo[1].toDouble(), questInfo[2].toDouble())
        locationName = questInfo[3]
        questText = questInfo[4]
        questNext = questInfo[5]
        icondIndex = questInfo[6].toInt()
    }

    constructor(context: Context, map: Map, nameQuest: Int, placemarkNext: PlacemarkMapObject) {
        this.map = map
        this.nameQuest = nameQuest
        this.context = context
        sharedPreferences = context.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        questInfo = context.resources.getStringArray(nameQuest)
        this.placemarkNext = placemarkNext
        questName = questInfo[0]
        questLocation = Point(questInfo[1].toDouble(), questInfo[2].toDouble())
        locationName = questInfo[3]
        questText = questInfo[4]
        questNext = questInfo[5]
        icondIndex = questInfo[6].toInt()
    }


    private val iconTapListen = MapObjectTapListener { _, _ ->
        newDialog()
        false
    }

    private fun updatePlacemarkVisibility(placemark: PlacemarkMapObject?, isVisible: Boolean) {
        placemark?.isVisible = isVisible
    }

    fun createNewQuest() {
        val icons = context.resources.obtainTypedArray(R.array.icons)
        val selectedIconResId = icons.getResourceId(icondIndex, -1)

//        val iconIndex = (0 until icons.length()).random()
//        val selectedIconResId = icons.getResourceId(iconIndex, -1)

        collection = map.mapObjects.addCollection()
        placemark = collection.addPlacemark().apply {
            geometry = questLocation
            addTapListener(iconTapListen)
            setText(
                locationName,
                TextStyle().apply {
                    size = 15f
                    placement = TextStyle.Placement.TOP
                    color = Color.WHITE
                },
            )
            useCompositeIcon().apply {
                setIcon(
                    ImageProvider.fromResource(context, selectedIconResId),
                    IconStyle().apply {
                        anchor = PointF(0.5f, 1.0f)
                        scale = 0.3f
                    }
                )
            }
            isVisible = sharedPreferences.getBoolean(questName, false)
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun newDialog() {
        val dialogBinding = LayoutInflater.from(context).inflate(R.layout.dialog_enemy, null)
        val mainActiv = context as Activity
        val myDialog = Dialog(context)
        val textQuest = dialogBinding.findViewById<TextView>(R.id.textQuest)
        myDialog.setContentView(dialogBinding)
        myDialog.setCancelable(true)
        myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        textQuest.text = questText
        myDialog.show()


        val video = mainActiv.findViewById<VideoView>(R.id.videoCom)

        val btnClose = mainActiv.findViewById<ImageView>(R.id.btnClose)
        btnClose.setOnClickListener {
            video.stopPlayback()
            video.visibility = View.INVISIBLE
            btnClose.visibility = View.INVISIBLE
        }

        val btnBattle = dialogBinding.findViewById<Button>(R.id.btnBattle)
        btnBattle.setOnClickListener {

            myDialog.dismiss()

            video.visibility = View.VISIBLE
            video.setVideoPath("android.resource://" + context.packageName + "/" + R.raw.video1)

            video.setOnPreparedListener {
                val layoutParams = video.layoutParams
                val d = mainActiv.findViewById<View>(R.id.lay)
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



            video.setOnTouchListener {_, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    if (btnClose.visibility == View.INVISIBLE)
                        btnClose.visibility = View.VISIBLE
                    else
                        btnClose.visibility = View.INVISIBLE
                }
                true
            }

            val editor = sharedPreferences.edit()
            var currentState = sharedPreferences.getBoolean(questName, false)
            editor.putBoolean(questName, !currentState)
            editor.apply()
            updatePlacemarkVisibility(placemark, sharedPreferences.getBoolean(questName, false))
            Log.d(TAG, "QUEST1 BOOL: " + sharedPreferences.getBoolean(questName, false).toString())

            if (questNext.isNotBlank()) {
                currentState = sharedPreferences.getBoolean(questNext, false)
                editor.putBoolean(questNext, !currentState)
                editor.apply()
                val s = collection.parent
                Log.d(TAG, s.toString())
                updatePlacemarkVisibility(placemarkNext, sharedPreferences.getBoolean(questNext, false))
                Log.d(TAG, "QUEST2 BOOL: " + sharedPreferences.getBoolean(questNext, false).toString())
            }

//            currentState = sharedPreferences.getBoolean(getString(R.string.quest2), false)
//            editor.putBoolean(getString(R.string.quest2), !currentState)
//            editor.apply()
//            updatePlacemarkVisibility(placemark1, sharedPreferences.getBoolean(getString(R.string.quest2), false))
//            Log.d(TAG, "QUEST2 BOOL: " + sharedPreferences.getBoolean(getString(R.string.quest2), false).toString())


        }
    }




}