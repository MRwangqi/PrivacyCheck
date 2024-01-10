'use strict';

const LOG_TAG = "Frida";

function doHook(className, methodName) {
    var Log = Java.use("android.util.Log");
    try {
        var fridaManager = Java.use("com.codelang.fridacheck.FridaManager");
        var javaClazz = Java.use(className);
        if (javaClazz[methodName]) {
            for (var o = 0; o < javaClazz[methodName].overloads.length; o++) {
                javaClazz[methodName].overloads[o].implementation = function () {
                    var paramStr = "("
                    for (var a = 0; a < arguments.length; a++) {
                        if (a != arguments.length - 1){
                            paramStr += arguments[a] + ",";
                        }else{
                            paramStr += arguments[a]
                        }
                    }
                    paramStr += ")";

                    fridaManager.addStackLog(className,methodName + paramStr)
                    // invoke method
                    return this[methodName].apply(this, arguments);
                };
            }
        } else {
            Log.w(LOG_TAG, className + "." + methodName + "does not exist!");
        }
    } catch (error) {
        Log.e(LOG_TAG, error.toString());
    }
}

if (Java.available) {
    Java.perform(function () {
        var startTime = Date.now();
        var Log = Java.use("android.util.Log");
        try {
            Log.v(LOG_TAG, "init  " + Process.getCurrentThreadId());
            if (Process.getCurrentThreadId() != Process.id) {
                return;
            }

            var currentTime = new Date().getTime();
            Log.v(LOG_TAG, "inject start >> ");

            var fridaManager = Java.use("com.codelang.fridacheck.FridaManager");
            var jsonObj = JSON.parse(fridaManager.getApiJson());

            for (var item in jsonObj) {
                var clazz = jsonObj[item]["clazz"]
                var methods = jsonObj[item]["method"]
                for(var m in methods){
//                     Log.e(LOG_TAG, clazz + " : " + methods[m]);
                     doHook(clazz, methods[m]);
                }
            }
            Log.v(LOG_TAG, "inject end cost time >> " + (new Date().getTime() - currentTime)+"ms");
        } catch (e) {
            Log.e(LOG_TAG, e.toString());
        }
    });
}
