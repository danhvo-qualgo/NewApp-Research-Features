/*
 * gate1_jni.c — JNI bridge between Kotlin and C feature extraction library.
 */
#include <jni.h>
#include <string.h>
#include "gate1_features.h"

JNIEXPORT jlong JNICALL
Java_com_safenest_gate1_Gate1Classifier_nativeLoadBrands(
    JNIEnv* env, jobject thiz, jbyteArray data) {
    jsize len = (*env)->GetArrayLength(env, data);
    jbyte* buf = (*env)->GetByteArrayElements(env, data, NULL);
    Gate1BrandList* bl = gate1_load_brands_from_buffer(buf, (size_t)len);
    (*env)->ReleaseByteArrayElements(env, data, buf, JNI_ABORT);
    return (jlong)(intptr_t)bl;
}

JNIEXPORT void JNICALL
Java_com_safenest_gate1_Gate1Classifier_nativeFreeBrands(
    JNIEnv* env, jobject thiz, jlong ptr) {
    gate1_free_brands((Gate1BrandList*)(intptr_t)ptr);
}

JNIEXPORT jboolean JNICALL
Java_com_safenest_gate1_Gate1Classifier_nativeIsAllowlisted(
    JNIEnv* env, jobject thiz, jstring url, jlong brandsPtr) {
    const char* curl = (*env)->GetStringUTFChars(env, url, NULL);
    int result = gate1_is_allowlisted(curl, (const Gate1BrandList*)(intptr_t)brandsPtr);
    (*env)->ReleaseStringUTFChars(env, url, curl);
    return result ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jfloatArray JNICALL
Java_com_safenest_gate1_Gate1Classifier_nativeExtractFeatures(
    JNIEnv* env, jobject thiz, jstring url, jlong brandsPtr) {
    const char* curl = (*env)->GetStringUTFChars(env, url, NULL);
    Gate1Features feat = gate1_extract_features(curl, (const Gate1BrandList*)(intptr_t)brandsPtr);
    (*env)->ReleaseStringUTFChars(env, url, curl);

    jfloatArray result = (*env)->NewFloatArray(env, GATE1_N_FEATURES);
    (*env)->SetFloatArrayRegion(env, result, 0, GATE1_N_FEATURES, feat.values);
    return result;
}

JNIEXPORT jlongArray JNICALL
Java_com_safenest_gate1_Gate1Classifier_nativeEncodeUrl(
    JNIEnv* env, jobject thiz, jstring url) {
    const char* curl = (*env)->GetStringUTFChars(env, url, NULL);
    Gate1URLEncoding enc = gate1_encode_url(curl);
    (*env)->ReleaseStringUTFChars(env, url, curl);

    /* ONNX expects int64 (jlong), convert from int32 */
    jlong longs[GATE1_MAX_URL_LEN];
    for (int i = 0; i < GATE1_MAX_URL_LEN; i++) {
        longs[i] = (jlong)enc.ids[i];
    }
    jlongArray result = (*env)->NewLongArray(env, GATE1_MAX_URL_LEN);
    (*env)->SetLongArrayRegion(env, result, 0, GATE1_MAX_URL_LEN, longs);
    return result;
}
