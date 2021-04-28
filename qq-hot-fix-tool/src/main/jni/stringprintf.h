//
// Created by Administrator on 2021.04.23.
//

#ifndef JUSTAPP_STRINGPRINTF_H
#define JUSTAPP_STRINGPRINTF_H

#pragma once

#include <stdarg.h>
#include <string>

namespace android {
    namespace base {

// These printf-like functions are implemented in terms of vsnprintf, so they
// use the same attribute for compile-time format string checking.

// Returns a string corresponding to printf-like formatting of the arguments.
        std::string StringPrintf(const char* fmt, ...) __attribute__((__format__(__printf__, 1, 2)));

// Appends a printf-like formatting of the arguments to 'dst'.
        void StringAppendF(std::string* dst, const char* fmt, ...)
        __attribute__((__format__(__printf__, 2, 3)));

// Appends a printf-like formatting of the arguments to 'dst'.
        void StringAppendV(std::string* dst, const char* format, va_list ap)
        __attribute__((__format__(__printf__, 2, 0)));

    }  // namespace base
}  // namespace android

#endif //JUSTAPP_STRINGPRINTF_H
