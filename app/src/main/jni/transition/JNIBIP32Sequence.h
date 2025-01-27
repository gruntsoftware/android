
#ifndef BRAINWALLET_JNIBIP32SEQUENCE_H
#define BRAINWALLET_JNIBIP32SEQUENCE_H

#ifdef __cplusplus
extern "C" {
#endif


JNIEXPORT jbyteArray JNICALL Java_com_jniwrappers_BRBIP32Sequence_bip32BitIDKey(JNIEnv *env,
                                                                             jobject thiz,
                                                                             jbyteArray seed,
                                                                             jint index,
                                                                             jstring strUri);


#ifdef __cplusplus
}
#endif

#endif //BRAINWALLET_JNIBIP32SEQUENCE_H
