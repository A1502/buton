package com.wuxian.buton.util;

import com.wuxian.buton.core.ButonContext;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;

public class JCTreeUtils {

    private static final String THIS = "this";

    public static JCTree.JCVariableDecl getField(JCTree.JCClassDecl classDecl, String fieldName) {
        for (JCTree jcTree : classDecl.defs) {
            if (jcTree instanceof JCTree.JCVariableDecl) {
                JCTree.JCVariableDecl field = (JCTree.JCVariableDecl) jcTree;
                if (field.name.toString().equals(fieldName)) {
                    return field;
                }
            }
        }
        return null;
    }

    public static JCTree.JCMethodDecl createSetter(ButonContext butonContext
            , String fieldName, Type type) {
        TreeMaker treeMaker = butonContext.getTreeMaker();

        Name fieldNameObj = butonContext.getNames().fromString(fieldName);
        JCTree.JCExpression jcTypeExp = treeMaker.Type(type);

        //方法的访问级别
        JCTree.JCModifiers modifiers = treeMaker.Modifiers(PropertyUtils.GETTER_SETTER_FLAG);

        //定义方法名
        Name methodName = butonContext.getNames().fromString(PropertyUtils.calcSetterName(fieldName));
        //定义返回值类型
        JCTree.JCExpression returnMethodType = treeMaker.Type(new Type.JCVoidType());
        ListBuffer<JCTree.JCStatement> statements = new ListBuffer<>();
        statements.append(treeMaker.Exec(treeMaker.Assign(treeMaker.Select(
                treeMaker.Ident(butonContext.getNames().fromString(THIS))
                , fieldNameObj)
                , treeMaker.Ident(fieldNameObj))));
        //定义方法体
        JCTree.JCBlock methodBody = treeMaker.Block(0, statements.toList());
        List<JCTree.JCTypeParameter> methodGenericParams = List.nil();
        //定义入参
        JCTree.JCVariableDecl param = treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER
                , List.nil())
                , fieldNameObj
                , jcTypeExp
                , null);
        //设置入参
        List<JCTree.JCVariableDecl> parameters = List.of(param);
        List<JCTree.JCExpression> throwsClauses = List.nil();
        //构建新方法
        return treeMaker.MethodDef(modifiers
                , methodName
                , returnMethodType
                , methodGenericParams
                , parameters
                , throwsClauses
                , methodBody
                , null);
    }

    public static JCTree.JCMethodDecl createSetter(ButonContext butonContext
            , JCTree.JCVariableDecl jcVariableDecl) {
        return createSetter(butonContext, jcVariableDecl.name.toString(), jcVariableDecl.vartype.type);
    }

    public static JCTree.JCMethodDecl createGetter(ButonContext butonContext
            , JCTree.JCVariableDecl jcVariableDecl) {
        return createGetter(butonContext, jcVariableDecl.name.toString(), jcVariableDecl.vartype.type);
    }

    public static JCTree.JCMethodDecl createGetter(ButonContext butonContext
            , String fieldName, Type type) {

        TreeMaker treeMaker = butonContext.getTreeMaker();
        Name fieldNameObj = butonContext.getNames().fromString(fieldName);
        JCTree.JCExpression jcTypeExp = treeMaker.Type(type);
        Name methodName = butonContext.getNames().fromString(PropertyUtils.calcGetterName(fieldName));

        ListBuffer<JCTree.JCStatement> statements = new ListBuffer<>();
        statements.append(treeMaker.Return(treeMaker.Select(treeMaker.Ident(butonContext.getNames().fromString(THIS)),
                fieldNameObj)));
        JCTree.JCBlock body = treeMaker.Block(0, statements.toList());
        return treeMaker.MethodDef(treeMaker.Modifiers(PropertyUtils.GETTER_SETTER_FLAG),
                methodName, jcTypeExp,
                List.nil(), List.nil(), List.nil(), body, null);
    }
}
