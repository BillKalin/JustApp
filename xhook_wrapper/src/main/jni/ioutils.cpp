//
// Created by Administrator on 2020.12.12.
//

#include "ioutils.h"
#include <cstdint>
#include <unistd.h>

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