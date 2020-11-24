//
// Created by Administrator on 2020.11.23.
//
#include "jdpa-debug.h"
#include <jni.h>
#include <string>
#include <android/log.h>
#include <android/api-level.h>
#include <sys/system_properties.h>
#include "fake_dlfcn.h"
#include "dlfcn.h"

#define DEBUG 1
#define TAG "jdpa"

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


void reloadJdwpPreNougat(jboolean);

void reloadJdwpNougat(jboolean);

void openJawp(jboolean open) {
    LOGD("open jawp called, %d", open);
    char osVersion[PROP_VALUE_MAX];
    __system_property_get("ro.build.version.sdk", osVersion);
    int version = atoi(osVersion);
    LOGD("os version: %d", version);
    if (version >= __ANDROID_API_N__) {
        reloadJdwpNougat(open);
    } else {
        reloadJdwpPreNougat(open);
    }
}

void reloadJdwpNougat(jboolean open) {
    void *handler = v_dlopen("/system/lib/libart.so", RTLD_NOW);
    if (handler == nullptr) {
        const char *error = dlerror();
        LOGD("dlerror : %s,", error);
    }

    void (*allowjdwp)(bool);
    allowjdwp = (void (*)(bool)) v_dlsym(handler, "_ZN3art3Dbg14SetJdwpAllowedEb");
    allowjdwp(true);
    LOGD("dlerror: allowjdwp()");

    void (*pfun)();
    pfun = (void (*)()) v_dlsym(handler, "_ZN3art3Dbg8StopJdwpEv");
    pfun();
    LOGD("dlerror: stop last jdwp()");

    if (open == JNI_TRUE) {
        JdwpOptions options;
        options.server = true;
        options.suspend = false;
        options.host = "127.0.0.1";
        options.port = 8000;
        options.transport = JdwpTransportType::kJdwpTransportAndroidAdb;

        void (*configureJdwp)(const void *);
        configureJdwp = (void (*)(const void *)) v_dlsym(handler,
                                                         "_ZN3art3Dbg13ConfigureJdwpERKNS_4JDWP11JdwpOptionsE");
        configureJdwp(&options);

        LOGD("dlerror: set options()");

        pfun = (void (*)()) v_dlsym(handler, "_ZN3art3Dbg9StartJdwpEv");
        pfun();
    }

    v_dlclose(handler);
}


void reloadJdwpPreNougat(jboolean open) {
    void *handler = dlopen("/system/lib/libart.so", RTLD_NOW);
    if (handler == nullptr) {
        const char *error = dlerror();
        LOGD("dlerror: %s", error);
    }

    //对于debuggable false的配置，重新设置为可调试
    void (*allowjdwp)(bool);
    allowjdwp = (void (*)(bool)) dlsym(handler, "_ZN3art3Dbg14SetJdwpAllowedEb");
    allowjdwp(true);

    //关闭之前启动的jdwp-thread
    void (*pfun)();
    pfun = (void (*)()) dlsym(handler, "_ZN3art3Dbg8StopJdwpEv");
    pfun();

    if (open == JNI_TRUE) {
        //重新配置gJdwpOptions
        bool (*parseJdwpOptions)(const std::string &);
        parseJdwpOptions = (bool (*)(const std::string &)) dlsym(handler,
                                                                 "_ZN3art3Dbg16ParseJdwpOptionsERKNSt3__112basic_stringIcNS1_11char_traitsIcEENS1_9allocatorIcEEEE");
        std::string option = "transprt=dt_socket,address=8000,server=y,suspend=n";
        parseJdwpOptions(option);

        pfun = (void (*)()) dlsym(handler, "_ZN3art3Dbg9StartJdwpEv");
        pfun();
    }

    dlclose(handler);
}

