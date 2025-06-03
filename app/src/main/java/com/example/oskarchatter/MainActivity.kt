package com.example.oskarchatter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var imageView: ImageView // avatar
    private var imageURL: String = ""
    private lateinit var imageUri2: Uri
    private var imagePicked = false

    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val data: Intent? = it.data
            val imageUri = data?.data
            if (imageUri != null) {
                imageUri2 = imageUri
                imageView.setImageURI(imageUri)
                imagePicked = true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        val emailEdit: EditText = findViewById(R.id.emailEditText)
        val passwordEdit: EditText = findViewById(R.id.passwordEditText)
        val registerButton: Button = findViewById(R.id.goToHome)
        val goToLogin: TextView = findViewById(R.id.goToLogin)
        imageView = findViewById(R.id.imageView)

        Picasso.get().load("https://i.imgur.com/DvpvklR.png").into(imageView)

        imageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            getContent.launch(intent)
        }

        goToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        registerButton.setOnClickListener {
            val emailText = emailEdit.text.toString()
            val passwordText = passwordEdit.text.toString()
            val shake = AnimationUtils.loadAnimation(this, R.anim.shake)
            val defaultBackground = emailEdit.background

            if (emailText.isEmpty() || passwordText.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                emailEdit.startAnimation(shake)
                passwordEdit.startAnimation(shake)
                passwordEdit.background = ContextCompat.getDrawable(this, R.drawable.error_input)
                return@setOnClickListener
            } else {
                emailEdit.background = defaultBackground
                passwordEdit.background = defaultBackground
            }


            auth.createUserWithEmailAndPassword(emailText, passwordText)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        Toast.makeText(baseContext, "Hello ${user?.email}", Toast.LENGTH_SHORT).show()
                        createUserInDB(this, user)
                    } else {
                        emailEdit.startAnimation(shake)
                        passwordEdit.startAnimation(shake)
                        emailEdit.background = ContextCompat.getDrawable(this, R.drawable.error_input)
                        passwordEdit.background = ContextCompat.getDrawable(this, R.drawable.error_input)

                        when (val e = task.exception) {
                            is FirebaseAuthUserCollisionException -> {
                                emailEdit.error = "This email is already registered."
                            }
                            is FirebaseAuthWeakPasswordException -> {
                                passwordEdit.error = "Password must be at least 6 characters."
                            }
                            is FirebaseAuthInvalidCredentialsException -> {
                                emailEdit.error = "Invalid email format."
                            }
                            else -> {
                                val errorMsg = e?.localizedMessage ?: "Authentication failed."
                                Toast.makeText(baseContext, errorMsg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
        }
    }

    private fun createUserInDB(context: Context, user: FirebaseUser?) {
        if (user == null) {
            Log.e("Auth", "User is null after sign-up")
            return
        }

        if (!imagePicked) {
            imageURL = "https://i.imgur.com/DvpvklR.png"
            saveUserToFirestore(context, user)
        } else {
            val storageReference = FirebaseStorage.getInstance().reference
            val imageRef = storageReference.child("images/${UUID.randomUUID()}.jpg")

            imageRef.putFile(imageUri2)
                .addOnSuccessListener {
                    imageRef.downloadUrl
                        .addOnSuccessListener { uri ->
                            imageURL = uri.toString()
                            saveUserToFirestore(context, user)
                        }
                        .addOnFailureListener { e ->
                            Log.e("Storage", "Failed to get download URL: ${e.message}")
                            Toast.makeText(context, "Failed to upload image URL", Toast.LENGTH_SHORT).show()
                            // Save without avatar URL fallback
                            imageURL = "https://i.imgur.com/DvpvklR.png"
                            saveUserToFirestore(context, user)
                        }
                }
                .addOnFailureListener {
                    Log.e("Storage", "Image upload failed: ${it.message}")
                    Toast.makeText(context, "Image upload failed", Toast.LENGTH_SHORT).show()
                    imageURL = "https://i.imgur.com/DvpvklR.png"
                    saveUserToFirestore(context, user)
                }
        }
    }

    private fun saveUserToFirestore(context: Context, user: FirebaseUser) {
        val db = Firebase.firestore
        val newUser = hashMapOf(
            "email" to (user.email ?: ""),
            "avatar" to imageURL
        )

        db.collection("users").document(user.uid)
            .set(newUser)
            .addOnSuccessListener {
                Log.d("Firestore", "User saved successfully!")
                val intentRegister = Intent(context, HomeActivity::class.java)
                context.startActivity(intentRegister)
                if (context is Activity) {
                    context.finish()
                }
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error writing document", e)
                Toast.makeText(context, "Failed to save user data", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d("Main", "Zalogowany")
            Toast.makeText(baseContext, "Zalogowany", Toast.LENGTH_SHORT).show()
            startActivity(Intent(baseContext, HomeActivity::class.java))
            finish()
        } else {
            Log.d("Main", "Nie zalogowany")
        }
    }
}
