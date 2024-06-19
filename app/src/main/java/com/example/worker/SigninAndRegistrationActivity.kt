package com.example.worker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.worker.databinding.ActivitySigninAndRegistrationBinding
import com.example.worker.register.WelcomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class SigninAndRegistrationActivity : AppCompatActivity() {
    private val binding: ActivitySigninAndRegistrationBinding by lazy {
        ActivitySigninAndRegistrationBinding.inflate(layoutInflater)
    }
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://worker-3391d-default-rtdb.asia-southeast1.firebasedatabase.app")
        storage = FirebaseStorage.getInstance()

        // Get the action from the intent extras
        val action = intent.getStringExtra("action")

        // Adjust visibility for login
        if (action == "login") {
            binding.loginEmailAddress.visibility = View.VISIBLE
            binding.loginPassword.visibility = View.VISIBLE
            binding.loginButton.visibility = View.VISIBLE

            binding.registerButton.isEnabled = false
            binding.registerButton.alpha = 0.5f
            binding.cardView.visibility = View.GONE
            binding.registerName.visibility = View.GONE
            binding.registerEmail.visibility = View.GONE
            binding.registerPassword.visibility = View.GONE
            binding.registerNewHere.isEnabled = false
            binding.registerNewHere.alpha = 0.5f

            binding.loginButton.setOnClickListener {
                val loginEmail = binding.loginEmailAddress.text.toString()
                val loginPassword = binding.loginPassword.text.toString()
                if (loginEmail.isEmpty() || loginPassword.isEmpty()) {
                    Toast.makeText(this, "Please Fill All The Details", Toast.LENGTH_SHORT).show()
                } else {
                    auth.signInWithEmailAndPassword(loginEmail, loginPassword)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            } else {
                                Toast.makeText(this, "Login Failed. Please Enter Correct Details", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
        } else if (action == "register") {
            binding.loginButton.isEnabled = false
            binding.loginButton.alpha = 0.5f

            binding.registerButton.setOnClickListener {
                // Get data from edit text fields
                val registerName = binding.registerName.text.toString()
                val registerEmail = binding.registerEmail.text.toString()
                val registerPassword = binding.registerPassword.text.toString()
                if (registerName.isEmpty() || registerEmail.isEmpty() || registerPassword.isEmpty()) {
                    Toast.makeText(this, "Please Fill All The Details", Toast.LENGTH_SHORT).show()
                } else {
                    auth.createUserWithEmailAndPassword(registerEmail, registerPassword)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                user?.let {
                                    val userReference = database.getReference("users")
                                    val userId = user.uid
                                    val userData = UserData(registerName, registerEmail)

                                    userReference.child(userId).setValue(userData)
                                    // upload image to firebase storage
                                    val storageReference = storage.reference.child("profile_image/$userId.jpg")
                                    storageReference.putFile(imageUri!!)
                                        .addOnCompleteListener { uploadTask ->
                                            if (uploadTask.isSuccessful) {
                                                storageReference.downloadUrl.addOnSuccessListener { imageUrl ->
                                                    val imageUrlString = imageUrl.toString()
                                                    // Save the image URL to the realtime database
                                                    userReference.child(userId).child("profileImage").setValue(imageUrlString)
                                                    Glide.with(this)
                                                        .load(imageUrl)
                                                        .apply(RequestOptions.circleCropTransform())
                                                        .into(binding.registerUserImage)
                                                    // Sign out and redirect to WelcomeActivity
                                                    auth.signOut()
                                                    Toast.makeText(this, "User Registered Successfully", Toast.LENGTH_SHORT).show()
                                                    startActivity(Intent(this, WelcomeActivity::class.java))
                                                    finish()
                                                }
                                            } else {
                                                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                }
                            } else {
                                Toast.makeText(this, "Authentication Failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
        }

        // Set onClickListener for the choose image
        binding.cardView.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select image"), PICK_IMAGE_REQUEST)
        }
    }

    private fun uploadImage(userId: String) {
        imageUri?.let { uri ->
            val storageReference: StorageReference = storage.reference.child("profile_image/$userId.jpg")
            storageReference.putFile(uri).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "User Registered Successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Registration Successful but Failed to Upload Image", Toast.LENGTH_SHORT).show()
                }
            }
        } ?: run {
            Toast.makeText(this, "Registration Successful but No Image Selected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            Glide.with(this)
                .load(imageUri)
                .apply(RequestOptions.circleCropTransform())
                .into(binding.registerUserImage)
        }
    }
}

data class UserData(val name: String, val email: String)
