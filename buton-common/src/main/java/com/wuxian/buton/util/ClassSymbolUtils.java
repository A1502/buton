package com.wuxian.buton.util;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version 1.0.0
 * @author: solomon
 * @date: 2020-04-07 15:55
 */
public class ClassSymbolUtils {

    /**
     * 递归所有父类获取全部符号
     *
     * @param classSymbol
     * @return
     */
    public static List<Symbol> getRecursiveEnclosedElements(Symbol.ClassSymbol classSymbol) {
        List<Symbol> result = new ArrayList<>(classSymbol.getEnclosedElements());
        if (classSymbol.getKind().equals(ElementKind.INTERFACE)) {
            for (Type type : classSymbol.getInterfaces()) {
                Symbol.ClassSymbol subInterface = (Symbol.ClassSymbol) type.tsym;
                result.addAll(getRecursiveEnclosedElements(subInterface));
            }
        } else {
            //比如注解类型就没有superType，会得到null。不可再递归
            Symbol.TypeSymbol superType = classSymbol.getSuperclass().asElement();
            if (superType != null) {
                Symbol.ClassSymbol superClassSymbol = superType.enclClass();
                if (superClassSymbol != null && !(Object.class.getCanonicalName().equals(superClassSymbol.fullname.toString()))) {
                    List<Symbol> elements = getRecursiveEnclosedElements(superClassSymbol);
                    result.addAll(elements);
                }
            }
        }
        return result;
    }

    public static Symbol.VarSymbol getField(Symbol.ClassSymbol classSymbol, String fieldName) {
        List<Symbol> symbols = getRecursiveEnclosedElements(classSymbol);
        for (Symbol symbol : symbols) {
            if (symbol.getKind().equals(ElementKind.LOCAL_VARIABLE) && symbol.name.toString().equals(fieldName)) {
                return (Symbol.VarSymbol) symbol;
            }
        }
        return null;
    }

    public static List<Symbol.MethodSymbol> getMethods(Symbol.ClassSymbol classSymbol, String methodName) {
        List<Symbol> symbols = getRecursiveEnclosedElements(classSymbol);

        List<Symbol.MethodSymbol> result = new ArrayList<>();

        for (Symbol symbol : symbols) {
            if (symbol.getKind().equals(ElementKind.METHOD) && symbol.name.toString().equals(methodName)) {
                result.add((Symbol.MethodSymbol) symbol);
            }
        }
        return result;
    }
}
