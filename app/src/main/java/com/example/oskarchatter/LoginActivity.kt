package com.example.oskarchatter

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()
        val loginEmailEdit: EditText = findViewById(R.id.emailLoginEditText)
        val loginPasswordEdit: EditText = findViewById(R.id.passwordLoginEditText)
        val loginButton: Button = findViewById(R.id.goToProfile)
        val goToRegister: TextView = findViewById(R.id.goToRegister)

        val shake = AnimationUtils.loadAnimation(this, R.anim.shake)
        val defaultBackground = loginEmailEdit.background

        goToRegister.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        loginButton.setOnClickListener {
            var hasError = false

            if (loginEmailEdit.text.isEmpty()) {
                loginEmailEdit.startAnimation(shake)
                loginEmailEdit.error = "Email is required"
                loginEmailEdit.background = ContextCompat.getDrawable(this, R.drawable.error_input)
                hasError = true
            } else {
                loginEmailEdit.background = defaultBackground
            }

            if (loginPasswordEdit.text.isEmpty()) {
                loginPasswordEdit.startAnimation(shake)
                loginPasswordEdit.error = "Password is required"
                loginPasswordEdit.background = ContextCompat.getDrawable(this, R.drawable.error_input)
                hasError = true
            } else {
                loginPasswordEdit.background = defaultBackground
            }

            if (hasError) return@setOnClickListener


            val loadingIntent = Intent(this, LoadingScreen::class.java)
            startActivity(loadingIntent)


            auth.signInWithEmailAndPassword(loginEmailEdit.text.toString(), loginPasswordEdit.text.toString())
                .addOnCompleteListener(this) { task ->

                    finish()

                    if (task.isSuccessful) {
                        Log.d("TAG", "signInWithEmail:success")
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        loginEmailEdit.startAnimation(shake)
                        loginPasswordEdit.startAnimation(shake)
                        loginEmailEdit.background = ContextCompat.getDrawable(this, R.drawable.error_input)
                        loginPasswordEdit.background = ContextCompat.getDrawable(this, R.drawable.error_input)

                        val errorMsg = task.exception?.localizedMessage ?: "Authentication failed."
                        when (task.exception) {
                            is FirebaseAuthInvalidUserException -> {
                                loginEmailEdit.error = "No account found with this email"
                                loginEmailEdit.requestFocus()
                            }
                            is FirebaseAuthInvalidCredentialsException -> {
                                loginPasswordEdit.error = "Incorrect password or invalid email format"
                                loginPasswordEdit.requestFocus()
                            }
                            else -> {
                                Toast.makeText(baseContext, errorMsg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
        }
    }
}
