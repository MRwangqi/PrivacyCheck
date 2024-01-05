package com.codelang.module.bean

data class ApiNode @JvmOverloads constructor(
    var clazz: String? = null,
    var method: List<String>? = null
)