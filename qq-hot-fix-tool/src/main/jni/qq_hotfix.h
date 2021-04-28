//
// Created by Administrator on 2021.04.17.
//
#include <jni.h>
#include <android/log.h>
#include <dlfcn.h>
#include "convert.h"

#ifndef JUSTAPP_QQ_HOTFIX_H
#define JUSTAPP_QQ_HOTFIX_H
#define LOG_TAG "QHotfix"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG, __VA_ARGS__)

#define ARRAY_SIZE_FIND_CLASS 3
const static char *SYMBOL_FIND_LOADED_CLASS[ARRAY_SIZE_FIND_CLASS] = {
        "_Z18dvmFindLoadedClassPKc",
        "_Z18kvmFindLoadedClassPKc",
        "dvmFindLoadedClass"
};

#define ARRAY_SIZE_RESOlVE_CLASS 2
const static char *ARRAY_RESOLVE_CLASS[ARRAY_SIZE_RESOlVE_CLASS] = {
        "dvmResolveClass",
        "vResolveClass"
};

void *(*g_pDvmFindLoadedClass_Addr)(const char *);

void *(*g_pDvmResolveClass_Addr)(const void *, unsigned int, int);

#endif //JUSTAPP_QQ_HOTFIX_H
