# 简介

一个Android模拟器检测库，实现原理简单，且稳定。

## 如何使用

增加

``` gradle
 repositories {
    maven { url "https://jitpack.io" }
 }
```

引入

``` gradle
implementation 'com.github.jarofcolor:EmulatorCheck:1.0.0'
```

## 使用

```kotlin
//是否为模拟器
val isEmulator = EmulatorCheck.isEmulator()

//模拟器名称
val emulatorName = EmulatorCheck.getEmulatorName()
```

未检测出的只能表明不能确定设备类型。