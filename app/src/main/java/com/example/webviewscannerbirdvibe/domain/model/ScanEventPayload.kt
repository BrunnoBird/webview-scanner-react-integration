package com.example.webviewscannerbirdvibe.domain.model

sealed class ScanEventPayload {

    data class Success(
        val value: String,
        val type: ScanResultType,
        val rawFormat: String?
    ) : ScanEventPayload()

    data class Error(
        val code: ScanErrorCode,
        val message: String?
    ) : ScanEventPayload()
}
