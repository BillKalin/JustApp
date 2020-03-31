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

## base app asm/plugin/spi

  gradle 插件架构，便于新增自定义gradle插件，参考滴滴出行的[booster](https://github.com/didi/booster)开源项目
  