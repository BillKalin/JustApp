//
// Created by Administrator on 2020.11.23.
//

#ifndef JUSTAPP_FAKE_DLFCN_H
#define JUSTAPP_FAKE_DLFCN_H

#include <android/log.h>
#include <cstdint>
#include <string>

struct ctx;

void *v_dlopen(const char *filename, int flags);

int v_dlclose(void *handle);

void *v_dlsym(void *hanlde, const char *symbol);

enum JdwpTransportType {
    kJdwpTransportUnknown = 0,
    kJdwpTransportSocket = 1,       // transport=dt_socket
    kJdwpTransportAndroidAdb = 2,   // transport=dt_android_adb
};

struct JdwpOptions {
    JdwpTransportType transport = kJdwpTransportUnknown;
    bool server = false;
    bool suspend = false;
    std::string host = "";
    uint16_t port = static_cast<uint16_t>(-1);
};

#endif //JUSTAPP_FAKE_DLFCN_H
