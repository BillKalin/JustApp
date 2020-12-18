//
// Created by Administrator on 2020.12.12.
//

#ifndef JUSTAPP_IO_MONITOR_H
#define JUSTAPP_IO_MONITOR_H

#include <jni.h>
#include <xhook.h>
#include <android/log.h>

const static char *TARGET_MODULES[] = {
        "libopenjdkjvm.so",
        "libjavacore.so",
        "libopenjdk.so"
};

static bool initJniEnv(JavaVM *vm);

static int (*origin_open)(const char *path, int flag, mode_t mode);

static int (*origin_open64)(const char *path, int flag, mode_t mode);

static ssize_t (*origin_read)(int fd, void *buffer, size_t size);

static ssize_t (*origin_read_chk)(int fd, void *buffer, size_t count, size_t buf_size);

static ssize_t (*origin_write)(int fd, const void *buffer, size_t size);

static ssize_t (*origin_write_chk)(int fd, const void *buffer, size_t count, size_t buf_size);

static int (*origin_close)(int fd);

static void DoProxyReadLogic(const char *pathname, int flag, mode_t mode, int ret);

#endif //JUSTAPP_IO_MONITOR_H
