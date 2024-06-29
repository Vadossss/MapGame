package com.example.mapgame

import android.content.Context
import android.content.SharedPreferences
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.PlacemarkMapObject

class Quest {
    private lateinit var quest: NewQuest
    private var map: Map
    private var nameQuest: Int = 0
    lateinit var placemark: PlacemarkMapObject
    private var context: Context
    private lateinit var placemarkList: MutableList<NewQuest>


    constructor(context: Context, map: Map, nameQuest: Int) {
        this.map = map
        this.nameQuest = nameQuest
        this.context = context
    }


    fun createQuest() {
        val questArray = context.resources.obtainTypedArray(nameQuest)
        placemarkList = mutableListOf()
        for (i in (questArray.length()-1) downTo 0) {
            if (questArray.length() == 0) {
                return
            }
            val s = questArray.getResourceId(i, -1)
            if (questArray.length() == 1) {
                quest = NewQuest(context, map, s)
                quest.createNewQuest()
            }
            else if (questArray.length() - 1 == i && questArray.length() > 1) {
                quest = NewQuest(context, map, s)
                quest.createNewQuest()
                placemarkList.add(quest)
            }
            else {
                quest = NewQuest(context, map, s, placemarkList.last().placemark)
                quest.createNewQuest()
                placemarkList.add(quest)
            }
        }
    }



}