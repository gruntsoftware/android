package com.brainwallet.data.model

@Retention(AnnotationRetention.RUNTIME)
annotation class AudioRes

data class CountryAudio(
    val langDescription: String,
    val langQuestion: String,
    @AudioRes val langAudio: Int,
)
