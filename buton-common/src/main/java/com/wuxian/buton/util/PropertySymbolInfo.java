package com.wuxian.buton.util;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;

import java.util.HashSet;
import java.util.Set;

public class PropertySymbolInfo {

    /**
     * 属性名称，首字母小写
     */
    private String name;

    /**
     * 属性类型，同时是getter的返回值类型，setter唯一的参数的类型，field的类型
     */
    private Type type;

    private Symbol.VarSymbol field;

    /**
     * 在不同的父类定义过多次，故为List,通过MethodSymbol.owner获取所声明的类
     */
    private Set<Symbol.MethodSymbol> getter = new HashSet<>();

    /**
     * 在不同的父类定义过多次，故为List,通过MethodSymbol.owner获取所声明的类
     */
    private Set<Symbol.MethodSymbol> setter = new HashSet<>();


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Symbol.VarSymbol getField() {
        return field;
    }

    public void setField(Symbol.VarSymbol field) {
        this.field = field;
    }

    public Set<Symbol.MethodSymbol> getGetter() {
        return getter;
    }

    public Set<Symbol.MethodSymbol> getSetter() {
        return setter;
    }
}
