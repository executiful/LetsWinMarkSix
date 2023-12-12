package com.cmlee.executiful.letswinmarksix.helper

import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog

object AlertDialogHelper {
    fun DialogInterface.PositiveButton() = (this as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
    fun DialogInterface.NeutralButton() = (this as AlertDialog).getButton(AlertDialog.BUTTON_NEUTRAL)
    fun DialogInterface.ListView() = (this as AlertDialog).listView
}