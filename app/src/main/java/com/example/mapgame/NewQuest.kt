package com.example.mapgame

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.PointF
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.VideoView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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

    companion object {
        const val QUEST_NAME_BATTLE = "battle"
        const val QUEST_NAME_MEMO = "memo"
        const val QUEST_NAME_NUMBERS = "numbers"
    }

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
    private var iconIndex: Int = 6
    private var questVisible: Boolean
    private var video: VideoView
    private val mainActiv: Activity
    private val activity: MainActivity
    private var resultLauncher: ActivityResultLauncher<Intent>
    private var typeQuest: String
    var dialogShow: Boolean = false
        get() {
            return field
        }


    private lateinit var collection: MapObjectCollection
    fun getName(): Int {
        return nameQuest
    }
    fun getLocationName(): String {
        return locationName
    }

    fun getQuestName(): String {
        return questName
    }
    fun getVisible(): Boolean {
        return questVisible
    }

    constructor(activity: MainActivity,context: Context, map: Map, nameQuest: Int) {
        this.map = map
        this.nameQuest = nameQuest
        this.context = context
        sharedPreferences = context.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        questInfo = context.resources.getStringArray(nameQuest)
        questName = questInfo[0]
        questLocation = Point(questInfo[1].toDouble(), questInfo[2].toDouble())
        locationName = questInfo[3]
        questText = questInfo[4]
        questVisible = questInfo[7].toBoolean()
        typeQuest = questInfo[11]
        mainActiv = context as Activity
        video = mainActiv.findViewById(R.id.videoCom)
        setVideo()
        this.activity = activity
        resultLauncher = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                achievementWindow()
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
    }

    constructor(activity: MainActivity, context: Context, map: Map, nameQuest: Int, placemarkNext: NewQuest) {
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
        questVisible = questInfo[7].toBoolean()
        typeQuest = questInfo[11]
        mainActiv = context as Activity
        video = mainActiv.findViewById(R.id.videoCom)
        setVideo()
        this.activity = activity
        resultLauncher = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                achievementWindow()
                val editor = sharedPreferences.edit()
                //val currentState = sharedPreferences.getBoolean(questName, false)
                editor.putBoolean(questName, true)
                editor.putBoolean(questName+"Visible", false)
                Log.d(TAG, "Questname: "+sharedPreferences.getBoolean(questName+"Visible", false).toString())
                updatePlacemarkVisibility(placemark, false)


                /*placemark может быть null, в таком случае ничего не произойдёт*/
                updatePlacemarkVisibility(placemarkNext.placemark, true)
                editor.putBoolean(placemarkNext.getQuestName() + "Visible", true)
                Log.d(
                    TAG,
                    "QuestnameNext: " + sharedPreferences.getBoolean(
                        placemarkNext.getQuestName() + "Visible",
                        false
                    ).toString()
                )
                editor.apply()
            }
        }
    }

    private fun setVideo() {
        video.setVideoPath("android.resource://" + context.packageName + "/" + R.raw.video1)

        video.setOnPreparedListener {
            val layoutParams = video.layoutParams
            val d = mainActiv.findViewById<View>(R.id.lay)
            layoutParams.width = d.layoutParams.width
            layoutParams.height = d.layoutParams.height
            video.layoutParams = layoutParams
        }
    }

    private val iconTapListen = MapObjectTapListener { _, _ ->
        newDialog()
        false
    }

    fun updatePlacemarkVisibility(placemark: PlacemarkMapObject?, isVisible: Boolean) {
        placemark?.isVisible = isVisible
    }


    fun createNewQuest() {
        val icons = context.resources.obtainTypedArray(nameQuest)
        var colorPlacemark = Color.WHITE
        var selectedIconResId = 0
        try {
            if (sharedPreferences.getBoolean("nightTheme", false)) {
                selectedIconResId = icons.getResourceId(5, -1)
                colorPlacemark = Color.WHITE
            }
            else {
                selectedIconResId = icons.getResourceId(iconIndex, -1)
                colorPlacemark = Color.BLACK
            }

            collection = map.mapObjects.addCollection()
            placemark = collection.addPlacemark().apply {
                geometry = questLocation
                addTapListener(iconTapListen)

                setText(
                    locationName,
                    TextStyle().apply {
                        size = 15f
                        placement = TextStyle.Placement.TOP
                        color = colorPlacemark
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

    private fun fadeOut(view: View) {
        val animator = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f)
        animator.duration = 200 // Длительность анимации в миллисекундах
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                view.visibility = View.INVISIBLE
            }
        })
        animator.start()
    }

    // Функция для плавного появления кнопки
    private fun fadeIn(view: View) {
        view.visibility = View.VISIBLE
        val animator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
        animator.duration = 300 // Длительность анимации в миллисекундах
        animator.interpolator = DecelerateInterpolator()
        animator.start()
    }
    private var dimmingView: View? = null

    @SuppressLint("ObjectAnimatorBinding")
    private fun fadeInDimmingView() {
        dimmingView = View(context)
        dimmingView?.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        dimmingView?.setBackgroundColor(Color.BLACK)
        dimmingView?.alpha = 0f // Начальная прозрачность

        val decorView = mainActiv.window.decorView as ViewGroup
        decorView.addView(dimmingView)


        val animator = ObjectAnimator.ofFloat(dimmingView, "alpha", 0f, 1f, 0f)
        animator.duration = 500 // Длительность анимации в миллисекундах
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.start()
    }

    @SuppressLint("ObjectAnimatorBinding")
    private fun fadeOutView() {
        dimmingView = View(context)
        dimmingView?.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        dimmingView?.setBackgroundColor(Color.BLACK)
        dimmingView?.alpha = 0f // Начальная прозрачность

        val decorView = mainActiv.window.decorView as ViewGroup
        decorView.addView(dimmingView)

        val animator = ObjectAnimator.ofFloat(dimmingView, "alpha", 1f, 0f)
        animator.duration = 4000 // Длительность анимации в миллисекундах
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.start()
    }



    @SuppressLint("ClickableViewAccessibility")
    fun newDialog() {
        DialogManager.closeAllDialogs()
        val dialogBinding = LayoutInflater.from(context).inflate(R.layout.dialog_enemy, null)
        val myDialog = Dialog(context)
        val locationQuest = dialogBinding.findViewById<TextView>(R.id.locationQuest)
        val textQuest = dialogBinding.findViewById<TextView>(R.id.questText)
        myDialog.setContentView(dialogBinding)
        myDialog.setCancelable(true)
        myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        myDialog.window?.setWindowAnimations(R.style.dialog_animation_fade)
        locationQuest.text = questInfo[3]
        textQuest.text = questText
        myDialog.show()

        DialogManager.addDialog(myDialog)


        val btnClose = mainActiv.findViewById<ImageView>(R.id.btnClose)
        btnClose.setOnClickListener {
            video.stopPlayback()
            fadeOutView()
            video.visibility = View.INVISIBLE
            btnClose.visibility = View.INVISIBLE
            checkTypeQuest()
        }

        val btnBattle = dialogBinding.findViewById<Button>(R.id.btnBattle)
        btnBattle.setOnClickListener {
            fadeInDimmingView()
            video.start()
            video.visibility = View.VISIBLE

            myDialog.dismiss()

            video.setOnCompletionListener {
                video.stopPlayback()
                fadeOutView()
                video.visibility = View.INVISIBLE
                fadeOut(btnClose)
                fadeInDimmingView()
                checkTypeQuest()
            }



            video.setOnTouchListener {_, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    if (btnClose.visibility == View.INVISIBLE) {
                        fadeIn(btnClose)
                    }
                    else {
                        fadeOut(btnClose)
                    }
                }
                true
            }
        }
    }

    private fun checkTypeQuest() {
        if (typeQuest == QUEST_NAME_BATTLE) {
            val intent = Intent(context, ArActivity::class.java)
            intent.putExtra("name", questInfo)
            resultLauncher.launch(intent)
        }
        else if (typeQuest == QUEST_NAME_MEMO) {
            val intent = Intent(context, CardGameActivity::class.java)
            resultLauncher.launch(intent)
        }
        else if (typeQuest == QUEST_NAME_NUMBERS) {
            val intent = Intent(context, NumbersActivity::class.java)
            resultLauncher.launch(intent)
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