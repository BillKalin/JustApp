//
// Created by Administrator on 2021.04.23.
//

#ifndef JUSTAPP_CONVERT_H
#define JUSTAPP_CONVERT_H
#include <fstream>
#include <iostream>
#include "tinyxml2.h"
#include "java_lang_builder.h"
#include "dex_layout_compiler.h"
#include "util.h"
using namespace startop::util;
using namespace tinyxml2;

void convert(const char *filepath, const char *layoutname, const char *pkg, const char *outFile);

#endif //JUSTAPP_CONVERT_H
