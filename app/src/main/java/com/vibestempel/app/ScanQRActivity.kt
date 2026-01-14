package com.vibestempel.app

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView

class ScanQRActivity : AppCompatActivity() {
    
    private lateinit var barcodeView: DecoratedBarcodeView
    private lateinit var storage: StempelStorage
    
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
        
        storage = StempelStorage(this)
        
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
        
        val stamp = Stamp(
            eventId = event.id,
            eventName = event.name
        )
        
        val success = storage.addStamp(stamp)
        if (success) {
            showCelebrationDialog(event.name)
        } else {
            Toast.makeText(this, R.string.stamp_already_exists, Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    private fun showCelebrationDialog(eventName: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_celebration)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)
        
        val celebrationView = dialog.findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.celebrationEmoji)?.parent as? android.view.View
        celebrationView?.let {
            val scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.stamp_collect_scale)
            it.startAnimation(scaleAnimation)
        }
        
        val okButton = dialog.findViewById<Button>(R.id.celebrationOkButton)
        okButton.setOnClickListener {
            dialog.dismiss()
            setResult(Activity.RESULT_OK)
            finish()
        }
        
        dialog.show()
    }
}
