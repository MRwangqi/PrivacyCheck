#include <jni.h>
#include <string>
#include <android/log.h>
#include "jvmti.h"
#include <map>

#define LOG_TAG "jvmti"
#define ALOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, __VA_ARGS__)
#define ALOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define ALOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define ALOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define ALOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)


void initPrivacy(JavaVM *vm, jvmtiEnv *jvmti_env);

jvmtiEnv *CreateJvmtiEnv(JavaVM *vm) {
    jvmtiEnv *jvmti_env;
    jint result = vm->GetEnv((void **) &jvmti_env, JVMTI_VERSION_1_2);
    if (result != JNI_OK) {
        return nullptr;
    }
    return jvmti_env;
}

void SetAllCapabilities(jvmtiEnv *jvmti) {
    jvmtiCapabilities caps;
    jvmtiError error;
    error = jvmti->GetPotentialCapabilities(&caps);
    error = jvmti->AddCapabilities(&caps);
}

static jclass clsJvmtiHelper;

void MethodEntry(jvmtiEnv *jvmti_env, JNIEnv *jni_env, jthread thread, jmethodID method) {
    char *methodName = NULL;
    char *methodSign = NULL;
    char *generic = NULL;
    jvmtiError result;

    jvmtiThreadInfo tinfo;
    jvmti_env->GetThreadInfo(thread, &tinfo);

    jvmti_env->GetMethodName(method, &methodName, &methodSign, &generic);

    jclass clazz;
    jvmti_env->GetMethodDeclaringClass(method, &clazz);

    char *classSign = NULL;
    jvmti_env->GetClassSignature(clazz, &classSign, nullptr);


    if (!strcmp(methodName, "printLog") &&
        !strcmp(classSign, "Lcom/codelang/jvmticheck/JvmtiHelper;")) {
//        ALOGI("==========触发 printLog 线程名%s class=%s 方法名=%s%s =======", tinfo.name,
//              classSign, methodName, methodSign);
        // todo 避免 printLog 方法的死循环
        return;
    }

    // todo 放开如下判断，会导致崩溃，应该是死循环问题，待解
    // 在 setText 的时候再做 java 方法回调，MethodEntry 方法回调频次太高了，会导致卡顿
    if (!strcmp(methodName, "setText") && !strcmp(classSign, "Landroid/widget/TextView;")) {
        ALOGI("==========触发 MethodEntry 线程名%s class=%s 方法名=%s%s =======", tinfo.name,
              classSign, methodName, methodSign);


        if (clsJvmtiHelper == nullptr) {

            // 在 boot classloader 中，使用上下文类加载器加载应用的 class
            // Class.forName("com/codelang/jvmticheck/JvmtiHelper",true,contextClassLoader)
            jstring className = jni_env->NewStringUTF("com.codelang.jvmticheck.JvmtiHelper");
            jclass currentClass = (jni_env)->FindClass("java/lang/Class");
            jmethodID jmethodId = jni_env->GetStaticMethodID(currentClass, "forName",
                                                             "(Ljava/lang/String;ZLjava/lang/ClassLoader;)Ljava/lang/Class;");
            jclass cls = (jclass) jni_env->CallStaticObjectMethod(currentClass, jmethodId,
                                                                  className,
                                                                  JNI_FALSE,
                                                                  tinfo.context_class_loader);
            // 全局缓存 class
            clsJvmtiHelper = (jclass) jni_env->NewGlobalRef(cls);

        }

        jmethodID mPrintLog = jni_env->GetStaticMethodID(clsJvmtiHelper, "printLog",
                                                         "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");

        jstring clsName = jni_env->NewStringUTF(classSign);
        jstring mName = jni_env->NewStringUTF(methodName);
        jstring mSign = jni_env->NewStringUTF(methodSign);
        jni_env->CallStaticVoidMethod(clsJvmtiHelper, mPrintLog, clsName, mName, mSign);


        jni_env->DeleteLocalRef(clsName);
        jni_env->DeleteLocalRef(mName);
        jni_env->DeleteLocalRef(mSign);

    }
}


void SetEventNotification(jvmtiEnv *jvmti, jvmtiEventMode mode,
                          jvmtiEvent event_type) {
    jvmtiError err = jvmti->SetEventNotificationMode(mode, event_type, nullptr);
}


extern "C" JNIEXPORT jint JNICALL Agent_OnAttach(JavaVM *vm, char *options, void *reserved) {
    jvmtiEnv *jvmti_env = CreateJvmtiEnv(vm);

    if (jvmti_env == nullptr) {
        return JNI_ERR;
    }
    // 初始化隐私合规
    initPrivacy(vm, jvmti_env);

    SetAllCapabilities(jvmti_env);

    jvmtiEventCallbacks callbacks;
    memset(&callbacks, 0, sizeof(callbacks));
    callbacks.MethodEntry = &MethodEntry;

    int error = jvmti_env->SetEventCallbacks(&callbacks, sizeof(callbacks));

    SetEventNotification(jvmti_env, JVMTI_ENABLE,
                         JVMTI_EVENT_GARBAGE_COLLECTION_START);
    SetEventNotification(jvmti_env, JVMTI_ENABLE,
                         JVMTI_EVENT_GARBAGE_COLLECTION_FINISH);
    SetEventNotification(jvmti_env, JVMTI_ENABLE,
                         JVMTI_EVENT_NATIVE_METHOD_BIND);
    SetEventNotification(jvmti_env, JVMTI_ENABLE,
                         JVMTI_EVENT_VM_OBJECT_ALLOC);
    SetEventNotification(jvmti_env, JVMTI_ENABLE,
                         JVMTI_EVENT_OBJECT_FREE);
    SetEventNotification(jvmti_env, JVMTI_ENABLE,
                         JVMTI_EVENT_CLASS_FILE_LOAD_HOOK);
    SetEventNotification(jvmti_env, JVMTI_ENABLE,
                         JVMTI_EVENT_METHOD_ENTRY);
    ALOGI("==========Agent_OnAttach=======");
    return JNI_OK;
}

void initPrivacy(JavaVM *vm, jvmtiEnv *jvmti_env) {

    // 获取当前线程
    JNIEnv *env;
    jint result = vm->GetEnv((void **) &env, JNI_VERSION_1_6);
    if (result != JNI_OK) {
        return;
    }
    jclass threadClass = env->FindClass("java/lang/Thread");
    jmethodID currentThreadMethod = env->GetStaticMethodID(threadClass, "currentThread",
                                                           "()Ljava/lang/Thread;");
    jobject threadObj = env->CallStaticObjectMethod(threadClass, currentThreadMethod);
    jthread currentThread = env->NewGlobalRef(threadObj);

    jvmtiThreadInfo tinfo;
    jvmti_env->GetThreadInfo(currentThread, &tinfo);
    ALOGI("========== Agent_OnAttach 线程名%s  =======", tinfo.name);

    // 在 boot classloader 中，使用上下文类加载器加载应用的 class
    // Class.forName("com/codelang/jvmticheck/JvmtiHelper",true,contextClassLoader)
    jstring className = env->NewStringUTF("com.codelang.jvmticheck.JvmtiHelper");
    jclass currentClass = (env)->FindClass("java/lang/Class");
    jmethodID jmethodId = env->GetStaticMethodID(currentClass, "forName",
                                                 "(Ljava/lang/String;ZLjava/lang/ClassLoader;)Ljava/lang/Class;");
    jclass cls = (jclass) env->CallStaticObjectMethod(currentClass, jmethodId,
                                                      className,
                                                      JNI_FALSE,
                                                      tinfo.context_class_loader);


    jmethodID privacyData = env->GetStaticMethodID(cls, "getPrivacyData", "()Ljava/util/Map;");
    jobject mapObj = env->CallStaticObjectMethod(cls, privacyData);

}







extern "C" JNIEXPORT jstring JNICALL
tempStringFromJNI(
        JNIEnv *env,
        jclass /* this */) {
    std::string hello = "Hello from C++";

    ALOGE("Hello from C++ %s", "aaa");
    return env->NewStringUTF(hello.c_str());
}


static JNINativeMethod methods[] = {
        {"stringFromJNI", "()Ljava/lang/String;", reinterpret_cast<jlong *>(tempStringFromJNI)},
};

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }
    ALOGI("==============library load====================");
    jclass jvmtiClazz = env->FindClass("com/codelang/jvmticheck/JvmtiHelper");
    env->RegisterNatives(jvmtiClazz, methods, sizeof(methods) / sizeof(methods[0]));


    return JNI_VERSION_1_6;
}
