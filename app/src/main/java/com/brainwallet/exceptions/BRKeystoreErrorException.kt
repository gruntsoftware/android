package com.brainwallet.exceptions

class BRKeystoreErrorException(message: String?) : Exception(message) {
    companion object {
        val TAG: String = BRKeystoreErrorException::class.java.name
    }
}
