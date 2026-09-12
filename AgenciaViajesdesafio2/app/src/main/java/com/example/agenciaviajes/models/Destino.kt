package com.example.agenciaviajes.models

data class Destino(
    val id: String = "",
    val nombre: String = "",
    val pais: String = "",
    val precio: Double = 0.0,
    val descripcion: String = "",
    val imagenDriveId: String = ""
)