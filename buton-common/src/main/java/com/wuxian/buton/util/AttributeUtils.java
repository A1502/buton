package com.wuxian.buton.util;

import com.wuxian.buton.core.ButonContext;
import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Pair;

import java.security.InvalidParameterException;

public class AttributeUtils {

    public static Attribute.Compound create(ButonContext context, Class<?> annotationType) {

        Type.ClassType declaredType = TypeUtils.getDeclaredType(context, annotationType);

        List<Pair<Symbol.MethodSymbol, Attribute>> attributes = List.nil();
        Attribute.Compound result = new Attribute.Compound(declaredType, attributes);
        return result;
    }

    public static Attribute.Compound appendConstantAttribute(Attribute.Compound compound, String attributeName, Object constantValue) {
        Symbol.MethodSymbol keyMethod = getAttributeMethod(compound, attributeName);
        Attribute constant = createConstantAttribute((Symbol.ClassSymbol) compound.type.tsym
                , attributeName
                , constantValue);

        Pair<Symbol.MethodSymbol, Attribute> pair = new Pair<>(keyMethod, constant);

        List<Pair<Symbol.MethodSymbol, Attribute>> list = List.nil();
        list = list.appendList(compound.values);
        list = list.append(pair);

        Attribute.Compound result = new Attribute.Compound(compound.type, list);
        return result;
    }

    /**
     * 创建常量特性
     *
     * @param annotationClassSymbol 注解类型符号
     * @param attributeName         特性名
     * @param constantValue         特性值，可以是对象和Iterable
     * @return 特性
     */
    public static Attribute createConstantAttribute(Symbol.ClassSymbol annotationClassSymbol, String attributeName, Object constantValue) {
        Symbol.MethodSymbol keyMethod = getAttributeMethod(annotationClassSymbol, attributeName);

        Attribute result;

        if (constantValue instanceof Iterable) {
            List<Attribute> attributes = List.nil();

            Type elementType = ((Type.ArrayType) keyMethod.getReturnType()).elemtype;
            for (Object item : (Iterable) constantValue) {
                attributes = attributes.append(new Attribute.Constant(elementType, item));
            }
            result = new Attribute.Array(elementType, attributes);
        } else {
            result = new Attribute.Constant(keyMethod.getReturnType(), constantValue);
        }
        return result;
    }

    public static Symbol.MethodSymbol getAttributeMethod(Attribute.Compound compound, String attributeName) {
        return getAttributeMethod((Symbol.ClassSymbol) compound.type.tsym, attributeName);
    }

    public static Symbol.MethodSymbol getAttributeMethod(Symbol.ClassSymbol annotationClassSymbol, String attributeName) {
        java.util.List<Symbol.MethodSymbol> methods = ClassSymbolUtils.getMethods(annotationClassSymbol, attributeName);
        if (methods.size() == 1) {
            return methods.get(0);
        } else {
            throw new InvalidParameterException(attributeName);
        }
    }

    /**
     * 在指定符号上获取指定类型的注解
     *
     * @param symbol         指定符号symbol
     * @param annotationType 指定注解类型
     * @return 获取成功时返回Attribute.Compound, 否则返回null
     */
    public static Attribute.Compound getDeclarationAttributes(Symbol symbol, Class<?> annotationType) {
        List<Attribute.Compound> declarationAttributes = symbol.getDeclarationAttributes();
        for (Attribute.Compound compound : declarationAttributes) {
            if (annotationType.getCanonicalName().equals(compound.type.toString())) {
                return compound;
            }
        }
        return null;
    }

    /**
     * 在指定类定义上获取指定类型的注解
     *
     * @param jcClassDecl    指定类定义
     * @param annotationType 指定注解的类型
     * @return 获取成功时返回Attribute.Compound, 否则返回null
     */
    public static Attribute.Compound getDeclarationAttributes(JCTree.JCClassDecl jcClassDecl, Class<?> annotationType) {
        return getDeclarationAttributes(jcClassDecl.sym, annotationType);
    }

    /**
     * 以filters指定的类型范围获取symbol上定义的Attributes
     *
     * @param symbol  源symbol
     * @param filters 指定ClassSymbol的范围
     * @return
     */
    public static List<Attribute.Compound> getDeclarationAttributes(Symbol symbol, List<Symbol.TypeSymbol> filters) {
        List<Attribute.Compound> result = List.nil();
        List<Attribute.Compound> declarationAttributes = symbol.getDeclarationAttributes();
        for (Attribute.Compound compound : declarationAttributes) {
            if (filters.contains(compound.type.tsym.enclClass())) {
                result = result.append(compound);
            }
        }
        return result;
    }

    /**
     * 在指定compound中按attributeName获取attribute
     *
     * @param compound      compound
     * @param attributeName attributeName
     * @return 获取成功时返回Attribute, 否则返回null
     */
    public static Attribute getAttribute(Attribute.Compound compound, String attributeName) {
        for (Pair<Symbol.MethodSymbol, Attribute> pair : compound.values) {
            if (pair.fst.name.toString().equals(attributeName)) {
                return pair.snd;
            }
        }
        return null;
    }
}
