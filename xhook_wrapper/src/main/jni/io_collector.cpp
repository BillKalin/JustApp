//
// Created by Administrator on 2020.12.18.
//

#include "io_collector.h"

void IoCollector::OnOpen(const char *path, int flag, mode_t mode, int ret_id, JavaContext javaContext) {
    if (ret_id == -1)
        return;
    if (map.find(ret_id) != map.end())
        return;
    std::shared_ptr<IoInfo> info = std::make_shared<IoInfo>(path, javaContext);
    map.insert(std::make_pair(ret_id, info));
}

std::shared_ptr<IoInfo> IoCollector::OnClose(int fd, int ret_id) {
    if (ret_id == -1)
        return nullptr;

    if (map.find(fd) == map.end())
        return nullptr;

    std::shared_ptr<IoInfo> info = map[fd];
    info->file_size = GetFileSize(info->path_.c_str());
    map.erase(fd);

    return info;
}

void IoCollector::OnRead(int fd, const void *buffer, size_t buf_size, ssize_t ret, long cost) {
    if (ret == -1)
        return;
    if (map.find(fd) == map.end())
        return;
    CountRWInfo(fd, buf_size, cost);
}

void IoCollector::OnWrite(int fd, const void *buffer, size_t buf_size, ssize_t ret, long cost) {
    if (ret == -1)
        return;
    if (map.find(fd) == map.end())
        return;
    CountRWInfo(fd, buf_size, cost);
}

void IoCollector::CountRWInfo(int fd, long op_size, long rw_cost) {
    if (map.find(fd) == map.end())
        return;

    LOGD("CountRWInfo: fd = %d, op_size = %ld, cost = %ld", fd, op_size, rw_cost);
    const int64_t now = GetSystemTimeMicros();
    std::shared_ptr<IoInfo> info = map[fd];
    info->op_count ++;
    info->op_size += op_size;
    info->last_rw_time = now;
    info->rw_cost_time += rw_cost;
}

