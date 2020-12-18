//
// Created by Administrator on 2020.12.16.
//

#ifndef JUSTAPP_IO_DETECTOR_H
#define JUSTAPP_IO_DETECTOR_H

#include <sys/types.h>
#include <string>
#include "ioutils.h"
#include <android/log.h>
#include <deque>
#include <mutex>
#include <unordered_map>
#include <vector>
#include "io_collector.h"

class IoDetector {
public:
    IoDetector(const IoDetector &) = delete;

    IoDetector &operator=(IoDetector const &) = delete;

    static IoDetector &Get();

    void
    OnOpen(const char *pathname, int flag, mode_t mode, int open_ret, JavaContext &javaContext);
    void OnRead(int fd, const void* buffer, size_t buf_size, ssize_t ret, long cost);
    void OnWrite(int fd, const void* buffer, size_t buf_size, ssize_t ret, long cost);
    void onClose(int fd, int ret_id);

private:
    IoDetector();

    ~IoDetector();

    void OfferFileIoInfo(std::shared_ptr<IoInfo> &ioinfo);

    int TakeFileIoInfo(std::shared_ptr<IoInfo> &fileInfo);

    void detect();

    bool exit_;

    IoCollector collector;

    std::deque<std::shared_ptr<IoInfo>> deque_;
    std::mutex queue_mutex;
    std::condition_variable queue_cv;
};


#endif //JUSTAPP_IO_DETECTOR_H
