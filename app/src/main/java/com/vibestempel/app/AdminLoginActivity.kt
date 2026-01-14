package com.vibestempel.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AdminLoginActivity : AppCompatActivity() {
    
    private lateinit var storage: MCPStorage
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_login)
        
        storage = MCPStorage(this)
        
        val tokenInput = findViewById<EditText>(R.id.tokenInput)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val backButton = findViewById<Button>(R.id.backButton)
        
        loginButton.setOnClickListener {
            val token = tokenInput.text.toString()
            if (storage.validateAdminToken(token)) {
                val intent = Intent(this, AdminDashboardActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, R.string.invalid_token, Toast.LENGTH_SHORT).show()
            }
        }
        
        backButton.setOnClickListener {
            finish()
        }
    }
}
