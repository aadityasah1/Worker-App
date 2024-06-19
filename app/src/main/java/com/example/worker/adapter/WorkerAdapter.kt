package com.example.worker.adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.worker.Model.BlogItemModel
import com.example.worker.R
import com.example.worker.ReadMoreActivity
import com.example.worker.databinding.BlogItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class WorkerAdapter(val items: MutableList<BlogItemModel>) : RecyclerView.Adapter<WorkerAdapter.BlogViewHolder>() {

    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance("https://worker-3391d-default-rtdb.asia-southeast1.firebasedatabase.app").reference
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlogViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = BlogItemBinding.inflate(inflater, parent, false)
        return BlogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BlogViewHolder, position: Int) {
        val blogItem = items[position]
        holder.bind(blogItem, position)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class BlogViewHolder(private val binding: BlogItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(blogItemModel: BlogItemModel, position: Int) {
            val postId = blogItemModel.postId
            val context = binding.root.context
            binding.heading.text = blogItemModel.heading
            Glide.with(binding.profile.context)
                .load(blogItemModel.profileImage)
                .into(binding.profile)
            binding.username.text = blogItemModel.userName
            binding.date.text = blogItemModel.date
            binding.post.text = blogItemModel.post
            binding.likecount.text = blogItemModel.likeCount.toString()

            // set on click listener
            binding.root.setOnClickListener {
                val context = binding.root.context
                val intent = Intent(context, ReadMoreActivity::class.java)
                intent.putExtra("blogItem", blogItemModel)
                context.startActivity(intent)
            }

            // check if the current user liked the post and update the like button image
            val postLikeReference = databaseReference.child("blogs").child(postId).child("likes")
            currentUser?.uid?.let { uid ->
                postLikeReference.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            binding.likebutton.setImageResource(R.drawable.heart_filled)
                        } else {
                            binding.likebutton.setImageResource(R.drawable.like)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle possible errors
                    }
                })
            }

            // handle like button clicks
            binding.likebutton.setOnClickListener {
                if (currentUser != null) {
                    handleLikeButtonClicked(postId, blogItemModel, binding, position)
                } else {
                    Toast.makeText(context, "You have to login first", Toast.LENGTH_SHORT).show()
                }
            }
            //set the initial icon based on the saved status
            val userReference = databaseReference.child("users").child(currentUser?.uid?:"")
            val postSaveReference = userReference.child("saveBlogPosts").child(postId)

            postSaveReference.addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        //if blog already saved
                        binding.postSaveButton.setImageResource(R.drawable.unsave_red)
                    }else
                    {
                        //if blog not saved yet
                        binding.postSaveButton.setImageResource(R.drawable.savee)

                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

            //Handle Save button clicks
            binding.postSaveButton.setOnClickListener {
                if (currentUser != null) {
                    handleButtonClicked(postId, blogItemModel, binding, position)
                } else {
                    Toast.makeText(context, "You have to login first", Toast.LENGTH_SHORT).show()
                }

            }
        }
    }


    private fun handleLikeButtonClicked(postId: String, blogItemModel: BlogItemModel, binding: BlogItemBinding, position: Int) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val userReference = databaseReference.child("users").child(currentUser.uid)
        val postLikeReference = databaseReference.child("blogs").child(postId).child("likes")

        postLikeReference.child(currentUser.uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    userReference.child("likes").child(postId).removeValue()
                        .addOnSuccessListener {
                            postLikeReference.child(currentUser.uid).removeValue()
                            blogItemModel.likedBy?.remove(currentUser.uid)
                            updateLikeButtonImage(binding, false)

                            //Decrement the like in the Database

                            val newLikeCount = blogItemModel.likeCount -1
                            blogItemModel.likeCount = newLikeCount
                            databaseReference.child("blogs").child(postId).child("likeCount").setValue(newLikeCount)

                            notifyItemChanged(position)
                        }
                        .addOnFailureListener { e ->
                            Log.e("LikeClicked", "Failed to unlike the blog $e")
                        }
                } else {
                    userReference.child("likes").child(postId).setValue(true)
                        .addOnSuccessListener {
                            postLikeReference.child(currentUser.uid).setValue(true)
                            blogItemModel.likedBy?.add(currentUser.uid)
                            updateLikeButtonImage(binding, true)

                            //Increment the like in the database
                            val newLikeCount = blogItemModel.likeCount +1
                            blogItemModel.likeCount = newLikeCount
                            databaseReference.child("blogs").child(postId).child("likeCount").setValue(newLikeCount)

                            notifyItemChanged(position)
                        }
                        .addOnFailureListener { e ->
                            Log.e("LikeClicked", "Failed to like the blog $e")
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle possible errors
            }
        })
    }

    private fun updateLikeButtonImage(binding: BlogItemBinding, liked: Boolean) {
        if (liked) {
            binding.likebutton.setImageResource(R.drawable.heart_filled)
        } else {
            binding.likebutton.setImageResource(R.drawable.like)
        }
    }

    private fun handleButtonClicked(
        postId: String,
        blogItemModel: BlogItemModel,
        binding: BlogItemBinding,
        position: Int
    ) {
        val userReference = databaseReference.child("users").child(currentUser!!.uid)
        userReference.child("saveBlogPosts").child(postId).addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    //The blog is currently saved, so un saved it
                    userReference.child("saveBlogPosts").child(postId).removeValue()
                        .addOnSuccessListener {
                            //Update the UI
                            val clickedBlogItem = items.find {it.postId == postId}
                            clickedBlogItem?.isSaved = false
                            notifyDataSetChanged()

                            val context = binding.root.context
                            Toast.makeText(context, "Blog Unsaved!", Toast.LENGTH_SHORT).show()
                        }.addOnFailureListener {
                            val context = binding.root.context
                            Toast.makeText(context, "Failed to UnSave The Blog", Toast.LENGTH_SHORT).show()
                        }
                    binding.postSaveButton.setImageResource(R.drawable.save_red)

                }else
                {
                    //the blog is not saved, so save it

                    userReference.child("saveBlogPosts").child(postId).setValue(true)
                        .addOnSuccessListener {
                            //Update the UI

                            val clickedBlogItem = items.find { it.postId == postId }
                            clickedBlogItem?.isSaved = true
                            notifyDataSetChanged()

                            val context = binding.root.context
                            Toast.makeText(context, "Blog saved!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener{
                            val context = binding.root.context
                            Toast.makeText(context, "Failed to save Blog", Toast.LENGTH_SHORT).show()
                        }
                    //change the save button icon
                    binding.postSaveButton.setImageResource(R.drawable.unsave_red)
                }


            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }

    fun updateData(savedBlogsArticles: MutableList<BlogItemModel>) {
        items.clear()
        items.addAll(savedBlogsArticles)
        notifyDataSetChanged()
    }

}
