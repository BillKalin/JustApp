//
// Created by Administrator on 2020.12.12.
//

#include <sys/types.h>
#include <cstring>
#include <cstdlib>
#include "io_monitor.h"
#include "ioutils.h"
#include "io_detector.h"

static JavaVM *jvm;

static jclass jIoMonitorJniClass;
static jclass jIoMonitorKtClass;
static jmethodID jIoMonitorKtGetJavaContextMethod;
static jclass jJavaContextClass;
static jfieldID jJavaContextThreadNameField;
static jfieldID jJavaContextThreadStackStringField;

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    LOGD("JNI_OnLoad");
    JNIEnv *env;
    if (vm->GetEnv((void **) (&env), JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }

    if (!initJniEnv(vm)) {
        return -1;
    }

    LOGD("JNI_OnLoad done!");
    return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *vm, void *reserved) {
    JNIEnv *env = nullptr;
    jvm->GetEnv((void **) &env, JNI_VERSION_1_6);
    if (env != nullptr) {
        if (jIoMonitorJniClass) {
            env->DeleteGlobalRef(jIoMonitorJniClass);
        }
    }
    LOGD("JNI_OnUnload");
}

static bool initJniEnv(JavaVM *vm) {
    jvm = vm;
    JNIEnv *env = nullptr;
    if (jvm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        LOGE("initJniEnv: get env failed!");
        return false;
    }

    jclass tmpCls = env->FindClass("com/billkalin/xnative/xhook/wrapper/IoMonitorJni");
    if (tmpCls == nullptr) {
        LOGE("initJniEnv: find IoMonitorJni class failed !");
        return false;
    }
    jIoMonitorJniClass = reinterpret_cast<jclass>(env->NewGlobalRef(tmpCls));

    jclass iomonitorcls = env->FindClass("com/billkalin/xnative/xhook/wrapper/IoMonitorJniKt");
    jIoMonitorKtClass = reinterpret_cast<jclass>(env->NewGlobalRef(iomonitorcls));

    jIoMonitorKtGetJavaContextMethod = env->GetStaticMethodID(iomonitorcls, "getJavaContext",
                                                              "()Lcom/billkalin/xnative/xhook/wrapper/JavaContext;");

    jclass javacontextcls = env->FindClass("com/billkalin/xnative/xhook/wrapper/JavaContext");
    jJavaContextClass = reinterpret_cast<jclass>(env->NewGlobalRef(javacontextcls));

    jJavaContextThreadNameField = env->GetFieldID(jJavaContextClass, "threadName",
                                                  "Ljava/lang/String;");
    jJavaContextThreadStackStringField = env->GetFieldID(jJavaContextClass, "stackString",
                                                         "Ljava/lang/String;");
    return true;
}


int ProxyOpen(const char *pathname, int flag, mode_t mode) {
    LOGD("ProxyOpen.open: %s", pathname);
    bool isMainThread = IsMainThread();
    if (!isMainThread) {
        return origin_open(pathname, flag, mode);
    }
    int ret = origin_open(pathname, flag, mode);
    DoProxyReadLogic(pathname, flag, mode, ret);
    return ret;
}

int ProxyOpen64(const char *pathname, int flag, mode_t mode) {
    bool isMainThread = IsMainThread();
    LOGD("ProxyOpen64.open64: %s, is main thread : %s", pathname,
         isMainThread ? "true" : "false");
    if (!isMainThread) {
        return origin_open64(pathname, flag, mode);
    }

    int ret = origin_open64(pathname, flag, mode);
    if (ret != -1) {
        DoProxyReadLogic(pathname, flag, mode, ret);
    }
    return ret;
}

static void DoProxyReadLogic(const char *pathname, int flag, mode_t mode, int ret) {
    JNIEnv *env = nullptr;
    jvm->GetEnv((void **) &env, JNI_VERSION_1_6);
    if (env == nullptr) {
        LOGE("DoProxyReadLogic failed..");
        return;
    }

    jobject ktobj = env->CallStaticObjectMethod(jIoMonitorKtClass,
                                                jIoMonitorKtGetJavaContextMethod);
    if (ktobj == nullptr) {
        LOGE("call IomonitorKt.getJavaContext() method failed.");
        return;
    }

    auto threadname = (jstring) (env->GetObjectField(ktobj,
                                                     jJavaContextThreadNameField));
    auto stackstring = (jstring) (env->GetObjectField(ktobj,
                                                      jJavaContextThreadStackStringField));
    char *thread_name = jstringToChars(env, threadname);
    char *stack_trace = jstringToChars(env, stackstring);
    LOGD("DoProxyReadLogic threadname = %s", thread_name);
    LOGD("DoProxyReadLogic stack = %s", stack_trace);
    JavaContext javaContext(GetCurrThreadId(), thread_name == nullptr ? "" : thread_name,
                            stack_trace == nullptr ? "" : stack_trace);
    free(thread_name);
    free(stack_trace);

    IoDetector::Get().OnOpen(pathname, flag, mode, ret, javaContext);

    env->DeleteLocalRef(ktobj);
    env->DeleteLocalRef(threadname);
    env->DeleteLocalRef(stackstring);
}

size_t ProxyRead(int fd, void *buffer, size_t size) {
    LOGD("ProxyRead : fd = %d ", fd);
    if (!IsMainThread()) {
        return origin_read(fd, buffer, size);
    }
    long start = GetTickTimeMicros();
    ssize_t ret = origin_read(fd, buffer, size);
    long cost = GetTickTimeMicros() - start;
    IoDetector::Get().OnRead(fd, buffer, size, ret, cost);
    return ret;
}

size_t ProxyReadChk(int fd, void *buffer, size_t count, size_t buff_size) {
    LOGD("ProxyReadChk : fd = %d ", fd);
    if (!IsMainThread()) {
        return origin_read_chk(fd, buffer, count, buff_size);
    }
    long start = GetTickTimeMicros();
    ssize_t ret = origin_read_chk(fd, buffer, count, buff_size);
    long cost = GetTickTimeMicros() - start;
    IoDetector::Get().OnRead(fd, buffer, count, ret, cost);
    return ret;
}

ssize_t ProxyWrite(int fd, const void *buffer, size_t size) {
    LOGD("ProxyWrite : fd = %d ", fd);
    if (!IsMainThread()) {
        return origin_write(fd, buffer, size);
    }
    long start = GetTickTimeMicros();
    ssize_t ret = origin_write(fd, buffer, size);
    long cost = GetTickTimeMicros() - start;
    IoDetector::Get().OnWrite(fd, buffer, size, ret, cost);
    return ret;
}

ssize_t ProxyWriteChk(int fd, const void *buffer, size_t count, size_t buf_size) {
    LOGD("ProxyWriteChk : fd = %d ", fd);
    if (!IsMainThread()) {
        return origin_write_chk(fd, buffer, count, buf_size);
    }
    long start = GetTickTimeMicros();
    ssize_t ret = origin_write_chk(fd, buffer, count, buf_size);
    long cost = GetTickTimeMicros() - start;
    IoDetector::Get().OnWrite(fd, buffer, count, ret, cost);
    return ret;
}

int ProxyClose(int fd) {
    LOGD("ProxyClose : fd = %d ", fd);
    if (!IsMainThread()) {
        return origin_close(fd);
    }
    int ret_id = origin_close(fd);
    IoDetector::Get().onClose(fd, ret_id);
    return ret_id;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_billkalin_xnative_xhook_wrapper_IoMonitorJni_doHook(JNIEnv *env, jobject thiz) {
    LOGD("Java_com_billkalin_xnative_xhook_wrapper_IoMonitorJni_doHook");
    for (auto soname : TARGET_MODULES) {
        LOGD("doHook: so: %s", soname);
        void *soinfo = xhook_elf_open(soname);
        if (!soinfo) {
            LOGW("doHook: open so: %s failed !!", soname);
            continue;
        }
        xhook_hook_symbol(soinfo, "open", (void *) ProxyOpen, (void **) &origin_open);
        xhook_hook_symbol(soinfo, "open64", (void *) ProxyOpen64, (void **) &origin_open64);
        bool isJavaCoreSo = strstr(soname, "libjavacore.so") != nullptr;
        if (isJavaCoreSo) {
            if (xhook_hook_symbol(soinfo, "read", (void *) ProxyRead, (void **) &origin_read) !=
                0) {
                LOGW("doHook read failed ...");
                if (xhook_hook_symbol(soinfo, "__read_chk", (void *) ProxyReadChk,
                                      (void **) &origin_read_chk) != 0) {
                    LOGW("doHook __read_chk failed ...");
                    xhook_elf_close(soinfo);
                    return JNI_FALSE;
                }
            }

            if (xhook_hook_symbol(soinfo, "write", (void *) ProxyWrite, (void **) &origin_write) !=
                0) {
                LOGW("doHook write failed ...");
                if (xhook_hook_symbol(soinfo, "__write_chk", (void *) ProxyWriteChk,
                                      (void **) &origin_write_chk) != 0) {
                    LOGW("doHook __write_chk failed ...");
                    xhook_elf_close(soinfo);
                    return JNI_FALSE;
                }
            }

            xhook_hook_symbol(soinfo, "close", (void *) ProxyClose, (void **) &origin_close);
        }

        xhook_elf_close(soinfo);
    }
    LOGD("doHook: done!!");
    return JNI_TRUE;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_billkalin_xnative_xhook_wrapper_IoMonitorJni_doUnHook(JNIEnv *env, jobject thiz) {
    LOGD("Java_com_billkalin_xnative_xhook_wrapper_IoMonitorJni_doUnHook");
    for (auto soname : TARGET_MODULES) {
        void *soinfo = xhook_elf_open(soname);
        if (!soinfo) {
            continue;
        }
        xhook_hook_symbol(soinfo, "open", (void *) origin_open, nullptr);
        xhook_hook_symbol(soinfo, "open64", (void *) origin_open64, nullptr);
        xhook_hook_symbol(soinfo, "read", (void *) origin_read, nullptr);
        xhook_hook_symbol(soinfo, "__read_chk", (void *) origin_read_chk, nullptr);
        xhook_hook_symbol(soinfo, "write", (void *) origin_write, nullptr);
        xhook_hook_symbol(soinfo, "close", (void *) origin_close, nullptr);
        xhook_elf_close(soinfo);
    }
    return JNI_TRUE;
}