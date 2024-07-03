package com.example.mapgame

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator

class FadePressets(private val context: Context, private val mainActiv: Activity) {

    fun fadeOut(view: View) {
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
    fun fadeIn(view: View) {
        view.visibility = View.VISIBLE
        val animator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
        animator.duration = 300 // Длительность анимации в миллисекундах
        animator.interpolator = DecelerateInterpolator()
        animator.start()
    }
    private var dimmingView: View? = null

    @SuppressLint("ObjectAnimatorBinding")
    fun fadeInDimmingView() {
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
    fun fadeOutView() {
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
}