package com.example.worker

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.worker.Model.BlogItemModel
import com.example.worker.databinding.ActivityEditBlogBinding
import com.google.firebase.database.FirebaseDatabase

class EditBlogActivity : AppCompatActivity() {
    private val binding : ActivityEditBlogBinding by lazy {
        ActivityEditBlogBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        binding.imageButton2.setOnClickListener {
            finish()
        }
        val blogItemModel = intent.getParcelableExtra<BlogItemModel>("blogItem")

        binding.blogTitle.editText?.setText(blogItemModel?.heading)
        binding.blogDescription.editText?.setText(blogItemModel?.post)

        binding.saveBlogButton.setOnClickListener {
            val updatedTitle = binding.blogTitle.editText?.text.toString().trim()
            val updatedDescription = binding.blogDescription.editText?.text.toString().trim()

            if (updatedTitle.isEmpty() || updatedDescription.isEmpty()){
                Toast.makeText(this, "Please Fill All The Details", Toast.LENGTH_SHORT).show()
            }else{
                blogItemModel?.heading = updatedTitle
                blogItemModel?.post = updatedDescription

                if (blogItemModel != null){
                    updateDataInFirebase(blogItemModel)
                }
            }

        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun updateDataInFirebase(blogItemModel: BlogItemModel) {
        val databaseReference = FirebaseDatabase.getInstance("https://worker-3391d-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("blogs")
        val postId = blogItemModel.postId
        databaseReference.child(postId).setValue(blogItemModel)
            .addOnSuccessListener {
                Toast.makeText(this, "Work Updated Successful", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Work  Updated Unsuccessful", Toast.LENGTH_SHORT).show()
            }
    }
}