package com.codelang.module.bean

import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.MethodNode


data class Clazz(
    var className: String? = null,
    var superName: String? = null,
    var interfaces: List<String>? = null,
    var fields: List<FieldNode>? = null,
    var methods: List<MethodNode>? = null,
    var visibleAnnotations: List<AnnotationNode>? = null,
    var dep: String?=null
)
