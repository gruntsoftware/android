package com.brainwallet.data.repository

import com.brainwallet.data.source.RemoteConfigSource
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirebaseRemoteConfigRepositoryImpl(
    private val firebaseRemoteConfig: FirebaseRemoteConfig,
) : RemoteConfigSource {

    override suspend fun fetchAndActivate(): Boolean =
        suspendCancellableCoroutine { continuation ->
            firebaseRemoteConfig.fetchAndActivate()
                .addOnSuccessListener { activated -> continuation.resume(activated) }
                .addOnFailureListener { e -> continuation.resumeWithException(e) }
        }

    override fun initialize() {}

    override fun getString(key: String): String =
        firebaseRemoteConfig.getString(key)

    override fun getNumber(key: String): Double =
        firebaseRemoteConfig.getDouble(key)

    override fun getBoolean(key: String): Boolean =
        firebaseRemoteConfig.getBoolean(key)
}
