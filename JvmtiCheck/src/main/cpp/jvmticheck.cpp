#include <jni.h>
#include <string>
#include <android/log.h>
#include "jvmti.h"

#define LOG_TAG "jvmti"
#define ALOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, __VA_ARGS__)
#define ALOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define ALOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define ALOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define ALOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static jvmtiEnv *localJvmtiEnv;

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

void MethodEntry(jvmtiEnv *jvmti_env, JNIEnv *jni_env, jthread thread, jmethodID method) {
    char *name = NULL;
    char *signature = NULL;
    char *generic = NULL;
    jvmtiError result;

    jvmtiThreadInfo tinfo;
    jvmti_env->GetThreadInfo(thread, &tinfo);

    jvmti_env->GetMethodName(method, &name, &signature, &generic);

    jclass clazz;
    jvmti_env->GetMethodDeclaringClass(method, &clazz);

    char *classSign = NULL;
    jvmti_env->GetClassSignature(clazz, &classSign, nullptr);


    if (!strcmp(name, "setText")) {
        ALOGI("==========触发 MethodEntry 线程名%s class=%s 方法名=%s%s =======", tinfo.name ,classSign,name, signature);
    }
    if (!strcmp(name, "thanks")) {
        ALOGI("==========触发 MethodEntry 线程名%s class=%s 方法名=%s%s =======", tinfo.name ,classSign,name, signature);
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
    localJvmtiEnv = jvmti_env;
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
    ALOGI("==========Agent_OnAttach=======22222");
    return JNI_OK;
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
    jclass clazz = env->FindClass("com/codelang/jvmticheck/JvmtiHelper");
    env->RegisterNatives(clazz, methods, sizeof(methods) / sizeof(methods[0]));
    return JNI_VERSION_1_6;
}
