package com.example.agenciaviajes.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object DriveUploader {

    private const val TAG = "DriveUploader"

    private const val SCRIPT_URL = "https://script.google.com/macros/s/AKfycbyXMj1vzWHDffoePss3MNWWLiUpjw2JlYqiqVQODzzq6X-_iuk261uMhgVzPW33OCuQ/exec"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    fun subirImagen(context: Context, uri: Uri, nombreArchivo: String): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()

            if (bytes == null) {
                Log.e(TAG, "No se pudieron leer los bytes")
                return null
            }

            Log.d(TAG, "Tamaño de imagen: ${bytes.size} bytes")

            val bytesArray = JSONArray()
            bytes.forEach { bytesArray.put(it.toInt()) }

            val json = JSONObject().apply {
                put("fileName", nombreArchivo)
                put("mimeType", "image/jpeg")
                put("bytes", bytesArray)
            }

            val requestBody = json.toString()
                .toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url(SCRIPT_URL)
                .post(requestBody)
                .build()

            Log.d(TAG, "Enviando POST a Apps Script...")

            val response = client.newCall(request).execute()

            Log.d(TAG, "HTTP Code: ${response.code}")
            Log.d(TAG, "isSuccessful: ${response.isSuccessful}")

            val responseBody = response.body?.string()
            Log.d(TAG, "Response: $responseBody")

            if (responseBody.isNullOrBlank()) {
                Log.e(TAG, "Respuesta vacía del servidor")
                return null
            }

            // Verificar que la respuesta sea JSON, no HTML
            if (!responseBody.trim().startsWith("{")) {
                Log.e(TAG, "Respuesta no es JSON: ${responseBody.take(200)}")
                return null
            }

            val jsonResponse = JSONObject(responseBody)

            if (jsonResponse.optBoolean("success", false)) {
                val fileId = jsonResponse.getString("fileId")
                Log.d(TAG, "Imagen subida. fileId: $fileId")
                fileId
            } else {
                Log.e(TAG, "Script error: ${jsonResponse.optString("error")}")
                null
            }

        } catch (e: Exception) {
            Log.e(TAG, "Excepción: ${e.message}", e)
            null
        }
    }

    fun construirUrlImagen(fileId: String): String {
        return "https://drive.google.com/uc?id=$fileId"
    }
}