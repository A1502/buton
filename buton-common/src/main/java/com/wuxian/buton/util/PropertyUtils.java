package com.wuxian.buton.util;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropertyUtils {

    private static final String IS = "is";
    private static final String GET = "get";
    private static final String SET = "set";

    public enum PropertyLevel {
        GETTER_SETTER_BOTH,
        GETTER_SETTER_ANY_ONE,
        FIELD_AT_LEAST
    }

    public static String calcGetterName(String fieldName) {
        return GET + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }

    public static String calcSetterName(String fieldName) {
        return SET + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }

    public static boolean isGetter(Symbol.MethodSymbol methodSymbol) {
        //必须是非静态的
        if (!methodSymbol.isStatic() && methodSymbol.getModifiers().contains(Modifier.PUBLIC)) {
            String methodName = methodSymbol.name.toString();
            //名称上满足要求：以get开头
            if (methodName.length() > GET.length() && methodName.startsWith(GET)) {
                //无参数或参数0个
                if (methodSymbol.params() == null || methodSymbol.params().length() == 0) {
                    //返回值不能是void
                    return !(methodSymbol.getReturnType() instanceof Type.JCVoidType);
                }
            }
        }
        return false;
    }

    public static boolean isSetter(Symbol.MethodSymbol methodSymbol) {
        //必须是非静态的
        if (!methodSymbol.isStatic()) {
            String methodName = methodSymbol.name.toString();
            //名称上满足要求：以set开头
            if (methodName.length() > SET.length() && methodName.startsWith(SET)) {
                //参数必须1个
                if (methodSymbol.params() != null && methodSymbol.params().length() == 1) {
                    //返回值是void
                    return methodSymbol.getReturnType() instanceof Type.JCVoidType;
                }
            }
        }
        return false;
    }

    public static boolean isPropertyField(Symbol varSymbol) {
        return (varSymbol.getKind().equals(ElementKind.FIELD))
                && !varSymbol.getModifiers().contains(Modifier.STATIC)
                && !varSymbol.getModifiers().contains(Modifier.PUBLIC);
    }

    public static PropertySymbolInfo getPropertySymbolInfo(Symbol.MethodSymbol methodSymbol) {

        if (isGetter(methodSymbol)) {
            PropertySymbolInfo result = new PropertySymbolInfo();
            result.setName(getPropertyName(methodSymbol.name.toString()));
            result.getGetter().add(methodSymbol);
            result.setType(methodSymbol.getReturnType());
            return result;
        } else if (isSetter(methodSymbol)) {
            PropertySymbolInfo result = new PropertySymbolInfo();
            result.setName(getPropertyName(methodSymbol.name.toString()));
            result.getSetter().add(methodSymbol);
            result.setType(methodSymbol.params().get(0).type);
            return result;
        }
        return null;
    }

    public static PropertySymbolInfo getPropertySymbolInfo(Symbol.VarSymbol varSymbol) {
        PropertySymbolInfo result = new PropertySymbolInfo();
        result.setName(varSymbol.name.toString());
        result.setType(varSymbol.type);
        result.setField(varSymbol);
        return result;
    }

    private static String getPropertyName(String getterSetterName) {

        String name = getterSetterName.substring(GET.length());

        name = name.substring(0, 1).toLowerCase() + name.substring(1);

        return name;
    }

    /**
     * 当满足下面两个条件
     * 1.one和another的name和type相同时
     * 2.one和another的field相同，或者至少一个为null
     * 把input和result的内容合并
     *
     * @param one 输入项
     * @param one 另一个输入项
     * @return 满足合并条件时返回合并后新的对象，否则返回null
     */
    public static PropertySymbolInfo merge(PropertySymbolInfo one, PropertySymbolInfo another) {
        boolean condition1_1 = one.getName() != null && one.getName().equals(another.getName());
        boolean condition1_2 = one.getType() != null && another.getType() != null
                && one.getType().toString().equals(another.getType().toString());

        if (condition1_1 && condition1_2) {
            boolean condition2_1 = one.getField() == null || another.getField() == null;
            boolean condition2_2 = one.getField() != null && one.getField().equals(another.getField());
            boolean condition2_3 = another.getField() != null && another.getField().equals(one.getField());
            if (condition2_1 || condition2_2 || condition2_3) {

                PropertySymbolInfo result = new PropertySymbolInfo();
                result.setName(one.getName());
                result.setType(one.getType());
                result.setField(one.getField() != null ? one.getField() : another.getField());

                result.getGetter().addAll(one.getGetter());
                result.getGetter().addAll(another.getGetter());

                result.getSetter().addAll(one.getSetter());
                result.getSetter().addAll(another.getSetter());

                return result;
            }
        }
        return null;
    }

    /**
     * 递归所有父类获取全部属性符号信息
     *
     * @param classSymbol
     * @param level       识别为property的要求
     * @return 属性符号集合
     */
    public static List<PropertySymbolInfo> getRecursivePropertySymbolInfo(Symbol.ClassSymbol classSymbol, PropertyLevel level) {
        List<Symbol> input = ClassSymbolUtils.getRecursiveEnclosedElements(classSymbol);

        Map<String, PropertySymbolInfo> dictionary = new HashMap<>();

        for (Symbol symbol : input) {
            PropertySymbolInfo propertySymbolInfo = null;
            //添加getter setter
            if (symbol instanceof Symbol.MethodSymbol) {
                Symbol.MethodSymbol method = (Symbol.MethodSymbol) symbol;

                //必须是getter setter之一
                if (isGetter(method) || isSetter(method)) {
                    propertySymbolInfo = PropertyUtils.getPropertySymbolInfo(method);
                }
            }
            //添加字段
            else if (isPropertyField(symbol)) {
                propertySymbolInfo = PropertyUtils.getPropertySymbolInfo((Symbol.VarSymbol) symbol);
            }
            //开始merge
            if (propertySymbolInfo != null) {
                String propertyName = propertySymbolInfo.getName();
                if (dictionary.containsKey(propertyName)) {
                    PropertySymbolInfo fromDic = dictionary.get(propertyName);
                    PropertySymbolInfo newValue = PropertyUtils.merge(fromDic, propertySymbolInfo);
                    dictionary.replace(propertyName, newValue);
                } else {
                    dictionary.put(propertyName, propertySymbolInfo);
                }
            }
        }

        List<PropertySymbolInfo> result = new ArrayList<>();

        for (PropertySymbolInfo item : dictionary.values()) {
            if (isProperty(item, level)) {
                result.add(item);
            }
        }
        return result;
    }

    public static boolean isProperty(PropertySymbolInfo symbolInfo, PropertyLevel level) {
        boolean allowed = false;
        switch (level) {
            case GETTER_SETTER_BOTH:
                allowed = symbolInfo.getGetter().size() > 0 && symbolInfo.getSetter().size() > 0;
                break;
            case GETTER_SETTER_ANY_ONE:
                allowed = symbolInfo.getGetter().size() > 0 || symbolInfo.getSetter().size() > 0;
                break;
            case FIELD_AT_LEAST:
                allowed = symbolInfo.getField() != null;
                break;
            default:
                throw new InvalidParameterException(level.toString());
        }
        return allowed;
    }
}
