package com.example.agenciaviajes.activities
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.agenciaviajes.R
import com.example.agenciaviajes.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Persistencia de sesión
        if (auth.currentUser != null) {
            startActivity(Intent(this, CatalogoActivity::class.java))
            finish()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener { iniciarSesion() }
        binding.tvRegisterLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun iniciarSesion() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty()) {
            binding.tilEmail.error = getString(R.string.error_campo_vacio); return
        } else binding.tilEmail.error = null

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = getString(R.string.error_email_invalido); return
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = getString(R.string.error_campo_vacio); return
        } else binding.tilPassword.error = null

        binding.btnLogin.isEnabled = false

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                binding.btnLogin.isEnabled = true
                if (task.isSuccessful) {
                    startActivity(Intent(this, CatalogoActivity::class.java))
                    finish()
                } else {
                    val mensaje = when (task.exception) {
                        is FirebaseAuthInvalidCredentialsException ->
                            getString(R.string.error_credenciales_invalidas)
                        is FirebaseAuthInvalidUserException ->
                            getString(R.string.error_usuario_no_existe)
                        else -> task.exception?.message ?: getString(R.string.error_desconocido)
                    }
                    Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
                }
            }
    }
}