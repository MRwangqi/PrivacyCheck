package com.codelang.checks.bean;

import java.util.List;

public class ApiNode {

    private String clazz;
    private List<String> method;

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public List<String> getMethod() {
        return method;
    }

    public void setMethod(List<String> method) {
        this.method = method;
    }
}
