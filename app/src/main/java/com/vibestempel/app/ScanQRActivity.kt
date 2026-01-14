package com.vibestempel.app

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import kotlinx.coroutines.launch

class ScanQRActivity : AppCompatActivity() {
    
    private lateinit var barcodeView: DecoratedBarcodeView
    private lateinit var storage: SupabaseStorage
    
    private val callback = object : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult?) {
            result?.let {
                handleQRCodeScanned(it.text)
            }
        }
        
        override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {
            // Not needed
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        storage = SupabaseStorage(this)
        
        barcodeView = DecoratedBarcodeView(this)
        barcodeView.decodeContinuous(callback)
        
        setContentView(barcodeView)
    }
    
    override fun onResume() {
        super.onResume()
        barcodeView.resume()
    }
    
    override fun onPause() {
        super.onPause()
        barcodeView.pause()
    }
    
    private fun handleQRCodeScanned(qrContent: String) {
        barcodeView.pause()
        
        val event = QRCodeGenerator.parseQRCode(qrContent)
        if (event == null) {
            Toast.makeText(this, R.string.invalid_qr_code, Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        // Add stamp via MCP server
        lifecycleScope.launch {
            val result = storage.addStamp(event.id, event.name)
            if (result.isSuccess && result.getOrDefault(false)) {
                showCelebrationDialog(event.name)
            } else if (result.isSuccess && !result.getOrDefault(false)) {
                Toast.makeText(this@ScanQRActivity, R.string.stamp_already_exists, Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(
                    this@ScanQRActivity,
                    "Failed to collect stamp: ${result.exceptionOrNull()?.message}",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }
    
    private fun showCelebrationDialog(eventName: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_celebration)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)
        
        // Get the card view parent and animate it
        val cardView = dialog.findViewById<CardView>(R.id.celebrationCard)
        cardView?.let {
            val scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.stamp_collect_scale)
            it.startAnimation(scaleAnimation)
        }
        
        val okButton = dialog.findViewById<MaterialButton>(R.id.celebrationOkButton)
        okButton.setOnClickListener {
            dialog.dismiss()
            setResult(Activity.RESULT_OK)
            finish()
        }
        
        dialog.show()
    }
}
