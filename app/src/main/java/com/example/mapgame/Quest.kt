package com.example.mapgame

import android.content.Context
import android.content.SharedPreferences
import com.yandex.mapkit.map.Map

class Quest(private var context: Context, private var map: Map, private var allQuest: Int) {
    private lateinit var quest: NewQuest
    private var questList: MutableList<MutableList<NewQuest>> = mutableListOf()
    private var sharedPreferences: SharedPreferences =
        context.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)



    fun initQuest() {
        val info = context.resources.obtainTypedArray(allQuest)
        try {
            for (index in 0 until info.length()) {
                questList.add(createQuest(info.getResourceId(index, -1)))
            }
        } finally {
            info.recycle()
        }
    }

    private fun checkPreferences(nameQuest: Int) {
        val questInfo = context.resources.getStringArray(nameQuest)
        if (!sharedPreferences.contains(questInfo[0])) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(questInfo[0], false)
            editor.apply()
        }
        if (!sharedPreferences.contains(questInfo[0]+"Visible")) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(questInfo[0]+"Visible", questInfo[7].toBoolean())
            editor.apply()
        }
    }

    private fun createQuest(nameQuestLine: Int): MutableList<NewQuest> {
        val questArray = context.resources.obtainTypedArray(nameQuestLine)
        try {
            val questList = mutableListOf<NewQuest>()
            for (i in (questArray.length() - 1) downTo 0) {
                if (questArray.length() == 0) {
                    throw Exception("ERROR")
                }
                val nameQuest = questArray.getResourceId(i, -1)
                checkPreferences(nameQuest)
                if (questArray.length() == 1) {
                    quest = NewQuest(context, map, nameQuest)
                    quest.createNewQuest()
                } else if (questArray.length() - 1 == i && questArray.length() > 1) {
                    quest = NewQuest(context, map, nameQuest)
                    quest.createNewQuest()
                    questList.add(quest)
                } else {
                    quest = NewQuest(context, map, nameQuest, questList.last())
                    quest.createNewQuest()
                    questList.add(quest)
                }
            }
            return questList
        } finally {
            questArray.recycle()
        }
    }

    fun resetQuests() {
        val editor = sharedPreferences.edit()
        questList.flatten().forEach { quest ->
            editor.putBoolean(quest.getQuestName()+"Visible", quest.getVisible())
        }
        editor.apply()
        updateQuestVisibility()
    }

    private fun updateQuestVisibility() {
        questList.flatten().forEach { quest ->
            quest.updatePlacemarkVisibility(quest.placemark, quest.getVisible())
        }
    }



}