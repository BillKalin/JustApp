//
// Created by Administrator on 2020.12.12.
//
#include <cstdint>
#include <jni.h>

#ifndef JUSTAPP_IOUTILS_H
#define JUSTAPP_IOUTILS_H

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

intmax_t GetMainThreadId();

intmax_t GetCurrThreadId();

bool IsMainThread();

char *jstringToChars(JNIEnv *env, jstring string);

long GetTickMicros();

int GetFileSize(const char *path);

#endif //JUSTAPP_IOUTILS_H
