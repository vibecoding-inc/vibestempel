package com.vibestempel.app

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.UUID

class AdminDashboardActivity : AppCompatActivity() {
    
    private lateinit var storage: StempelStorage
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)
        
        storage = StempelStorage(this)
        
        val eventNameInput = findViewById<EditText>(R.id.eventNameInput)
        val eventDescInput = findViewById<EditText>(R.id.eventDescInput)
        val generateButton = findViewById<Button>(R.id.generateButton)
        val qrCodeImage = findViewById<ImageView>(R.id.qrCodeImage)
        val currentTokenValue = findViewById<TextView>(R.id.currentTokenValue)
        val logoutButton = findViewById<Button>(R.id.logoutButton)
        
        // Display current admin token
        currentTokenValue.text = storage.getAdminToken()
        
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
            
            val qrBitmap = QRCodeGenerator.generateQRCode(event)
            qrCodeImage.setImageBitmap(qrBitmap)
            qrCodeImage.visibility = View.VISIBLE
            
            Toast.makeText(this, R.string.qr_generated, Toast.LENGTH_SHORT).show()
        }
        
        logoutButton.setOnClickListener {
            finish()
        }
    }
}
