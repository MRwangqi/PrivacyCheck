package com.codelang.module.bean

data class ApiCallResult @JvmOverloads constructor(
    var clazz: String = "",
    var method: String = "",
    var dep: String = "",
    var callClazz: String = "",
    var callMethod: String = ""
)