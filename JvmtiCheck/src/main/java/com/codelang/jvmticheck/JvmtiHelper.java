package com.codelang.jvmticheck;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Debug;
import android.text.TextUtils;
import android.util.Log;

import com.codelang.jvmticheck.bean.ApiNode;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kotlin.text.Charsets;

public class JvmtiHelper {

    public static final String TAG = "jvmti";
    private static final String LIB_NAME = "jvmticheck";

    private static final String ASSET_FILE = "privacy_api.json";
    private static Application context;

    public static void init(Application context) {
        JvmtiHelper.context = context;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ClassLoader classLoader = context.getClassLoader();
                Log.d("jvmtiagentlibpath", "classLoader " + classLoader);
                Method findLibrary = ClassLoader.class.getDeclaredMethod("findLibrary", String.class);
                String jvmtiAgentLibPath = (String) findLibrary.invoke(classLoader, LIB_NAME);
                //copy lib to /data/data/com.dodola.jvmti/files/jvmti
                Log.d("jvmtiagentlibpath", "jvmtiagentlibpath " + jvmtiAgentLibPath);
                File filesDir = context.getFilesDir();
                File jvmtiLibDir = new File(filesDir, "jvmti");
                if (!jvmtiLibDir.exists()) {
                    jvmtiLibDir.mkdirs();

                }
                File agentLibSo = new File(jvmtiLibDir, "agent.so");
                if (agentLibSo.exists()) {
                    agentLibSo.delete();
                }
                Files.copy(Paths.get(new File(jvmtiAgentLibPath).getAbsolutePath()), Paths.get((agentLibSo).getAbsolutePath()));

                Log.d("Jvmti", agentLibSo.getAbsolutePath() + "," + context.getPackageCodePath());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    Debug.attachJvmtiAgent(agentLibSo.getAbsolutePath(), null, classLoader);
                } else {
                    try {
                        Class vmDebugClazz = Class.forName("dalvik.system.VMDebug");
                        Method attachAgentMethod = vmDebugClazz.getMethod("attachAgent", String.class);
                        attachAgentMethod.setAccessible(true);
                        attachAgentMethod.invoke(null, agentLibSo.getAbsolutePath());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                System.loadLibrary(LIB_NAME);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static native String stringFromJNI();


    /**
     * call from jni
     */
    public static Map<String, List<String>> getPrivacyData() {
        if (context == null) return new HashMap<>();
        try {
            InputStream inputStream = context.getAssets().open(ASSET_FILE);
            Type type = new TypeToken<List<ApiNode>>() {
            }.getType();
            List<ApiNode> list = new Gson().fromJson(new InputStreamReader(inputStream, Charsets.UTF_8), type);

            Map<String, List<String>> map = new HashMap<>();
            for (ApiNode apiNode : list) {
                map.put(apiNode.getClazz().replace(".","/"), apiNode.getMethod());
            }
            return map;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}