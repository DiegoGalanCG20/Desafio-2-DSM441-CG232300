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
import com.example.agenciaviajes.models.Destino
import com.example.agenciaviajes.utils.DriveUploader
import com.example.agenciaviajes.utils.NetworkUtils
import com.example.agenciaviajes.utils.Validaciones
import com.google.firebase.firestore.FirebaseFirestore

class EditarDestinoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFormularioBinding
    private val db = FirebaseFirestore.getInstance()

    private lateinit var destinoId: String
    private var imagenUriNueva: Uri? = null
    private var imagenDriveIdActual: String = ""
    private lateinit var progressDialog: ProgressDialog

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            imagenUriNueva = uri
            Glide.with(this).load(uri).into(binding.ivPreview)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormularioBinding.inflate(layoutInflater)
        setContentView(binding.root)
        destinoId = intent.getStringExtra("destinoId") ?: run {
            Toast.makeText(this, "Destino no encontrado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        binding.tvTitulo.text = getString(R.string.titulo_editar)
        binding.btnGuardar.text = getString(R.string.actualizar_destino)

        ArrayAdapter.createFromResource(
            this,
            R.array.countries_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerPais.adapter = adapter
        }

        binding.btnSeleccionarImagen.setOnClickListener {
            pickImageLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        // Guardar cambios
        binding.btnGuardar.setOnClickListener { guardarCambios() }

        cargarDestino()
    }

    private fun cargarDestino() {
        db.collection("destinos").document(destinoId).get()
            .addOnSuccessListener { document ->
                val destino = document.toObject(Destino::class.java)
                if (destino == null) {
                    Toast.makeText(this, "Destino no encontrado", Toast.LENGTH_SHORT).show()
                    finish()
                    return@addOnSuccessListener
                }
                binding.etNombre.setText(destino.nombre)
                binding.etPrecio.setText(destino.precio.toString())
                binding.etDescripcion.setText(destino.descripcion)
                imagenDriveIdActual = destino.imagenDriveId

                val adapter = binding.spinnerPais.adapter as ArrayAdapter<CharSequence>
                val pos = adapter.getPosition(destino.pais)
                if (pos >= 0) binding.spinnerPais.setSelection(pos)

                // Cargar imagen desde Drive
                Glide.with(this)
                    .load(DriveUploader.construirUrlImagen(destino.imagenDriveId))
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_delete)
                    .into(binding.ivPreview)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun guardarCambios() {
        if (!Validaciones.validarFormulario(
                this,
                binding,
                imagenUriNueva,
                imagenDriveIdActual
            )
        ) return

        if (!NetworkUtils.hayConexion(this)) {
            Toast.makeText(this, R.string.error_sin_conexion, Toast.LENGTH_LONG).show()
            return
        }
        progressDialog = ProgressDialog(this).apply {
            setMessage(getString(R.string.loading_guardando))
            setCancelable(false)
            show()
        }
        binding.btnGuardar.isEnabled = false

        if (imagenUriNueva != null) {
            // Subir nueva imagen
            Thread {
                val nuevoFileId = DriveUploader.subirImagen(
                    this,
                    imagenUriNueva!!,
                    "destino_${System.currentTimeMillis()}"
                )
                runOnUiThread {
                    if (nuevoFileId == null) {
                        progressDialog.dismiss()
                        binding.btnGuardar.isEnabled = true
                        Toast.makeText(this, R.string.error_subir_imagen, Toast.LENGTH_LONG).show()
                        return@runOnUiThread
                    }
                    actualizarFirestore(nuevoFileId)
                }
            }.start()
        } else {
            actualizarFirestore(imagenDriveIdActual)
        }
    }

    private fun actualizarFirestore(fileId: String) {
        val datos = mapOf(
            "nombre" to binding.etNombre.text.toString().trim(),
            "pais" to binding.spinnerPais.selectedItem.toString(),
            "precio" to binding.etPrecio.text.toString().toDouble(),
            "descripcion" to binding.etDescripcion.text.toString().trim(),
            "imagenDriveId" to fileId
        )

        db.collection("destinos").document(destinoId).update(datos)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, R.string.destino_actualizado, Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                binding.btnGuardar.isEnabled = true
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}