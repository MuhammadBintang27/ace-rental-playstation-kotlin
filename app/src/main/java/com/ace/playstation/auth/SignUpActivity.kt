package com.ace.playstation.auth

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ace.playstation.R
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.UnknownHostException


class SignupActivity : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var confirmPasswordInput: EditText
    private lateinit var signupButton: Button
    private lateinit var loginButton: TextView

    private val supabase: SupabaseClient = SupabaseClientInstance.getClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Inisialisasi elemen UI
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput)
        signupButton = findViewById(R.id.registerButton)
        loginButton = findViewById(R.id.loginLink)

        signupButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val confirmPassword = confirmPasswordInput.text.toString().trim()

            // Validasi input sebelum mendaftar
            when {
                email.isEmpty() -> {
                    emailInput.error = "Email tidak boleh kosong"
                    emailInput.requestFocus()
                    return@setOnClickListener
                }
                !isValidEmail(email) -> {
                    emailInput.error = "Format email tidak valid"
                    emailInput.requestFocus()
                    return@setOnClickListener
                }
                password.isEmpty() -> {
                    passwordInput.error = "Password tidak boleh kosong"
                    passwordInput.requestFocus()
                    return@setOnClickListener
                }
                password.length < 8 -> {
                    passwordInput.error = "Password minimal 8 karakter"
                    passwordInput.requestFocus()
                    return@setOnClickListener
                }
                confirmPassword.isEmpty() -> {
                    confirmPasswordInput.error = "Konfirmasi password tidak boleh kosong"
                    confirmPasswordInput.requestFocus()
                    return@setOnClickListener
                }
                password != confirmPassword -> {
                    confirmPasswordInput.error = "Password dan Konfirmasi Password tidak cocok"
                    confirmPasswordInput.requestFocus()
                    return@setOnClickListener
                }
                else -> registerUser(email, password)
            }
        }

        loginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\$")
        return emailRegex.matches(email)
    }

    private fun registerUser(email: String, password: String) {
        // Tampilkan loading atau disable tombol saat proses registrasi
        signupButton.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = supabase.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password

                }

                runOnUiThread {
                    handleSignupResult(result, email)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    handleSignupError(e)
                }
            } finally {
                runOnUiThread {
                    signupButton.isEnabled = true
                }
            }
        }
    }

    private fun handleSignupResult(result: Any?, email: String) {
        // Periksa apakah result adalah null atau tidak
        if (result == null) {
            Toast.makeText(this, "Registrasi gagal, coba lagi.", Toast.LENGTH_SHORT).show()
            return
        }

        // Convert result ke string untuk pemeriksaan lebih lanjut
        val resultString = result.toString().lowercase()

        when {
            // Cek apakah result mengandung email yang didaftarkan
            resultString.contains(email.lowercase()) -> {
                Toast.makeText(
                    this,
                    "Registrasi Berhasil! Silakan cek email untuk verifikasi.",
                    Toast.LENGTH_LONG
                ).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            // Tambahkan kondisi tambahan jika diperlukan
            else -> {
                Toast.makeText(
                    this,
                    "Registrasi tidak berhasil. Silakan coba lagi.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun handleSignupError(e: Exception) {
        val errorMessage = when (e) {
            is UnknownHostException -> "Tidak ada koneksi internet. Periksa koneksi Anda."
            is RestException -> when (e.statusCode) {
                400 -> "Permintaan tidak valid. Periksa kembali data Anda."
                401 -> "Otorisasi gagal. Mungkin email sudah terdaftar."
                403 -> "Akses ditolak. Coba lagi nanti."
                404 -> "Layanan tidak ditemukan. Hubungi dukungan."
                409 -> "Email sudah terdaftar. Silakan gunakan email lain atau login."
                422 -> "Email tidak valid atau sudah digunakan."
                429 -> "Terlalu banyak permintaan. Coba lagi nanti."
                500 -> "Kesalahan server. Coba lagi nanti."
                else -> "Terjadi kesalahan: ${e.message}"
            }
            else -> "Gagal mendaftar: ${e.message}"
        }

        showErrorDialog(errorMessage)
    }


    private fun showErrorDialog(message: String) {
        val dialog = Dialog(this)
        dialog.setContentView(com.ace.playstation.R.layout.dialog_error)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val dialogTitle: TextView = dialog.findViewById(com.ace.playstation.R.id.dialogTitle)
        val dialogMessage: TextView = dialog.findViewById(com.ace.playstation.R.id.dialogMessage)
        val closeButton: Button = dialog.findViewById(com.ace.playstation.R.id.closeButton)

        dialogTitle.text = "Error"
        dialogMessage.text = message

        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}