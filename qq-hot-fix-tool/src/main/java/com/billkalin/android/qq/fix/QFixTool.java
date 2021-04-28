package com.billkalin.android.qq.fix;

public class QFixTool {

    public QFixTool() {
        System.loadLibrary("qq-hotfix");
    }

    public native void nativeResolveClass(String[] referenceClasses, long[] classIdxs, int size);
    public native void convertLayoutFile(String pkg, String layoutName, String filePath, String outFile);
}
