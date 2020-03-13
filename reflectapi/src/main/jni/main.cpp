//
// Created by Administrator on 2020.03.13.
//

#include <jni.h>
#include "art.h"

extern "C"
JNIEXPORT jint JNICALL
Java_com_billkalin_reflect_api_ReflectApi_openApi(JNIEnv *env, jobject type, jint targetSdkVersion) {
    return openApi(env, targetSdkVersion);
}