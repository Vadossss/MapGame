package com.example.mapgame

import android.app.Dialog

object DialogManager {
    val activeDialogs = mutableListOf<Dialog>()

    fun closeAllDialogs() {
        for (dialog in activeDialogs) {
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }
        activeDialogs.clear()
    }

    fun addDialog(dialog: Dialog) {
        activeDialogs.add(dialog)
    }

    fun removeDialog(dialog: Dialog) {
        activeDialogs.remove(dialog)
    }
}