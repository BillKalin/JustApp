//
// Created by Administrator on 2020.12.12.
//

#ifndef JUSTAPP_IO_MONITOR_H
#define JUSTAPP_IO_MONITOR_H

#include <jni.h>
#include <xhook.h>

const static char *TARGET_MODULES[] = {
        "libopenjdkjvm.so",
        "libjavacore.so",
        "libopenjdk.so"
};

const static size_t TARGET_COUNTS = sizeof(TARGET_MODULES) / sizeof(char *);

static int (*origin_open)(const char *path, int flag, mode_t mode);

static int (*origin_open64)(const char *path, int flag, mode_t mode);

#endif //JUSTAPP_IO_MONITOR_H
