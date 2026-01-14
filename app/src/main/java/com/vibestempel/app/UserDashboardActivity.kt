package com.vibestempel.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class UserDashboardActivity : AppCompatActivity() {
    
    private lateinit var storage: MCPStorage
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
        
        storage = MCPStorage(this)
        
        stampsRecyclerView = findViewById(R.id.stampsRecyclerView)
        noStampsText = findViewById(R.id.noStampsText)
        stampsCountText = findViewById(R.id.stampsCountText)
        val scanButton = findViewById<Button>(R.id.scanButton)
        val backButton = findViewById<Button>(R.id.backButton)
        
        setupRecyclerView()
        updateStampsList()
        
        scanButton.setOnClickListener {
            if (checkCameraPermission()) {
                startQRScanner()
            } else {
                requestCameraPermission()
            }
        }
        
        backButton.setOnClickListener {
            finish()
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
}
