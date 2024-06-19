package com.example.worker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.worker.Model.BlogItemModel
import com.example.worker.adapter.WorkerAdapter
import com.example.worker.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var lastKnownLocation: LatLng
    private val blogItems = mutableListOf<BlogItemModel>()
    private lateinit var workerAdapter: WorkerAdapter

    private val requestLocationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                // Permission has been granted, proceed to get location
                getLastKnownLocation()
            } else {
                // Permission denied, handle accordingly (show message or disable location features)
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Initialize fused location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Check location permissions
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted, request it
            requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            // Permission has been granted, proceed to get location
            getLastKnownLocation()
        }

        // Set up recycler view
        setupRecyclerView()

        // Set up Firebase
        setupFirebase()

        // Set up UI interactions
        setupUI()
    }

    private fun setupRecyclerView() {
        workerAdapter = WorkerAdapter(blogItems)
        binding.workerRecyclerView.apply {
            adapter = workerAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }

    private fun setupFirebase() {
        auth = FirebaseAuth.getInstance()
        databaseReference =
            FirebaseDatabase.getInstance("https://worker-3391d-default-rtdb.asia-southeast1.firebasedatabase.app")
                .reference.child("blogs")

        val userId = auth.currentUser?.uid
        // Set user profile
        userId?.let { loadUserProfileImage(it) }

        // Fetch data from Firebase database
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                blogItems.clear()
                for (dataSnapshot in snapshot.children) {
                    val blogItem = dataSnapshot.getValue(BlogItemModel::class.java)
                    blogItem?.let { blogItems.add(it) }
                }
                // Notify the adapter that the data has changed
                blogItems.reverse()
                workerAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Blog loading failed", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupUI() {
        // Save article button click listener
        binding.saveArticleButton.setOnClickListener {
            startActivity(Intent(this, SavedArticlesActivity::class.java))
        }

        // Profile image click listener
        binding.profileImage.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // CardView click listener
        binding.cardView3.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Add blog button click listener
        binding.floatingAddBlogButton.setOnClickListener {
            startActivity(Intent(this, AddArticleActivity::class.java))
        }

        // Window insets listener
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun getLastKnownLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    lastKnownLocation = LatLng(location.latitude, location.longitude)
                    // Update UI with location details
                    fetchLocationName(location.latitude, location.longitude)
                } else {
                    Toast.makeText(this@MainActivity, "Location not available", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this@MainActivity, "Failed to get location: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchLocationName(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val cityName = addresses[0].locality // Example: Fetch city name
                binding.locationTextView.text = "Your Location: $cityName" // Update locationTextView
            } else {
                Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error fetching location name", e)
            Toast.makeText(this, "Error fetching location", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUserProfileImage(userId: String) {
        val userReference =
            FirebaseDatabase.getInstance("https://worker-3391d-default-rtdb.asia-southeast1.firebasedatabase.app")
                .reference.child("users").child(userId)
        userReference.child("profileImage").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val profileImageUrl = snapshot.getValue(String::class.java)

                if (profileImageUrl != null) {
                    Glide.with(this@MainActivity)
                        .load(profileImageUrl)
                        .into(binding.profileImage)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Error Loading Profile Image", Toast.LENGTH_SHORT).show()
            }
        })
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
