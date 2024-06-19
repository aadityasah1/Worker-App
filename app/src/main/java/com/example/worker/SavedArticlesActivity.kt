package com.example.worker

import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.worker.Model.BlogItemModel
import com.example.worker.adapter.WorkerAdapter
import com.example.worker.databinding.ActivitySavedArticlesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SavedArticlesActivity : AppCompatActivity() {
    private val binding: ActivitySavedArticlesBinding by lazy {
        ActivitySavedArticlesBinding.inflate(layoutInflater)
    }

    private val savedBlogsArticles = mutableListOf<BlogItemModel>()
    private lateinit var workerAdapter: WorkerAdapter
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        enableEdgeToEdge()

        // Initialize workAdapter
        workerAdapter = WorkerAdapter(mutableListOf())
        val recyclerView = binding.savedArticleRecyclerview
        recyclerView.adapter = workerAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userReference = FirebaseDatabase.getInstance("https://worker-3391d-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users").child(userId).child("saveBlogPosts")

            userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach { postSnapshot ->
                        val postId = postSnapshot.key
                        val isSaved = postSnapshot.getValue(Boolean::class.java) ?: false
                        if (postId != null && isSaved) {
                            CoroutineScope(Dispatchers.IO).launch {
                                val blogItem = fetchBlogItem(postId)
                                if (blogItem != null) {
                                    savedBlogsArticles.add(blogItem)
                                    launch(Dispatchers.Main) {
                                        workerAdapter.updateData(savedBlogsArticles.toMutableList())
                                    }
                                }
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("SavedArticlesActivity", "Database error: ${error.message}")
                }
            })
        }

        binding.backButton.setOnClickListener {
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private suspend fun fetchBlogItem(postId: String): BlogItemModel? {
        val blogReference = FirebaseDatabase.getInstance("https://worker-3391d-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("blogs")
        return try {
            val dataSnapshot = blogReference.child(postId).get().await()
            dataSnapshot.getValue(BlogItemModel::class.java)
        } catch (e: Exception) {
            Log.e("SavedArticlesActivity", "Error fetching blog item: ${e.message}")
            null
        }
    }
}
