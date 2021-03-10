//
// Created by Administrator on 2020.12.12.
//

#include "ioutils.h"
#include <cstdint>
#include <unistd.h>
#include <jni.h>
#include <ctime>
#include <ctime>
#include "string"
#include <sys/stat.h>

intmax_t GetMainThreadId() {
    static intmax_t pid = getpid();
    return pid;
}

intmax_t GetCurrThreadId() {
    return gettid();
}

bool IsMainThread() {
    return GetCurrThreadId() == GetMainThreadId();
}

char *jstringToChars(JNIEnv *env, jstring string) {
    if (string == nullptr)
        return nullptr;
    jboolean isCopy = JNI_FALSE;
    const char *str = env->GetStringUTFChars(string, &isCopy);
    char *ret = strdup(str);
    env->ReleaseStringUTFChars(string, str);
    return ret;
}

int64_t GetSystemTimeMicros() {
#ifdef _WIN32
#define EPOCHFILETIME   (116444736000000000UL)
    FILETIME ft;
    LARGE_INTEGER li;
    int64_t tt = 0;
    GetSystemTimeAsFileTime(&ft);
    li.LowPart = ft.dwLowDateTime;
    li.HighPart = ft.dwHighDateTime;
    // 从1970年1月1日0:0:0:000到现在的微秒数(UTC时间)
    tt = (li.QuadPart - EPOCHFILETIME) / 10;
    return tt;
#else
    timeval tv;
    gettimeofday(&tv, 0);
    return (int64_t) tv.tv_sec * 1000000 + (int64_t) tv.tv_usec;
#endif
}

int64_t GetSystemTimeMilliSeconds() {
    return GetSystemTimeMicros() / 1000;
}

long GetTickTimeMicros() {
    timespec ts;
    int ret = clock_gettime(CLOCK_BOOTTIME, &ts);
    if (ret != 0)
        return 0;
    return (long) ts.tv_sec * CLOCKS_PER_SEC + (long) ts.tv_nsec / 1000;
}

int GetFileSize(const char *path) {
    struct stat stat_buf;
    int ret = stat(path, &stat_buf);
    if (ret != 0)
        return -1;
    return stat_buf.st_size;
}


