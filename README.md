# 隐私 api 调用检查全套工具

说明：
- 以下所有工具都需要搭配 [privacy_api.json](privacy_api.json) 文件进行检查，文件内的隐私合规 api 参考来自开源项目 [Camille](https://github.com/zhengjim/camille)，也欢迎大家贡献隐私合规 api。


静态检查工具：
- [LintCheck](./LintCheck/README.md): lint 静态检查
- [DepCheck](./DepCheck/README.md): 字节码依赖分析
- [ApkCheck](./ApkCheck/README.md): apk smail 文件分析
动态检查工具:
- [RuntimeCheck](./RuntimeCheck/README.md): 运行时的 AOP Hook
- [FridaCheck](./FridaCheck/README.md):frida gadget hook
- [JvmtiCheck](./JvmtiCheck/README.md):jvmti methodEntry 监听


参考链接:
- [Android Hook 技术](https://meik2333.com/posts/android-hook/)
- [Frida Gadget](https://frida.re/docs/gadget/)
- [网易云音乐 Android 隐私合规静态检查](https://musicfe.com/android-privacy/)
- [Android App隐私合规检测辅助工具 Camille](https://github.com/zhengjim/camille)
- [非root环境下frida的两种使用方式](https://nszdhd1.github.io/2021/06/15/%E9%9D%9Eroot%E7%8E%AF%E5%A2%83%E4%B8%8Bfrida%E7%9A%84%E4%B8%A4%E7%A7%8D%E4%BD%BF%E7%94%A8%E6%96%B9%E5%BC%8F/)
- [Mobile Security Framework (MobSF)](https://github.com/MobSF/Mobile-Security-Framework-MobSF?tab=readme-ov-file#mobile-security-framework-mobsf)
- [ART上的动态Java方法hook框架](https://blog.canyie.top/2020/04/27/dynamic-hooking-framework-on-art/)