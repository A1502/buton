package com.wuxian.buton.util;

import com.wuxian.buton.core.ButonContext;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;

public class TypeUtils {
    public static Type.ClassType getDeclaredType(ButonContext context, Class<?> clazz) {
        Symbol.ClassSymbol symbol = context.getJavacElements().getTypeElement(clazz.getCanonicalName());
        Type.ClassType result = (Type.ClassType) context.getJavacTypes().getDeclaredType(symbol);
        return result;
    }
}
