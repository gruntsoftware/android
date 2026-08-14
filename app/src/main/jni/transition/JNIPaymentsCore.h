#include "jni.h"

#ifndef BRAINWALLET_JNIPAYMENTSCORE_H
#define BRAINWALLET_JNIPAYMENTSCORE_H

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jbyteArray JNICALL
Java_com_brainwallet_tools_security_LitecoinURIHandler_parsePaymentRequest(JNIEnv *env, jobject obj, jbyteArray payment);

JNIEXPORT jbyteArray JNICALL
Java_com_brainwallet_tools_security_LitecoinURIHandler_getCertificatesFromPaymentRequest(JNIEnv *env, jobject obj,
                                                                                     jbyteArray payment, jint index);

JNIEXPORT jstring JNICALL
Java_com_brainwallet_tools_security_LitecoinURIHandler_parsePaymentACK(JNIEnv *env, jobject obj, jbyteArray paymentACK);

#ifdef __cplusplus
}
#endif

#endif //BRAINWALLET_JNIPAYMENTSCORE_H
