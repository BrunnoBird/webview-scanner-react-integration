package com.example.webviewscannerbirdvibe.scanner

import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.example.webviewscannerbirdvibe.domain.model.ScanErrorCode
import com.example.webviewscannerbirdvibe.domain.model.ScanResultType
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import org.json.JSONObject

class ScannerActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webView = WebView(this)
        webView.settings.javaScriptEnabled = true
        setContentView(webView)
    }

    fun startScan() {
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_CODE_128,
                Barcode.FORMAT_QR_CODE
            )
            .build()

        val scanner = GmsBarcodeScanning.getClient(this, options)

        scanner.startScan()
            .addOnSuccessListener { barcode ->
                val payload = buildSuccessPayload(barcode)
                pushToWebView(payload)
            }
            .addOnCanceledListener {
                val payload = buildErrorPayload(ScanErrorCode.CANCELLED, null)
                pushToWebView(payload)
            }
            .addOnFailureListener { exception ->
                val code = mapExceptionToCode(exception)
                val payload = buildErrorPayload(code, exception.message)
                pushToWebView(payload)
            }
    }

    private fun buildSuccessPayload(barcode: Barcode): String {
        val value = barcode.rawValue ?: ""
        val type = inferType(barcode)
        val rawFormat = formatName(barcode.format)

        return JSONObject().apply {
            put("status", "success")
            put("value", value)
            put("type", type.name.lowercase())
            if (rawFormat != null) put("rawFormat", rawFormat)
        }.toString()
    }

    private fun buildErrorPayload(code: ScanErrorCode, message: String?): String {
        return JSONObject().apply {
            put("status", "error")
            put("code", code.name)
            if (!message.isNullOrBlank()) put("message", message)
        }.toString()
    }

    private fun inferType(barcode: Barcode): ScanResultType {
        return when (barcode.format) {
            Barcode.FORMAT_QR_CODE -> ScanResultType.PIX
            Barcode.FORMAT_CODE_128 -> ScanResultType.BOLETO
            else -> ScanResultType.UNKNOWN
        }
    }

    private fun formatName(format: Int): String? {
        return when (format) {
            Barcode.FORMAT_CODE_128 -> "CODE_128"
            Barcode.FORMAT_QR_CODE -> "QR_CODE"
            Barcode.FORMAT_EAN_13 -> "EAN_13"
            Barcode.FORMAT_PDF417 -> "PDF417"
            else -> null
        }
    }

    private fun mapExceptionToCode(e: Exception): ScanErrorCode {
        return when {
            e.message?.contains("permission", ignoreCase = true) == true -> ScanErrorCode.CAMERA_DENIED
            e.message?.contains("timeout", ignoreCase = true) == true -> ScanErrorCode.TIMEOUT
            else -> ScanErrorCode.UNKNOWN_ERROR
        }
    }

    private fun pushToWebView(jsonPayload: String) {
        val js = "window.VibeBird && window.VibeBird.onScanResult($jsonPayload)"
        runOnUiThread {
            webView.evaluateJavascript(js, null)
        }
    }
}
