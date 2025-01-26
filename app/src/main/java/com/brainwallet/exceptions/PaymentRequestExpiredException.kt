package com.brainwallet.exceptions

import java.security.GeneralSecurityException


class PaymentRequestExpiredException : GeneralSecurityException {
    private constructor() : super()

    constructor(msg: String?) : super("The request is expired!")

    private constructor(msg: String, cause: Throwable) : super(msg, cause)

    private constructor(cause: Throwable) : super(cause)

    companion object {
        val TAG: String = PaymentRequestExpiredException::class.java.name
    }
}
