//
// Created by HanFei on 4/26/21.
//

#include <jni.h>
#include <string>
#include "android/log.h"
#include "client/linux/handler/exception_handler.h"
#include "client/linux/handler/minidump_descriptor.h"
#include "fstream"

#define TAG "native_crash_handler"

#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)

using namespace google_breakpad;

static JavaVM *jvm;
static jclass ThreadClass;
static jmethodID toString;
static jmethodID getStackTrace;
static jmethodID currThread;

char *getCurrThreadName(pid_t tid) {
    const int THREAD_NAME_MAX = 100;
    char *path = (char *) calloc(1, 200);
    char *line = (char *) calloc(1, THREAD_NAME_MAX);
    char *line2 = (char *) calloc(1, THREAD_NAME_MAX);
    snprintf(path, PATH_MAX, "proc/%d/comm", tid);


    std::ifstream infile;
    infile.open(path);
    infile >> line;
    infile >> line2;
    infile.close();
    LOGD("DumpCallback success, thread id = %d, path = %s, %s,%s", tid, path, line, line2);


    FILE *commFile = fopen(path, "r");
    if (commFile) {
        fgets(line, THREAD_NAME_MAX, commFile);
        fclose(commFile);
    }
    free(path);
    if (line) {
        int length = strlen(line);
        if (line[length - 1] == '\n') {
            line[length - 1] = '\0';
        }
    }
    return line;
}

void dumpCurrThreadInfo() {
    JNIEnv *env;
    if (jvm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return;
    }
    jobject threadObj = env->CallStaticObjectMethod(ThreadClass, currThread);
    if (threadObj) {
        auto allStackTrace = (jobjectArray) (env->CallObjectMethod(threadObj,
                                                                   getStackTrace));
        int size = env->GetArrayLength(allStackTrace);
        for (int i = 0; i < size; ++i) {
            jobject stackTrace = env->GetObjectArrayElement(allStackTrace, i);
            auto text = (jstring) (env->CallObjectMethod(stackTrace, toString));
            if (text) {
                char *desc = const_cast<char *>(env->GetStringUTFChars(text, JNI_FALSE));
                LOGD("crash: %s", desc);
                env->ReleaseStringUTFChars(text, desc);
            }
        }
    }
}


bool DumpCallback(const MinidumpDescriptor &descriptor,
                  void *context,
                  bool succeeded) {
    const char *threadname = getCurrThreadName(gettid());
    LOGD("DumpCallback success, thread name = %s, path = %s, is success = %d\n", threadname,
         descriptor.path(), succeeded);
    dumpCurrThreadInfo();

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
    jvm = vm;
    JNIEnv *env;
    if ((jvm->GetEnv((void **) &env, JNI_VERSION_1_6)) != JNI_OK)
        return JNI_ERR;
    LOGD("JNI_OnLoad()");
    jclass threadCls = env->FindClass("java/lang/Thread");
    ThreadClass = reinterpret_cast<jclass>(env->NewGlobalRef(threadCls));
    getStackTrace = env->GetMethodID(threadCls, "getStackTrace",
                                     "()[Ljava/lang/StackTraceElement;");
    currThread = env->GetStaticMethodID(threadCls, "currentThread", "()Ljava/lang/Thread;");

    jclass StackTraceElement = env->FindClass("java/lang/StackTraceElement");
    toString = env->GetMethodID(StackTraceElement, "toString", "()Ljava/lang/String;");

    return JNI_VERSION_1_6;
}

JNIEXPORT void JNI_OnUnload(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    if ((jvm->GetEnv((void **) &env, JNI_VERSION_1_6)) != JNI_OK)
        return;
    env->DeleteLocalRef(ThreadClass);
}