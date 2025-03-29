package com.ace.playstation.auth

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ace.playstation.MainActivity
import com.ace.playstation.R
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var signinButton: Button
    private lateinit var signupButton: TextView

    private val supabase: SupabaseClient = SupabaseClientInstance.getClient()
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE)

        // Jika sudah login, langsung ke MainActivity
        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // Inisialisasi elemen UI
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        signinButton = findViewById(R.id.loginButton)
        signupButton = findViewById(R.id.registerLink)

        signinButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                showErrorDialog("Harap isi semua data!")
            }
        }

        signupButton.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
            finish()
        }
    }

    private fun loginUser(email: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = supabase.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }

                withContext(Dispatchers.Main) {
                    if (result != null) {
                        // Simpan status login di SharedPreferences
                        sharedPreferences.edit().putBoolean("isLoggedIn", true).apply()

                        Toast.makeText(this@LoginActivity, "Login Berhasil!", Toast.LENGTH_LONG).show()
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    val errorMessage = when {
                        e.message.orEmpty().contains("invalid login credentials", ignoreCase = true) ->
                            "Email atau password salah"
                        e.message.orEmpty().contains("user not found", ignoreCase = true) ->
                            "Akun tidak ditemukan"
                        e.message.orEmpty().contains("invalid email", ignoreCase = true) ->
                            "Format email tidak valid"
                        e.message.orEmpty().contains("user disabled", ignoreCase = true) ->
                            "Akun anda dinonaktifkan"
                        else -> "Login gagal. Silakan coba lagi"
                    }
                    showErrorDialog(errorMessage)
                }
            }
        }
    }

    private fun showErrorDialog(message: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_error)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val dialogTitle: TextView = dialog.findViewById(R.id.dialogTitle)
        val dialogMessage: TextView = dialog.findViewById(R.id.dialogMessage)
        val closeButton: Button = dialog.findViewById(R.id.closeButton)

        dialogTitle.text = "Error"
        dialogMessage.text = message

        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
