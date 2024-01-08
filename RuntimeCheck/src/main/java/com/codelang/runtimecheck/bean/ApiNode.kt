package com.codelang.runtimecheck.bean

data class ApiNode @JvmOverloads constructor(
    var clazz: String? = null,
    var method: List<String>? = null
)