package com.brainwallet.tools.security;

import com.brainwallet.presenter.entities.PaymentRequestWrapper;

/**
 * BIP70 payment-protocol native bindings only.
 *
 * The higher-level litecoin: URI/QR handling this class used to own has moved to
 * {@link LitecoinURIHandler} - use that instead for anything new. This class stays
 * around solely to host these three native methods: the native library resolves them
 * by their fully-qualified JNI symbol name
 * (`Java_com_brainwallet_tools_security_BitcoinUrlHandler_...`, see
 * app/src/main/jni/transition/core.c, which lives in the `core` git submodule), so
 * renaming or moving them requires a matching change there first.
 */
public class BitcoinUrlHandler {

    public static native PaymentRequestWrapper parsePaymentRequest(byte[] req);

    public static native String parsePaymentACK(byte[] req);

    public static native byte[] getCertificatesFromPaymentRequest(byte[] req, int index);

}
