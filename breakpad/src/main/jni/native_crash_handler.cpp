//
// Created by HanFei on 4/26/21.
//

#include <jni.h>
#include <string>

#include "client/linux/handler/exception_handler.h"
#include "client/linux/handler/minidump_descriptor.h"

bool DumpCallback(const google_breakpad::MinidumpDescriptor &descriptor,
                  void *context,
                  bool succeeded) {
    printf("Dump path: %s\n", descriptor.path());
    return succeeded;
}

void Crash() {
    volatile int *a = reinterpret_cast<volatile int *>(NULL);
    *a = 1;
}


extern "C"
JNIEXPORT void JNICALL
Java_com_billkalin_breakpad_NativeCrashHandler_init(JNIEnv
*env,jobject thiz, jstring dump_path) {
    const char *path = env->GetStringUTFChars(dump_path, 0);
    google_breakpad::MinidumpDescriptor descriptor(path);
    google_breakpad::ExceptionHandler eh(descriptor, NULL, DumpCallback,
                                     NULL, true, -1);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_billkalin_breakpad_NativeCrashHandler_testCrash(JNIEnv *env, jobject thiz) {
    Crash();
}
