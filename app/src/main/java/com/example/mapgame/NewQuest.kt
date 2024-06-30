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
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class NewQuest {
    private var map: Map
    private var nameQuest: Int = 0
    lateinit var placemark: PlacemarkMapObject
    private var placemarkNext: NewQuest? = null
    private var context: Context
    private var sharedPreferences: SharedPreferences
    private var questInfo: Array<String>

    private var questName: String
    private var questLocation: Point
    private var locationName: String
    private var questText: String
//    private var questNext: String
    private var iconIndex: Int
    private var questVisible: Boolean



    private lateinit var collection: MapObjectCollection


    fun getQuestName(): String {
        return questName
    }
    fun getVisible(): Boolean {
        return questVisible
    }

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
//        questNext = questInfo[5]
        iconIndex = questInfo[6].toInt()
        questVisible = questInfo[7].toBoolean()
    }

    constructor(context: Context, map: Map, nameQuest: Int, placemarkNext: NewQuest) {
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
//        questNext = questInfo[5]
        iconIndex = questInfo[6].toInt()
        questVisible = questInfo[7].toBoolean()
    }


    private val iconTapListen = MapObjectTapListener { _, _ ->
        newDialog()
        false
    }

    fun updatePlacemarkVisibility(placemark: PlacemarkMapObject?, isVisible: Boolean) {
        placemark?.isVisible = isVisible
    }


    fun createNewQuest() {
        val icons = context.resources.obtainTypedArray(R.array.icons)
        try {
            val selectedIconResId = icons.getResourceId(iconIndex, -1)

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
                isVisible = sharedPreferences.getBoolean(questName+"Visible", false)
            }
        } finally {
            icons.recycle()
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
                achievementWindow()
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
            //val currentState = sharedPreferences.getBoolean(questName, false)
            editor.putBoolean(questName, true)
            editor.putBoolean(questName+"Visible", false)
            Log.d(TAG, "Questname: "+sharedPreferences.getBoolean(questName+"Visible", false).toString())
            updatePlacemarkVisibility(placemark, false)


            /*placemark может быть null, в таком случае ничего не произойдёт*/
            updatePlacemarkVisibility(placemarkNext?.placemark, true)
            editor.putBoolean(placemarkNext?.getQuestName() + "Visible", true)
            Log.d(
                TAG,
                "QuestnameNext: " + sharedPreferences.getBoolean(
                    placemarkNext?.getQuestName() + "Visible",
                    false
                ).toString()
            )
            editor.apply()

        }
    }

    private fun achievementWindow() {
        val achievementWindow = LayoutInflater.from(context).inflate(R.layout.achievement_window, null)
        val myDialog = Dialog(context)
        val nameAchievement = achievementWindow.findViewById<TextView>(R.id.achieveName)
        val textAchievement = achievementWindow.findViewById<TextView>(R.id.achieveText)
        myDialog.setContentView(achievementWindow)
        myDialog.setCancelable(false)
        myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.BLACK))
        myDialog.window?.setDimAmount(0F)

        myDialog.window?.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
        myDialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        myDialog.window?.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)

        nameAchievement.text = questInfo[8]
        textAchievement.text = questInfo[9]
        val params= myDialog.window?.attributes
        params?.gravity = Gravity.TOP
        params?.y = 50
        myDialog.show()

        myDialog.window?.decorView?.post {
            myDialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
        }

        val coroutineScope = CoroutineScope(Dispatchers.Main)
        coroutineScope.launch {
            doWork()
            myDialog.dismiss()
        }
    }

    private suspend fun doWork(){
            delay(4000)
    }
}