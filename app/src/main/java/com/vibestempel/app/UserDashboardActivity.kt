package com.vibestempel.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class UserDashboardActivity : AppCompatActivity() {
    
    private lateinit var storage: StempelStorage
    private lateinit var stampsAdapter: StampsAdapter
    private lateinit var stampsRecyclerView: RecyclerView
    private lateinit var noStampsText: TextView
    private lateinit var stampsCountText: TextView
    
    companion object {
        private const val CAMERA_PERMISSION_REQUEST = 100
        private const val SCAN_QR_REQUEST = 101
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_dashboard)
        
        storage = StempelStorage(this)
        
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
        val stamps = storage.getStamps()
        stampsCountText.text = getString(R.string.stamps_count, stamps.size)
        
        if (stamps.isEmpty()) {
            stampsRecyclerView.visibility = View.GONE
            noStampsText.visibility = View.VISIBLE
        } else {
            stampsRecyclerView.visibility = View.VISIBLE
            noStampsText.visibility = View.GONE
            stampsAdapter.updateStamps(stamps)
        }
    }
    
    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST
        )
    }
    
    private fun startQRScanner() {
        val intent = Intent(this, ScanQRActivity::class.java)
        startActivityForResult(intent, SCAN_QR_REQUEST)
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startQRScanner()
            } else {
                Toast.makeText(this, R.string.camera_permission_required, Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SCAN_QR_REQUEST && resultCode == RESULT_OK) {
            updateStampsList()
        }
    }
}
