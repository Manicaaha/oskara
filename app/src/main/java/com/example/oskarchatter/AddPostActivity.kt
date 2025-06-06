package com.example.oskarchatter

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.util.UUID

class AddPostActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    private lateinit var editText: EditText
    private lateinit var postBtn: Button
    private lateinit var usernameText: TextView
    private lateinit var profileImage: ImageView
    private lateinit var addPhotoImageView: ImageView
    private lateinit var image1: ImageView
    private lateinit var image2: ImageView
    private lateinit var cancelBtn: ImageView

    private var selectedImageUris = mutableListOf<Uri>()

    private val pickImagesLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris != null) {
            selectedImageUris.clear()
            selectedImageUris.addAll(uris.take(2))
            updateImageViews()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        editText = findViewById(R.id.editTextText)
        postBtn = findViewById(R.id.submitPostButton)
        usernameText = findViewById(R.id.textView3)
        profileImage = findViewById(R.id.userProfileImage)
        addPhotoImageView = findViewById(R.id.add_photo_ImageView)
        image1 = findViewById(R.id.selectedImageView1)
        image2 = findViewById(R.id.selectedImageView2)
        cancelBtn = findViewById(R.id.cancel)

        val currentUser = auth.currentUser

        cancelBtn.setOnClickListener {
            startActivity(Intent(this, PostsActivity::class.java))
            finish()
        }

        currentUser?.email?.let {
            val cleanedEmail = it.split(".").first()
            val usernamePart = cleanedEmail.substringBefore("@")
            val formattedUsername = usernamePart.replaceFirstChar { ch -> ch.uppercaseChar() }
            usernameText.text = formattedUsername
        }

        currentUser?.uid?.let { uid ->
            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    val avatarUrl = doc.getString("avatar")
                    if (!avatarUrl.isNullOrEmpty()) {
                        Picasso.get().load(avatarUrl)
                            .placeholder(R.drawable.default_avatar)
                            .error(R.drawable.default_avatar)
                            .into(profileImage)
                    }
                }
        }

        addPhotoImageView.setOnClickListener {
            pickImagesLauncher.launch("image/*")
        }

        postBtn.setOnClickListener {
            val content = editText.text.toString().trim()
            if (content.isEmpty()) {
                Toast.makeText(this, "Please enter some content", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            uploadImagesAndPost(content)

        }
    }

    private fun updateImageViews() {
        image1.setImageURI(null)
        image2.setImageURI(null)

        if (selectedImageUris.isNotEmpty()) {
            image1.setImageURI(selectedImageUris[0])
        }
        if (selectedImageUris.size > 1) {
            image2.setImageURI(selectedImageUris[1])
        }
    }

    private fun uploadImagesAndPost(content: String) {
        val currentUser = auth.currentUser ?: return
        val uid = currentUser.uid
        val email = currentUser.email ?: ""
        val username = email.substringBefore("@").replaceFirstChar { it.uppercaseChar() }

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val avatarUrl = doc.getString("avatar") ?: ""
                if (selectedImageUris.isEmpty()) {
                    savePostToFirestore(content, username, avatarUrl, listOf())
                } else {
                    uploadToFirebaseStorage(selectedImageUris) { imageUrls ->
                        savePostToFirestore(content, username, avatarUrl, imageUrls)
                    }
                }
            }
    }

    private fun uploadToFirebaseStorage(
        uris: List<Uri>,
        onComplete: (List<String>) -> Unit
    ) {
        val imageUrls = mutableListOf<String>()
        val totalImages = uris.size

        for ((index, uri) in uris.withIndex()) {
            val fileName = UUID.randomUUID().toString()
            val ref = storage.reference.child("posts/$fileName.jpg")
            ref.putFile(uri)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { downloadUri ->
                        imageUrls.add(downloadUri.toString())
                        if (imageUrls.size == totalImages) {
                            onComplete(imageUrls)
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun savePostToFirestore(
        content: String,
        username: String,
        avatarUrl: String,
        imageUrls: List<String>
    ) {
        val post = hashMapOf(
            "username" to username,
            "content" to content,
            "avatarUrl" to avatarUrl,
            "imageUrls" to imageUrls,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("posts").add(post)
            .addOnSuccessListener {
                Toast.makeText(this, "Post added!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, PostsActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
