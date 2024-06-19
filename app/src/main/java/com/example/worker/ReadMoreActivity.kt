package com.example.worker

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.worker.Model.BlogItemModel
import com.example.worker.databinding.ActivityReadMoreBinding

class ReadMoreActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReadMoreBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReadMoreBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            finish()
        }

        val blogs = intent.getParcelableExtra<BlogItemModel>("blogItem")

        if (blogs != null) {
            // Retrieve user related data here, e.g., blog title, etc.
            binding.titleText.text = blogs.heading
            binding.userName.text = blogs.userName
            binding.date.text = blogs.date
            binding.blogDescriptionTextView.text = blogs.post

            val userImageUrl = blogs.profileImage
            Glide.with(this)
                .load(userImageUrl)
                .apply(RequestOptions.circleCropTransform())
                .into(binding.profileImage)

        } else {
            Toast.makeText(this, "Failed to load job", Toast.LENGTH_SHORT).show()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
