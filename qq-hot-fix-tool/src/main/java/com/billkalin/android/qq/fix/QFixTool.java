package com.billkalin.android.qq.fix;

public class QFixTool {

    public QFixTool() {
        System.loadLibrary("qq-hotfix");
    }

    public native void nativeResolveClass(String[] referenceClasses, long[] classIdxs, int size);
}
