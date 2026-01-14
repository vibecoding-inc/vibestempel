package com.vibestempel.app

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.util.UUID

class AdminDashboardActivity : AppCompatActivity() {
    
    private lateinit var storage: SupabaseStorage
    private lateinit var userStampCountAdapter: UserStampCountAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)
        
        storage = SupabaseStorage(this)
        
        val eventNameInput = findViewById<TextInputEditText>(R.id.eventNameInput)
        val eventDescInput = findViewById<TextInputEditText>(R.id.eventDescInput)
        val generateButton = findViewById<MaterialButton>(R.id.generateButton)
        val qrCodeCard = findViewById<CardView>(R.id.qrCodeCard)
        val qrCodeImage = findViewById<ImageView>(R.id.qrCodeImage)
        val currentTokenValue = findViewById<TextView>(R.id.currentTokenValue)
        val logoutButton = findViewById<MaterialButton>(R.id.logoutButton)
        
        // User statistics views
        val userStampsRecyclerView = findViewById<RecyclerView>(R.id.userStampsRecyclerView)
        val totalUsersText = findViewById<TextView>(R.id.totalUsersText)
        val noUsersText = findViewById<TextView>(R.id.noUsersText)
        
        // Display current admin token
        currentTokenValue.text = storage.getAdminToken()
        
        // Set up user stamp counts RecyclerView
        setupUserStatsRecyclerView(userStampsRecyclerView)
        
        // Load initial user stats
        loadUserStats(totalUsersText, noUsersText, userStampsRecyclerView)
        
        // Subscribe to realtime updates
        subscribeToRealtimeUpdates(totalUsersText, noUsersText, userStampsRecyclerView)
        
        generateButton.setOnClickListener {
            val eventName = eventNameInput.text.toString().trim()
            val eventDesc = eventDescInput.text.toString().trim()
            
            if (eventName.isEmpty() || eventDesc.isEmpty()) {
                Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val event = Event(
                id = UUID.randomUUID().toString(),
                name = eventName,
                description = eventDesc
            )
            
            // Create event on server
            lifecycleScope.launch {
                val result = storage.createEvent(event)
                if (result.isSuccess) {
                    // Generate QR code
                    val qrBitmap = QRCodeGenerator.generateQRCode(event)
                    qrCodeImage.setImageBitmap(qrBitmap)
                    qrCodeCard.visibility = View.VISIBLE
                    
                    // Animate the QR code card appearance
                    val scaleAnimation = AnimationUtils.loadAnimation(this@AdminDashboardActivity, R.anim.stamp_collect_scale)
                    qrCodeCard.startAnimation(scaleAnimation)
                    
                    Toast.makeText(this@AdminDashboardActivity, R.string.qr_generated, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        this@AdminDashboardActivity,
                        "Failed to create event: ${result.exceptionOrNull()?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        
        logoutButton.setOnClickListener {
            finish()
        }
    }
    
    private fun setupUserStatsRecyclerView(recyclerView: RecyclerView) {
        userStampCountAdapter = UserStampCountAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = userStampCountAdapter
    }
    
    private fun loadUserStats(totalUsersText: TextView, noUsersText: TextView, recyclerView: RecyclerView) {
        lifecycleScope.launch {
            val result = storage.getUserStampCounts()
            if (result.isSuccess) {
                val users = result.getOrDefault(emptyList())
                updateUserStatsUI(users, totalUsersText, noUsersText, recyclerView)
            }
        }
    }
    
    private fun subscribeToRealtimeUpdates(totalUsersText: TextView, noUsersText: TextView, recyclerView: RecyclerView) {
        lifecycleScope.launch {
            storage.subscribeToUserStampCounts()
            
            // Collect realtime updates
            storage.userStampCounts.collect { users ->
                updateUserStatsUI(users, totalUsersText, noUsersText, recyclerView)
            }
        }
    }
    
    private fun updateUserStatsUI(
        users: List<UserStampCount>,
        totalUsersText: TextView,
        noUsersText: TextView,
        recyclerView: RecyclerView
    ) {
        totalUsersText.text = "Total Users: ${users.size}"
        
        if (users.isEmpty()) {
            recyclerView.visibility = View.GONE
            noUsersText.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            noUsersText.visibility = View.GONE
            userStampCountAdapter.updateUsers(users)
        }
    }
}
