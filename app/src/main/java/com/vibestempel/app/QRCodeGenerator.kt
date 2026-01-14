package com.vibestempel.app

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.google.gson.Gson

object QRCodeGenerator {
    private val gson = Gson()
    
    fun generateQRCode(event: Event, size: Int = 512): Bitmap {
        val qrData = QRData(
            eventId = event.id,
            eventName = event.name,
            description = event.description,
            timestamp = event.timestamp
        )
        val json = gson.toJson(qrData)
        
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(json, BarcodeFormat.QR_CODE, size, size)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        
        return bitmap
    }
    
    fun parseQRCode(qrContent: String): Event? {
        return try {
            val qrData = gson.fromJson(qrContent, QRData::class.java)
            Event(
                id = qrData.eventId,
                name = qrData.eventName,
                description = qrData.description,
                timestamp = qrData.timestamp
            )
        } catch (e: Exception) {
            null
        }
    }
    
    data class QRData(
        val eventId: String,
        val eventName: String,
        val description: String,
        val timestamp: Long
    )
}
