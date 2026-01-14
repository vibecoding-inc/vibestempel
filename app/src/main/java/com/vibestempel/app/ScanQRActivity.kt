package com.vibestempel.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
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
            Toast.makeText(this, R.string.stamp_received, Toast.LENGTH_LONG).show()
            setResult(Activity.RESULT_OK)
        } else {
            Toast.makeText(this, R.string.stamp_already_exists, Toast.LENGTH_SHORT).show()
        }
        
        finish()
    }
}
