#include <jni.h>
#include <string>
#include <android/log.h>
#include "jvmti.h"
#include <map>
#include <vector>

#define LOG_TAG "jvmti"
#define ALOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, __VA_ARGS__)
#define ALOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define ALOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define ALOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define ALOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)


void initPrivacy(JavaVM *vm, jvmtiEnv *jvmti_env);
std::map<std::string, std::vector<std::string>> javaToMap(JNIEnv *env, jobject javaMap);
static std::map<std::string, std::vector<std::string>> map;

/**
 * 从类签名中提取类名
 * @param classSign  Lcom/codelang/jvmticheck/JvmtiHelper;
 * @return  com/codelang/jvmticheck/JvmtiHelper
 */
char *extractClassName(char *classSign) {
    char *classNameStart = NULL;
    char *classNameEnd = NULL;
    char *className = NULL;

    // 查找 'L' 的位置
    classNameStart = strchr(classSign, 'L');

    if (classNameStart != NULL) {
        classNameStart++; // 移动指针到类名的起始位置
        classNameEnd = strchr(classNameStart, ';'); // 查找分号(';')

        if (classNameEnd != NULL) {
            // 提取所需的字符串
            int charLength = classNameEnd - classNameStart;
            className = (char *) malloc(charLength + 1); // 添加额外的位置用于字符串终止符
            strncpy(className, classNameStart, charLength);
            className[charLength] = '\0'; // 手动添加字符串终止符
        }
    }
    return className;
}

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


    // 从集合中查找是否命中隐私 api
    auto find = map.find(extractClassName(classSign));
    if (find != map.end()) {
        std::vector<std::string> vec = find->second;
        for (int i = 0; i < vec.size(); i++) {
            if (!strcmp(vec[i].c_str(), methodName)) {
                ALOGI("========== find MethodEntry 线程名%s class=%s 方法名=%s%s =======",
                      tinfo.name, extractClassName(classSign), methodName, methodSign);
                // 打印堆栈信息
                jclass  throwable_class = jni_env->FindClass("java/lang/Throwable");
                jmethodID  throwable_init = jni_env->GetMethodID(throwable_class, "<init>", "(Ljava/lang/String;)V");
                jobject throwable_obj = jni_env->NewObject(throwable_class, throwable_init, jni_env->NewStringUTF("privacy api"));
                jmethodID throwable_mid = jni_env->GetMethodID(throwable_class, "printStackTrace", "()V");
                jni_env->CallVoidMethod(throwable_obj, throwable_mid);
                break;
            }
        }
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


    map = javaToMap(env, mapObj);
}


std::map<std::string, std::vector<std::string>> javaToMap(JNIEnv *env, jobject javaMap) {
    std::map<std::string, std::vector<std::string>> cppMap;

    jclass mapClass = env->GetObjectClass(javaMap);
    jmethodID entrySetMethod = env->GetMethodID(mapClass, "entrySet", "()Ljava/util/Set;");
    jclass setClass = env->FindClass("java/util/Set");
    jmethodID iteratorMethod = env->GetMethodID(setClass, "iterator", "()Ljava/util/Iterator;");
    jclass iteratorClass = env->FindClass("java/util/Iterator");
    jmethodID nextMethod = env->GetMethodID(iteratorClass, "next", "()Ljava/lang/Object;");

    jobject entrySet = env->CallObjectMethod(javaMap, entrySetMethod);
    jobject iterator = env->CallObjectMethod(entrySet, iteratorMethod);

    while (env->CallBooleanMethod(iterator, env->GetMethodID(iteratorClass, "hasNext", "()Z"))) {
        jobject entry = env->CallObjectMethod(iterator, nextMethod);

        jclass entryClass = env->GetObjectClass(entry);
        jmethodID getKeyMethod = env->GetMethodID(entryClass, "getKey", "()Ljava/lang/Object;");
        jmethodID getValueMethod = env->GetMethodID(entryClass, "getValue", "()Ljava/lang/Object;");

        jstring key = (jstring) env->CallObjectMethod(entry, getKeyMethod);
        jobject value = env->CallObjectMethod(entry, getValueMethod);

        const char *keyStr = env->GetStringUTFChars(key, nullptr);

        jclass listClass = env->GetObjectClass(value);
        jmethodID listToArrayMethod = env->GetMethodID(listClass, "toArray",
                                                       "()[Ljava/lang/Object;");
        jobjectArray valueArray = (jobjectArray) env->CallObjectMethod(value, listToArrayMethod);

        std::vector<std::string> cppVec;
        jsize length = env->GetArrayLength(valueArray);
        for (jsize i = 0; i < length; i++) {
            jstring str = (jstring) env->GetObjectArrayElement(valueArray, i);
            const char *valueStr = env->GetStringUTFChars(str, nullptr);
            cppVec.push_back(std::string(valueStr));
            env->ReleaseStringUTFChars(str, valueStr);
            env->DeleteLocalRef(str);
        }

        cppMap[std::string(keyStr)] = cppVec;

        env->ReleaseStringUTFChars(key, keyStr);
        env->DeleteLocalRef(key);
        env->DeleteLocalRef(value);
        env->DeleteLocalRef(entry);
    }

    return cppMap;
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
