package com.example.agenciaviajes.activities
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.agenciaviajes.R
import com.example.agenciaviajes.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener { registrar() }
        binding.tvLoginLink.setOnClickListener { finish() }
    }

    private fun registrar() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirm = binding.etConfirmPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(this, R.string.error_campo_vacio, Toast.LENGTH_SHORT).show(); return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = getString(R.string.error_email_invalido); return
        }
        if (password.length < 6) {
            binding.tilPassword.error = getString(R.string.error_password_debil); return
        }
        if (password != confirm) {
            binding.tilConfirmPassword.error = getString(R.string.error_password_no_coincide); return
        }

        binding.btnRegister.isEnabled = false

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                binding.btnRegister.isEnabled = true
                if (task.isSuccessful) {
                    Toast.makeText(this, R.string.registro_exitoso, Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, CatalogoActivity::class.java))
                    finish()
                } else {
                    val mensaje = when (task.exception) {
                        is FirebaseAuthUserCollisionException ->
                            getString(R.string.error_email_registrado)
                        is FirebaseAuthWeakPasswordException ->
                            getString(R.string.error_password_debil)
                        else -> task.exception?.message ?: getString(R.string.error_desconocido)
                    }
                    Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
                }
            }
    }
}