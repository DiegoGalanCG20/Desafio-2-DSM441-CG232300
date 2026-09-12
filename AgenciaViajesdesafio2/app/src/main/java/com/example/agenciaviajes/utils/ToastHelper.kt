package com.example.agenciaviajes.utils
import android.content.Context
import android.widget.Toast

object ToastHelper {
    fun show(context: Context, mensaje: String) {
        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
    }
}
