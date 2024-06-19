package com.example.worker

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.worker.Model.BlogItemModel
import com.example.worker.Model.UserData
import com.example.worker.databinding.ActivityAddArticleBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date

class AddArticleActivity : AppCompatActivity() {
    private val binding: ActivityAddArticleBinding by lazy {
        ActivityAddArticleBinding.inflate(layoutInflater)
    }

    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance("https://worker-3391d-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("blogs")
    private val userReference: DatabaseReference = FirebaseDatabase.getInstance("https://worker-3391d-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("users")
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        binding.imageButton2.setOnClickListener{
            finish()
        }

        binding.saveBlogButton.setOnClickListener {
            val title = binding.blogTitle.editText?.text.toString().trim()
            val description = binding.blogDescription.editText?.text.toString().trim()

            if (title.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Please Fill All The Fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Get current user
            val user: FirebaseUser? = auth.currentUser

            if (user != null) {
                val userId = user.uid

                // Fetch user name and user profile from database
                userReference.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userData = snapshot.getValue(UserData::class.java)
                        if (userData != null) {
                            val userNameFromDB = userData.name
                            val userImageUrlFromDB = userData.profileImage

                            val currentDate = SimpleDateFormat("yyyy-MM-dd").format(Date())

                            // Create a BlogItemModel
                            val blogItem = BlogItemModel(
                                title,
                                userNameFromDB,
                                currentDate,
                                description,
                                userId,
                                0,
                                userImageUrlFromDB
                            )

                            // Generate a unique key for the blog post
                            val key = databaseReference.push().key
                            if (key != null) {
                                blogItem.postId = key
                                val blogReference = databaseReference.child(key)
                                blogReference.setValue(blogItem).addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        finish()
                                    } else {
                                        Toast.makeText(this@AddArticleActivity, "Failed to Add Article", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle database error
                        Toast.makeText(this@AddArticleActivity, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
