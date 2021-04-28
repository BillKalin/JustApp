//
// Created by Administrator on 2021.04.17.
//

#include "qq_hotfix.h"
#include "convert.h"

extern "C"
JNIEXPORT void JNICALL
Java_com_billkalin_android_qq_fix_QFixTool_nativeResolveClass(JNIEnv *env, jobject thiz,
                                                              jobjectArray reference_classes,
                                                              jlongArray class_idxs, jint size) {
    LOGD("start resolve class");
    int classSize = env->GetArrayLength(reference_classes);
    int classIdxSize = env->GetArrayLength(class_idxs);
    if (classSize != classIdxSize) {
        LOGD("reference_classes\'s size is not equal class_idxs\'s size");
        return;
    }
    jlong *classIdArray = env->GetLongArrayElements(class_idxs, 0);
    if (classIdArray == nullptr)
        return;
    void *handle = dlopen("/system/lib/libdvm.so", RTLD_LAZY);
    if (handle) {
        int i = 0;
        void (*findFunc)(const char *);
        while (i < ARRAY_SIZE_FIND_CLASS) {
            findFunc = (void (*)(const char *)) (dlsym(handle, SYMBOL_FIND_LOADED_CLASS[i]));
            if (findFunc) {
                break;
            }
            i++;
        }
        if (findFunc) {
            g_pDvmFindLoadedClass_Addr = reinterpret_cast<void *(*)(const char *)>(findFunc);
            i = 0;
            void (*resolveFunc)(const void *, unsigned int, int);
            while (i < ARRAY_SIZE_RESOlVE_CLASS) {
                resolveFunc = (void (*)(const void *, unsigned int, int)) dlsym(handle,
                                                                                ARRAY_RESOLVE_CLASS[i]);
                if (resolveFunc)break;
                i++;
            }

            if (resolveFunc) {
                g_pDvmResolveClass_Addr = reinterpret_cast<void *(*)(const void *, unsigned int,
                                                                     int)>(resolveFunc);
                i = 0;
                while (i < size) {
                    auto clsItem = (jstring) (env->GetObjectArrayElement(
                            reference_classes, i));
                    const char *classItem = env->GetStringUTFChars(clsItem, 0);
                    void *referenceObj = g_pDvmFindLoadedClass_Addr(classItem);
                    if (referenceObj) {
                        void *resClass;
                        try {
                            long idd = classIdArray[i];
                            resClass = g_pDvmResolveClass_Addr(referenceObj,
                                                               (unsigned int) idd, 1);
                        } catch (const char *msg) {
                            LOGD("resolve class error %s.", msg);
                        }
                        if (resClass) {
                            LOGD("resolve class success.");
                        } else {
                            LOGD("resolve class id, %d, failed", i);
                        }
                    } else {
                        LOGD("find loaded class %s, failed", classItem);
                    }
                    env->ReleaseLongArrayElements(reinterpret_cast<jlongArray>(class_idxs),
                                                  classIdArray, 0);
                    env->ReleaseStringUTFChars(clsItem, classItem);
                    i++;
                }
            } else {
                env->ReleaseLongArrayElements(reinterpret_cast<jlongArray>(class_idxs),
                                              classIdArray,
                                              0);
                LOGD("can not find the method : dvmResolveClass");
            }
        } else {
            env->ReleaseLongArrayElements(reinterpret_cast<jlongArray>(class_idxs), classIdArray,
                                          0);
            LOGD("can not find the method : find_loaded_class");
        }
    } else {
        env->ReleaseLongArrayElements(reinterpret_cast<jlongArray>(class_idxs), classIdArray, 0);
        LOGD("open libdvm.so failed...");
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_billkalin_android_qq_fix_QFixTool_convertLayoutFile(JNIEnv *env,
                                                            jobject thiz, jstring pkg,
                                                             jstring layout_name,
                                                             jstring file_path,
                                                             jstring out_file) {
    const char *pkgName = env->GetStringUTFChars(pkg, nullptr);
    const char *layoutName = env->GetStringUTFChars(layout_name, nullptr);
    const char *filePath = env->GetStringUTFChars(file_path, nullptr);
    const char *outFile = env->GetStringUTFChars(out_file, nullptr);

    convert(filePath, layoutName, pkgName, outFile);

    env->ReleaseStringUTFChars(pkg, pkgName);
    env->ReleaseStringUTFChars(layout_name, layoutName);
    env->ReleaseStringUTFChars(file_path, filePath);
    env->ReleaseStringUTFChars(out_file, outFile);
}