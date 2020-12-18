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
    map.erase(fd);

    return info;
}

void IoCollector::OnRead(int fd, const void *buffer, size_t buf_size, ssize_t ret, long cost) {
    if (ret == -1)
        return;
    if (map.find(fd) == map.end())
        return;

    std::shared_ptr<IoInfo> info = map[fd];
    info->cost_time += cost;
}

void IoCollector::OnWrite(int fd, const void *buffer, size_t buf_size, ssize_t ret, long cost) {
    if (ret == -1)
        return;
    if (map.find(fd) == map.end())
        return;

    std::shared_ptr<IoInfo> info = map[fd];
    info->cost_time += cost;
}