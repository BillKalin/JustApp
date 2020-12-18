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

long GetTickMicros() {
    struct timespec ts;
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


