package com.cmlee.executiful.letswinmarksix.helper

import android.content.DialogInterface
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AlertDialog

object AlertDialogHelper {
    val DialogInterface.PositiveButton: Button get() = (this as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
    val DialogInterface.NeutralButton: Button get() = (this as AlertDialog).getButton(AlertDialog.BUTTON_NEUTRAL)
    val DialogInterface.NegativeButton: Button get() = (this as AlertDialog).getButton(AlertDialog.BUTTON_NEGATIVE)
    val DialogInterface.ListView:ListView get() = (this as AlertDialog).listView
}