//
// Created by Administrator on 2020.12.18.
//
#include <sys/types.h>
#include <mutex>
#include <unordered_map>
#include "ioutils.h"
#include <android/log.h>

#ifndef JUSTAPP_IO_COLLECTOR_H
#define JUSTAPP_IO_COLLECTOR_H

class JavaContext {
public:
    JavaContext();
    JavaContext(intmax_t thread_id, const std::string &thread_name, const std::string &stack)
            : thread_id_(thread_id), thread_name_(thread_name), stack_(stack) {}

    const intmax_t thread_id_;
    const std::string thread_name_;
    const std::string stack_;
};

class IoInfo {
public:
//    IoInfo() = default;

    IoInfo(const std::string path, const JavaContext javaContext)
            : path_(path), javaContext_(javaContext) {}

    const std::string path_;
    const JavaContext javaContext_;

    long file_size = 0;
    long total_rw_time = 0;
    int op_count = 0;
    long op_size = 0;
    long rw_cost_time = 0;
    int64_t last_rw_time = 0;
};

class IoCollector {
public:
    void OnOpen(const char *path, int flag, mode_t mode, int ret_id, const JavaContext javaContext);
    void OnRead(int fd, const void* buffer, size_t buf_size, ssize_t ret, long cost);
    void OnWrite(int fd, const void* buffer, size_t buf_size, ssize_t ret, long cost);
    std::shared_ptr<IoInfo> OnClose(int fd, int ret_id);

private:
    void CountRWInfo(int fd, long op_size, long rw_cost);
    std::unordered_map<int, std::shared_ptr<IoInfo>> map;
};

#endif //JUSTAPP_IO_COLLECTOR_H
