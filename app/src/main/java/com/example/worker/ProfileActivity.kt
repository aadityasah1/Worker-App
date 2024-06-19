package com.example.worker

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.worker.databinding.ActivityProfileBinding
import com.example.worker.register.WelcomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue

class ProfileActivity : AppCompatActivity() {
    private val binding: ActivityProfileBinding by lazy {
        ActivityProfileBinding.inflate(layoutInflater)
    }
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private fun loadUserProfileData(userId: String) {
        val userReference = databaseReference.child(userId)

        //load user profile image
        userReference.child("profileImage").addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
               val profileImageUrl = snapshot.getValue(String::class.java)
                if (profileImageUrl != null){
                    Glide.with(this@ProfileActivity)
                        .load(profileImageUrl)
                        .into(binding.userProfile)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ProfileActivity, "Failed to load user image", Toast.LENGTH_SHORT).show()

            }
        })
        //load user name

        userReference.child("name").addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val userName = snapshot.getValue(String::class.java)

                if (userName != null){
                    binding.userName.text = userName
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        //to go add article page
        binding.addNewBlogButton.setOnClickListener {
            startActivity(Intent(this,AddArticleActivity::class.java))
        }
        //to go to your Article activity
        binding.articlesButton.setOnClickListener {
            startActivity(Intent(this,ArticleActivity::class.java))
        }
        //to logOut
        binding.logOutButton.setOnClickListener {
            auth.signOut()

            //navigate
            startActivity(Intent(this,WelcomeActivity::class.java))
            finish()
        }

        //Initialize Firebase
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance("https://worker-3391d-default-rtdb.asia-southeast1.firebasedatabase.app").reference.child("users")
        val userId = auth.currentUser?.uid
        if (userId != null){
            loadUserProfileData(userId)
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}