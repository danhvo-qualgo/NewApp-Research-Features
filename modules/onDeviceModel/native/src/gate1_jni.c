/*
 * gate1_jni.c — JNI bridge between Gate1Classifier.kt and gate1_features.c
 *
 * Provides native methods for:
 *   - Loading brands from byte array
 *   - Checking allowlist
 *   - Extracting 30-float feature vector
 *   - Encoding URL for CNN branch
 */

#include <jni.h>
#include <string.h>
#include <stdlib.h>
#include "gate1_features.h"

/* Helper: convert jstring to C string (caller must free with ReleaseStringUTFChars) */

JNIEXPORT jlong JNICALL
Java_com_safenest_urlanalyzer_gate1_Gate1Classifier_nativeLoadBrands(
    JNIEnv *env, jobject thiz, jbyteArray assetData
) {
    jsize len = (*env)->GetArrayLength(env, assetData);
    jbyte *data = (*env)->GetByteArrayElements(env, assetData, NULL);
    if (!data) return 0;

    Gate1BrandList *bl = gate1_load_brands_from_buffer(data, (size_t)len);

    (*env)->ReleaseByteArrayElements(env, assetData, data, JNI_ABORT);
    return (jlong)(intptr_t)bl;
}

JNIEXPORT void JNICALL
Java_com_safenest_urlanalyzer_gate1_Gate1Classifier_nativeFreeBrands(
    JNIEnv *env, jobject thiz, jlong brandsPtr
) {
    Gate1BrandList *bl = (Gate1BrandList *)(intptr_t)brandsPtr;
    if (bl) gate1_free_brands(bl);
}

JNIEXPORT jboolean JNICALL
Java_com_safenest_urlanalyzer_gate1_Gate1Classifier_nativeIsAllowlisted(
    JNIEnv *env, jobject thiz, jstring url, jlong brandsPtr
) {
    Gate1BrandList *bl = (Gate1BrandList *)(intptr_t)brandsPtr;
    if (!bl) return JNI_FALSE;

    const char *urlStr = (*env)->GetStringUTFChars(env, url, NULL);
    if (!urlStr) return JNI_FALSE;

    int result = gate1_is_allowlisted(urlStr, bl);

    (*env)->ReleaseStringUTFChars(env, url, urlStr);
    return result ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jfloatArray JNICALL
Java_com_safenest_urlanalyzer_gate1_Gate1Classifier_nativeExtractFeatures(
    JNIEnv *env, jobject thiz, jstring url, jlong brandsPtr
) {
    Gate1BrandList *bl = (Gate1BrandList *)(intptr_t)brandsPtr;
    const char *urlStr = (*env)->GetStringUTFChars(env, url, NULL);
    if (!urlStr) {
        jfloatArray arr = (*env)->NewFloatArray(env, 30);
        return arr;
    }

    Gate1Features features = gate1_extract_features(urlStr, bl);
    (*env)->ReleaseStringUTFChars(env, url, urlStr);

    jfloatArray result = (*env)->NewFloatArray(env, 30);
    (*env)->SetFloatArrayRegion(env, result, 0, 30, features.values);
    return result;
}

JNIEXPORT jintArray JNICALL
Java_com_safenest_urlanalyzer_gate1_Gate1Classifier_nativeEncodeUrl(
    JNIEnv *env, jobject thiz, jstring url
) {
    const char *urlStr = (*env)->GetStringUTFChars(env, url, NULL);
    if (!urlStr) {
        jintArray arr = (*env)->NewIntArray(env, 200);
        return arr;
    }

    Gate1URLEncoding encoding = gate1_encode_url(urlStr);
    (*env)->ReleaseStringUTFChars(env, url, urlStr);

    jintArray result = (*env)->NewIntArray(env, 200);
    (*env)->SetIntArrayRegion(env, result, 0, 200, encoding.ids);
    return result;
}
