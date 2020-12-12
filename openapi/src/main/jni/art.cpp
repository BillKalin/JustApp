//
// Created by Administrator on 2020.03.13.
//
#include "art.h"
#include <android/log.h>
#include <vector>
#include <string>

#define LOGV(...) ((void)__android_log_print(ANDROID_LOG_INFO, "ReflectApi", __VA_ARGS__))

template<typename T>
//寻找变量偏移量
int findOffset(void *start, int regionStart, int regionEnd, T value) {
    if (nullptr == start || regionStart < 0 || regionEnd <= 0)
        return -1;
    char *c_start = (char *) start;
    for (int i = regionStart; i < regionEnd; i += 4) {
        T *currValue = (T *) (c_start + i);
        if (*currValue == value) {
            LOGV("found offset: %d", i);
            return i;
        }
    }
    LOGV("not found offset: %d", value);
    return -2;
}

int openApi(JNIEnv *env, jint targetSdkVersion) {
    JavaVM *javaVm;
    env->GetJavaVM(&javaVm);
    JavaVMExt *javaVMExt = (JavaVMExt *) javaVm;
    void *runtime = javaVMExt->runtime;
    LOGV("runtime : %p, javaVmExt: %p", runtime, javaVMExt);
    const int MAX = 2000;
    int offsetVmExt = findOffset(runtime, 0, MAX, (size_t) javaVMExt);
    LOGV("offsetVmExt: %d", offsetVmExt);
    if (offsetVmExt < 0)
        return -1;
    int offsetTargetVersion = findOffset(runtime, 0, MAX, targetSdkVersion);
    LOGV("offsetTargetVersion: %d", offsetTargetVersion);
    if (offsetTargetVersion < 0)
        return -2;
    PartialRuntime *partialRuntime = (PartialRuntime *) ((char *) runtime + offsetTargetVersion);
    bool safe_mode = partialRuntime->safe_mode_;
    bool is_java_debuggable = partialRuntime->is_java_debuggable_;
    bool is_native_debuggable = partialRuntime->is_native_debuggable_;

    LOGV("is_java_debuggable: %d, is_native_debuggable: %d, safe_mode: %d", is_java_debuggable,
         is_native_debuggable, safe_mode);
    LOGV("hidden api before: %d", partialRuntime->hidden_api_policy_);
    LOGV("fingerprint : %s", partialRuntime->fingerprint_.c_str());

    partialRuntime->hidden_api_policy_ = EnforcementPolicy::kNoChecks;
    LOGV("hidden api after: %d", partialRuntime->hidden_api_policy_);
    return 0;
}
