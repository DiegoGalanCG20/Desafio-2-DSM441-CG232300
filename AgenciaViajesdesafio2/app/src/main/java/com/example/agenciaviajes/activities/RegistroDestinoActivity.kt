package com.example.agenciaviajes.activities

import android.app.ProgressDialog
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.agenciaviajes.R
import com.example.agenciaviajes.databinding.ActivityFormularioBinding
import com.example.agenciaviajes.utils.DriveUploader
import com.example.agenciaviajes.utils.NetworkUtils
import com.example.agenciaviajes.utils.Validaciones
import com.google.firebase.firestore.FirebaseFirestore

class RegistroDestinoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFormularioBinding
    private val db = FirebaseFirestore.getInstance()

    private var imagenUriSeleccionada: Uri? = null
    private lateinit var progressDialog: ProgressDialog

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            imagenUriSeleccionada = uri
            Glide.with(this).load(uri).into(binding.ivPreview)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormularioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Título
        binding.tvTitulo.text = getString(R.string.titulo_registrar)
        binding.btnGuardar.text = getString(R.string.guardar_destino)

        // Configurar Spinner
        ArrayAdapter.createFromResource(
            this,
            R.array.countries_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerPais.adapter = adapter
        }

        // Seleccionar imagen (FloatingActionButton)
        binding.btnSeleccionarImagen.setOnClickListener {
            pickImageLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        // Guardar
        binding.btnGuardar.setOnClickListener { guardarDestino() }
    }

    private fun guardarDestino() {
        // Validaciones
        if (!Validaciones.validarFormulario(this, binding, imagenUriSeleccionada, "")) return

        if (!NetworkUtils.hayConexion(this)) {
            Toast.makeText(this, R.string.error_sin_conexion, Toast.LENGTH_LONG).show()
            return
        }

        progressDialog = ProgressDialog(this).apply {
            setMessage(getString(R.string.loading_subiendo_imagen))
            setCancelable(false)
            show()
        }
        binding.btnGuardar.isEnabled = false

        Thread {
            val fileId = DriveUploader.subirImagen(
                this,
                imagenUriSeleccionada!!,
                "destino_${System.currentTimeMillis()}"
            )
            runOnUiThread {
                if (fileId == null) {
                    progressDialog.dismiss()
                    binding.btnGuardar.isEnabled = true
                    Toast.makeText(this, R.string.error_subir_imagen, Toast.LENGTH_LONG).show()
                    return@runOnUiThread
                }
                progressDialog.setMessage(getString(R.string.loading_guardando))
                guardarEnFirestore(fileId)
            }
        }.start()
    }

    private fun guardarEnFirestore(fileId: String) {
        val datos = mapOf(
            "nombre" to binding.etNombre.text.toString().trim(),
            "pais" to binding.spinnerPais.selectedItem.toString(),
            "precio" to binding.etPrecio.text.toString().toDouble(),
            "descripcion" to binding.etDescripcion.text.toString().trim(),
            "imagenDriveId" to fileId
        )

        db.collection("destinos").add(datos)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, R.string.destino_guardado, Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                binding.btnGuardar.isEnabled = true
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}