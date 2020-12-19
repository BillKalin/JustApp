//
// Created by Administrator on 2020.12.16.
//

#include "io_detector.h"
#include <thread>
#include "ioutils.h"

IoDetector &IoDetector::Get() {
    static IoDetector instance;
    return instance;
}

IoDetector::IoDetector() {
    LOGD("IoDetector()");
    exit_ = false;
    std::thread detect_thread(&IoDetector::detect, this);
    detect_thread.detach();
}

IoDetector::~IoDetector() {
    LOGD("~IoDetector()");
    exit_ = true;
}

void IoDetector::detect() {
    LOGD("detect()");
    std::shared_ptr<IoInfo> ioinfo;
    while (true) {
        int ret = TakeFileIoInfo(ioinfo);
        if (ret == -1) {
            break;
        }

        LOGD("detect : path = %s, file_size=%ld, op_count=%d, cost_time=%ld, stack_trace=%s", ioinfo->path_.c_str(),
             ioinfo->file_size, ioinfo->op_count, ioinfo->rw_cost_time, ioinfo->javaContext_.stack_.c_str());

        ioinfo = nullptr;
    }
}

void IoDetector::OnOpen(const char *pathname, int flag, mode_t mode, int open_ret,
                        JavaContext &javaContext) {
    collector.OnOpen(pathname, flag, mode, open_ret, javaContext);
}

int IoDetector::TakeFileIoInfo(std::shared_ptr<IoInfo> &fileInfo) {
    std::unique_lock<std::mutex> lock(queue_mutex);
    while (deque_.empty()) {
        queue_cv.wait(lock);
        if (exit_) {
            return -1;
        }
    }
    fileInfo = deque_.front();
    deque_.pop_front();
    return 0;
}

void IoDetector::onClose(int fd, int ret_id) {
    std::shared_ptr<IoInfo> ioinfo = collector.OnClose(fd, ret_id);
    if (ioinfo == nullptr)
        return;
    OfferFileIoInfo(ioinfo);
}

void IoDetector::OfferFileIoInfo(std::shared_ptr<IoInfo> &ioinfo) {
    std::unique_lock<std::mutex> lock(queue_mutex);
    deque_.push_back(ioinfo);
    queue_cv.notify_one();
    lock.unlock();
}

void IoDetector::OnRead(int fd, const void *buffer, size_t buf_size, ssize_t ret, long cost) {
    collector.OnRead(fd, buffer, buf_size, ret, cost);
}

void IoDetector::OnWrite(int fd, const void *buffer, size_t buf_size, ssize_t ret, long cost) {
    collector.OnWrite(fd, buffer, buf_size, ret, cost);
}

