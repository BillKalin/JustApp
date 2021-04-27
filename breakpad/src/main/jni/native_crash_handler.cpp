//
// Created by HanFei on 4/26/21.
//

#include <jni.h>
#include <string>
#include "android/log.h"
#include "client/linux/handler/exception_handler.h"
#include "client/linux/handler/minidump_descriptor.h"

#define TAG "native_crash_handler"

#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)

using namespace google_breakpad;

bool DumpCallback(const MinidumpDescriptor &descriptor,
                  void *context,
                  bool succeeded) {
    LOGD("DumpCallback success, path = %s, is success = %d\n", descriptor.path(), succeeded);
    return succeeded;
}

void Crash() {
    volatile int *a = reinterpret_cast<volatile int *>(NULL);
    *a = 1;
}


extern "C"
JNIEXPORT void JNICALL
Java_com_billkalin_breakpad_NativeCrashHandler_init(JNIEnv *env, jobject thiz, jstring dump_path) {
    const char *path = env->GetStringUTFChars(dump_path, 0);
    LOGD("NativeCrashHandler.init: dump path = %s\n", path);
    MinidumpDescriptor descriptor(path);
    static ExceptionHandler eh(descriptor, NULL, DumpCallback,
                               NULL, true, -1);
    env->ReleaseStringUTFChars(dump_path, path);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_billkalin_breakpad_NativeCrashHandler_testCrash(JNIEnv *env, jobject thiz) {
    Crash();
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    if ((vm->GetEnv((void **) &env, JNI_VERSION_1_6)) != JNI_OK)
        return JNI_ERR;
    LOGD("JNI_OnLoad()");
    return JNI_VERSION_1_6;
}