//
// Created by Administrator on 2021.04.23.
//

#include "util.h"

using std::string;

namespace startop {
    namespace util {

// TODO: see if we can borrow this from somewhere else, like aapt2.
        string FindLayoutNameFromFilename(const string& filename) {
            size_t start = filename.rfind('/');
            if (start == string::npos) {
                start = 0;
            } else {
                start++;  // advance past '/' character
            }
            size_t end = filename.find('.', start);

            return filename.substr(start, end - start);
        }

    }  // namespace util
}  // namespace startop