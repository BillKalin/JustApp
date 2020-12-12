//
// Created by Administrator on 2020.12.12.
//

#include <android/log.h>
#include <sys/types.h>
#include "io_monitor.h"
#include "ioutils.h"

#define DEBUG 1
#define TAG "IoMonitor"

#if DEBUG
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG ,__VA_ARGS__) // 定义LOGD类型
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,TAG ,__VA_ARGS__) // 定义LOGI类型
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,TAG ,__VA_ARGS__) // 定义LOGW类型
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,TAG ,__VA_ARGS__) // 定义LOGE类型
#define LOGF(...) __android_log_print(ANDROID_LOG_FATAL,TAG ,__VA_ARGS__) // 定义LOGF类型
#else
#define LOGD(...)
#define LOGI(...)
#define LOGW(...)
#define LOGE(...)
#define LOGF(...)
#endif

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    __android_log_print(ANDROID_LOG_DEBUG, TAG, "JNI_OnLoad");
    JNIEnv *env;
    if (vm->GetEnv((void **) (&env), JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }
    __android_log_print(ANDROID_LOG_DEBUG, TAG, "JNI_OnLoad done!");
    return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *vm, void *reserved) {
    __android_log_print(ANDROID_LOG_DEBUG, TAG, "JNI_OnUnload");
}


int ProxyOpen(const char *pathname, int flag, mode_t mode) {
    __android_log_print(ANDROID_LOG_DEBUG, TAG,
                        "ProxyOpen.open64: %s", pathname);
    return origin_open(pathname, flag, mode);
}

int ProxyOpen64(const char *pathname, int flag, mode_t mode) {
    bool isMainThread = IsMainThread();
    __android_log_print(ANDROID_LOG_DEBUG, TAG,
                        "ProxyOpen64.open64: %s, %s", pathname, isMainThread);
    int ret = origin_open64(pathname, flag, mode);
    if (ret != -1) {

    }
    return ret;
}

extern "C" JNIEXPORT void JNICALL
Java_com_billkalin_xnative_xhook_wrapper_IoMonitorJni_doHook(JNIEnv *env, jobject thiz) {
    __android_log_print(ANDROID_LOG_DEBUG, TAG,
                        "Java_com_billkalin_xnative_xhook_wrapper_IoMonitorJni_doHook");
    for (int i = 0; i < TARGET_COUNTS; i++) {
        const char *soname = TARGET_MODULES[i];
        __android_log_print(ANDROID_LOG_DEBUG, TAG, "doHook: so: %s", soname);
        void *ret = xhook_elf_open(soname);
        if (!ret) {
            __android_log_print(ANDROID_LOG_DEBUG, TAG, "doHook: open so: %s failed !!", soname);
            continue;
        }
        xhook_hook_symbol(ret, "open", (void *) ProxyOpen, (void **) &origin_open);
        xhook_hook_symbol(ret, "open64", (void *) ProxyOpen64, (void **) &origin_open64);
    }
    __android_log_print(ANDROID_LOG_DEBUG, TAG, "doHook: done!!");
}