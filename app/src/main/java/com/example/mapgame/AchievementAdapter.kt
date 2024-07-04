package com.example.mapgame

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class Achievement(val name: String, val text: String, val imageResId: Int)

class AchievementAdapter(
    private val achievements: MutableList<Achievement>,
    private val onItemClick: (Achievement) -> Unit
) : RecyclerView.Adapter<AchievementAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.achieveName)
        val text: TextView = itemView.findViewById(R.id.achieveText)
        val image: ImageView = itemView.findViewById(R.id.imageAchiv)

        fun bind(achievement: Achievement) {
            name.text = achievement.name
            text.text = achievement.text
            image.setImageResource(achievement.imageResId)
            itemView.setOnClickListener {
                onItemClick(achievement)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.achievement_window, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(achievements[position])
    }

    override fun getItemCount() = achievements.size
}
