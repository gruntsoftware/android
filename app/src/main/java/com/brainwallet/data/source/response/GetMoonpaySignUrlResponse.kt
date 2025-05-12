package com.brainwallet.data.source.response

import kotlinx.serialization.Serializable

@Serializable
data class GetMoonpaySignUrlResponse(
    val signedUrl: String
)
