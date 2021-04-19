# Just App

这个只是用来记录学习的App，包含的内容都是平时工作或者学习总结。

## performance library
    性能优化检查工具集合
    
    1. UI 耗时检查
    2. 待续...

## openapi library
    
  插件化里的hook技术，在Android P以上会受到限制，因此需要绕过系统对非SDK接口(带有@hide相关方法及字段)检查，参考并fork 自 [FreeReflection](https://github.com/tiann/FreeReflection)。
  
  只需要调用
  
  ```java
  // useNative 默认使用native 绕过方式，targetVersion Api的版本
  OpenApi.open(useNative: Boolean, targetVersion: Int)
  ```
      
## kreflect library

  反射工具类集合
  
   ```java
   //反射某个类，并获取对象
   KQuietReflect.on("android.app.ActivityThread").method("currentActivityThread").call<Any>()
   ```
   
## hook library

  Hook相关代码

## xhook

  爱奇艺的开源项目xhook源码

## base app asm/plugin/spi

  gradle 插件架构，便于新增自定义gradle插件，参考滴滴出行的[booster](https://github.com/didi/booster)开源项目

## r-inline-plugin

    Android R类内联插件, 用于APK瘦身

## io_monitor

    参考腾讯开源项目Matrix的io优化，hook文件读写来判定文件读写的上的优化

## qzone-hot-fix-plugin

    QQ空间热修复方案中，在类的构造函数中插桩代码的插件代码

## qq-hot-fix-tool

    手Q热修复方案：native的加载补丁dex中的class