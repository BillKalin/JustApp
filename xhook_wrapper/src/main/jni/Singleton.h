//
// Created by Administrator on 2020.12.19.
//
#include <iostream>

#ifndef JUSTAPP_SINGLETON_H
#define JUSTAPP_SINGLETON_H

template<typename T>

class Singleton {
public:

    virtual ~Singleton() {
        std::cout << "destructor called." << std::endl;
    }

    Singleton(const Singleton &) = delete;

    Singleton &operator=(const Singleton &) = delete;

    static T& getInstance() {
        static T instance;
        return instance;
    }

protected:
    Singleton() {
        std::cout << "constructor called." << std::endl;
    }
};


#endif //JUSTAPP_SINGLETON_H
