//
// Created by Administrator on 2020.03.13.
//

#include <jni.h>
#include "art.h"
#include "jdpa-debug.h"

extern "C"
JNIEXPORT jint JNICALL
Java_com_billkalin_open_api_NativeOpenApi_openApi(JNIEnv *env, jobject type, jint targetSdkVersion) {
    return openApi(env, targetSdkVersion);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_billkalin_open_api_NativeOpenApi_openJdwp(JNIEnv *env, jobject thiz, jboolean open) {
    openJawp(open);
}