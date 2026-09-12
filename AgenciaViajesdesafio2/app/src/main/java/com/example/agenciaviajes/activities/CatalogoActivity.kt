package com.example.agenciaviajes.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.agenciaviajes.R
import com.example.agenciaviajes.adapters.DestinoAdapter
import com.example.agenciaviajes.databinding.ActivityCatalogoBinding
import com.example.agenciaviajes.models.Destino
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class CatalogoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCatalogoBinding
    private lateinit var adapter: DestinoAdapter
    private val listaDestinos = mutableListOf<Destino>()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var listenerRegistro: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCatalogoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // Mostrar correo del usuario
        val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: "Sin sesión"
        binding.tvUsuario.text = userEmail

        // Botón logout
        binding.btnLogout.setOnClickListener {
            confirmarCerrarSesion()
        }

        // RecyclerView
        binding.recycler.layoutManager = LinearLayoutManager(this)
        adapter = DestinoAdapter(
            listaDestinos,
            onItemClick = { destino ->
                val intent = Intent(this, EditarDestinoActivity::class.java)
                intent.putExtra("destinoId", destino.id)
                startActivity(intent)
            },
            onItemLongClick = { destino -> confirmarEliminacion(destino) }
        )
        binding.recycler.adapter = adapter

        // FAB
        binding.fabAgregar.setOnClickListener {
            startActivity(Intent(this, RegistroDestinoActivity::class.java))
        }

        escucharDestinos()
    }

    private fun escucharDestinos() {
        listenerRegistro = db.collection("destinos").addSnapshotListener { snapshot, error ->
            if (error != null) {
                // Solo mostrar error si el usuario sigue autenticado
                if (auth.currentUser != null) {
                    Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
                return@addSnapshotListener
            }

            listaDestinos.clear()
            snapshot?.documents?.forEach { doc ->
                val destino = doc.toObject(Destino::class.java)
                if (destino != null) listaDestinos.add(destino.copy(id = doc.id))
            }
            adapter.actualizarLista(listaDestinos)

            binding.tvVacio.visibility =
                if (listaDestinos.isEmpty()) View.VISIBLE else View.GONE
            binding.recycler.visibility =
                if (listaDestinos.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_catalogo, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                confirmarCerrarSesion()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun confirmarCerrarSesion() {
        AlertDialog.Builder(this)
            .setTitle(R.string.cerrar_sesion_titulo)
            .setMessage(R.string.cerrar_sesion_mensaje)
            .setPositiveButton(R.string.cerrar_sesion) { _, _ ->
                listenerRegistro?.remove()
                listenerRegistro = null
                auth.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton(R.string.cancelar, null)
            .show()
    }

    private fun confirmarEliminacion(destino: Destino) {
        AlertDialog.Builder(this)
            .setTitle(R.string.confirmar_eliminacion_titulo)
            .setMessage(getString(R.string.confirmar_eliminacion_mensaje, destino.nombre))
            .setPositiveButton(R.string.eliminar) { _, _ -> eliminarDestino(destino) }
            .setNegativeButton(R.string.cancelar, null)
            .show()
    }

    private fun eliminarDestino(destino: Destino) {
        db.collection("destinos").document(destino.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, R.string.destino_eliminado, Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerRegistro?.remove()
        listenerRegistro = null
    }
}