package com.vibestempel.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class UserDashboardActivity : AppCompatActivity() {
    
    private lateinit var storage: SupabaseStorage
    private lateinit var stampsAdapter: StampsAdapter
    private lateinit var stampsRecyclerView: RecyclerView
    private lateinit var noStampsText: TextView
    private lateinit var stampsCountText: TextView
    
    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startQRScanner()
        } else {
            Toast.makeText(this, R.string.camera_permission_required, Toast.LENGTH_SHORT).show()
        }
    }
    
    private val scanQRLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            updateStampsList()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_dashboard)
        
        storage = SupabaseStorage(this)
        
        stampsRecyclerView = findViewById(R.id.stampsRecyclerView)
        noStampsText = findViewById(R.id.noStampsText)
        stampsCountText = findViewById(R.id.stampsCountText)
        val scanButton = findViewById<Button>(R.id.scanButton)
        val backButton = findViewById<Button>(R.id.backButton)
        val settingsButton = findViewById<MaterialButton>(R.id.settingsButton)
        
        setupRecyclerView()
        updateStampsList()
        
        scanButton.setOnClickListener {
            if (checkCameraPermission()) {
                startQRScanner()
            } else {
                requestCameraPermission()
            }
        }
        
        settingsButton.setOnClickListener {
            showUsernameDialog()
        }
        
        backButton.setOnClickListener {
            finish()
        }

        // Heimo's esoterischer Chakren-Check
        stampsCountText.setOnClickListener {
            val vibe = VibeChakraService.getRandomVibe()
            Toast.makeText(this, vibe, Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onResume() {
        super.onResume()
        updateStampsList()
    }
    
    private fun setupRecyclerView() {
        stampsAdapter = StampsAdapter()
        stampsRecyclerView.layoutManager = LinearLayoutManager(this)
        stampsRecyclerView.adapter = stampsAdapter
    }
    
    private fun updateStampsList() {
        lifecycleScope.launch {
            val result = storage.getStamps()
            if (result.isSuccess) {
                val stamps = result.getOrDefault(emptyList())
                stampsCountText.text = getString(R.string.stamps_count, stamps.size)
                
                if (stamps.isEmpty()) {
                    stampsRecyclerView.visibility = View.GONE
                    noStampsText.visibility = View.VISIBLE
                } else {
                    stampsRecyclerView.visibility = View.VISIBLE
                    noStampsText.visibility = View.GONE
                    stampsAdapter.updateStamps(stamps)
                }
            } else {
                Toast.makeText(
                    this@UserDashboardActivity,
                    "Failed to load stamps: ${result.exceptionOrNull()?.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun requestCameraPermission() {
        requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }
    
    private fun startQRScanner() {
        val intent = android.content.Intent(this, ScanQRActivity::class.java)
        scanQRLauncher.launch(intent)
    }
    
    private fun showUsernameDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_set_username, null)
        val usernameInput = dialogView.findViewById<EditText>(R.id.usernameInput)
        
        // Load current username
        lifecycleScope.launch {
            val result = storage.getUsername()
            if (result.isSuccess) {
                val currentUsername = result.getOrNull()
                if (!currentUsername.isNullOrBlank()) {
                    usernameInput.setText(currentUsername)
                }
            }
        }
        
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.set_username)
            .setView(dialogView)
            .setPositiveButton(R.string.save) { _, _ ->
                // Will be overridden below
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        
        dialog.show()
        
        // Override the positive button to prevent auto-dismiss
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val newUsername = usernameInput.text.toString().trim()
            if (newUsername.isEmpty()) {
                Toast.makeText(this, R.string.username_empty, Toast.LENGTH_SHORT).show()
            } else {
                saveUsername(newUsername, dialog)
            }
        }
    }
    
    private fun saveUsername(username: String, dialog: AlertDialog) {
        lifecycleScope.launch {
            val result = storage.updateUsername(username)
            if (result.isSuccess) {
                Toast.makeText(
                    this@UserDashboardActivity,
                    R.string.username_updated,
                    Toast.LENGTH_SHORT
                ).show()
                dialog.dismiss()
            } else {
                Toast.makeText(
                    this@UserDashboardActivity,
                    R.string.username_update_failed,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
