package com.example.agenciaviajes.utils

import android.content.Context
import android.net.Uri
import com.example.agenciaviajes.R
import com.example.agenciaviajes.databinding.ActivityFormularioBinding

object Validaciones {

    fun validarFormulario(
        context: Context,
        binding: ActivityFormularioBinding,
        imagenSeleccionada: Uri?,
        imagenActualId: String
    ): Boolean {
        var valido = true

        // Nombre
        val nombre = binding.etNombre.text.toString().trim()
        if (nombre.isEmpty()) {
            binding.tilNombre.error = context.getString(R.string.error_campo_vacio)
            valido = false
        } else binding.tilNombre.error = null

        // País
        if (binding.spinnerPais.selectedItemPosition == 0) {
            ToastHelper.show(context, context.getString(R.string.error_pais_requerido))
            valido = false
        }

        // Precio
        val precioTexto = binding.etPrecio.text.toString().trim()
        if (precioTexto.isEmpty()) {
            binding.tilPrecio.error = context.getString(R.string.error_campo_vacio)
            valido = false
        } else {
            val precio = precioTexto.toDoubleOrNull()
            if (precio == null || precio <= 0) {
                binding.tilPrecio.error = context.getString(R.string.error_precio_invalido)
                valido = false
            } else binding.tilPrecio.error = null
        }

        // Descripción
        val descripcion = binding.etDescripcion.text.toString().trim()
        if (descripcion.isEmpty()) {
            binding.tilDescripcion.error = context.getString(R.string.error_campo_vacio)
            valido = false
        } else if (descripcion.length < 20) {
            binding.tilDescripcion.error = context.getString(R.string.error_descripcion_corta)
            valido = false
        } else binding.tilDescripcion.error = null

        // Imagen
        if (imagenSeleccionada == null && imagenActualId.isEmpty()) {
            ToastHelper.show(context, context.getString(R.string.error_imagen_requerida))
            valido = false
        }

        return valido
    }
}