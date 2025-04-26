package com.ace.playstation.auth

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ace.playstation.MainActivity
import com.ace.playstation.R
import com.ace.playstation.AdminActivity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

class LoginActivity : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var signinButton: Button
    private lateinit var signupButton: TextView

    private val supabase: SupabaseClient = SupabaseClientInstance.getClient()
    private lateinit var sharedPreferences: SharedPreferences

    @Serializable
    data class UserRole(
        val id: String,
        val email: String? = null,
        val role: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE)

        // Jika sudah login, langsung ke MainActivity atau AdminActivity sesuai role
        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            val isAdmin = sharedPreferences.getBoolean("isAdmin", false)
            if (isAdmin) {
                startActivity(Intent(this, AdminActivity::class.java))
            } else {
                startActivity(Intent(this, MainActivity::class.java))
            }
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
                showErrorMessage("Harap isi semua data!")
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

                // After successful login, check user role from the database
                if (result != null) {
                    val userId = supabase.auth.currentUserOrNull()?.id
                    if (userId != null) {
                        val userRole = getUserRole(userId)

                        withContext(Dispatchers.Main) {
                            // Simpan status login di SharedPreferences
                            val editor = sharedPreferences.edit()
                            editor.putBoolean("isLoggedIn", true)

                            // Check if user is admin and save to SharedPreferences
                            val isAdmin = userRole?.role == "admin"
                            editor.putBoolean("isAdmin", isAdmin)
                            editor.putString("userRole", userRole?.role)
                            editor.putString("userId", userId)
                            editor.apply()

                            Toast.makeText(this@LoginActivity, "Login Berhasil!", Toast.LENGTH_LONG).show()

                            // Redirect based on user role
                            val intent = if (isAdmin) {
                                Intent(this@LoginActivity, AdminActivity::class.java)
                            } else {
                                Intent(this@LoginActivity, MainActivity::class.java)
                            }

                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
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
                        else -> "Login gagal: ${e.message}"
                    }
                    showErrorMessage(errorMessage)
                }
            }
        }
    }

    // Using Toast instead of Dialog for error messages to avoid potential null view issues
    private fun showErrorMessage(message: String) {
        runOnUiThread {
            Toast.makeText(this@LoginActivity, message, Toast.LENGTH_LONG).show()
            Log.d("ERROR!!!", message)
        }
    }

    private suspend fun getUserRole(userId: String): UserRole? {
        return try {
            val response = supabase.postgrest["user"]
                .select {
                    filter {
                        eq("id", userId)
                    }
                }

            val users = response.decodeList<UserRole>()
            return if (users.isNotEmpty()) users[0] else null
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                showErrorMessage("Error fetching user role: ${e.message}")
            }
            null
        }
    }

    // Keep this method for other parts of code that might use it, but make it safer
    private fun showErrorDialog(message: String) {
        runOnUiThread {
            try {
                val dialog = Dialog(this)
                dialog.setContentView(R.layout.dialog_error)
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                val dialogTitle: TextView? = dialog.findViewById(R.id.dialogTitle)
                val dialogMessage: TextView? = dialog.findViewById(R.id.dialogMessage)
                val closeButton: Button? = dialog.findViewById(R.id.closeButton)

                if (dialogTitle != null && dialogMessage != null && closeButton != null) {
                    dialogTitle.text = "Error"
                    dialogMessage.text = message

                    closeButton.setOnClickListener {
                        dialog.dismiss()
                    }

                    if (!isFinishing) {
                        dialog.show()
                    }
                } else {
                    // Fallback to Toast if dialog elements not found
                    Toast.makeText(this@LoginActivity, message, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                // Ultimate fallback
                Toast.makeText(this@LoginActivity, message, Toast.LENGTH_LONG).show()
            }
        }
    }
}