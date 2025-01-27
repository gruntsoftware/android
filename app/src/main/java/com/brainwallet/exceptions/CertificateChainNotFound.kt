package com.brainwallet.exceptions

import java.security.GeneralSecurityException


class CertificateChainNotFound : GeneralSecurityException {
    private constructor() : super()

    constructor(msg: String?) : super(msg)

    private constructor(msg: String, cause: Throwable) : super(msg, cause)

    private constructor(cause: Throwable) : super(cause)

    companion object {
        val TAG: String = CertificateChainNotFound::class.java.name
    }
}

