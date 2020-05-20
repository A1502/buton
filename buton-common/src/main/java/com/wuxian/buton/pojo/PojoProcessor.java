package com.wuxian.buton.pojo;

import com.wuxian.buton.core.ProcessorBase;
import com.wuxian.buton.core.ButonContext;
import com.wuxian.buton.util.AttributeUtils;
import com.wuxian.buton.util.JCTreeUtils;
import com.wuxian.buton.util.PropertySymbolInfo;
import com.wuxian.buton.util.PropertyUtils;
import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.List;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

@javax.annotation.processing.SupportedAnnotationTypes("com.wuxian.buton.pojo.Pojo")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class PojoProcessor extends ProcessorBase {

    //todo 1 【完成】支持mappingTo
    //todo 2 【完成】getter setter生成
    //todo 3 【已有方案,工作量不大】编译错误友好并明确显示，目前控制台有maven的乱码让使用者难以解决错误
    //todo 4 用idea build会报错，用maven compile正常。但是用GJ的版本不会idea build报错
    //todo 5 【完成】重构代码中的常量
    //todo 6 【莫名其妙好了】Pojo类含有内嵌类(不带泛型，case3)会编译失败，但是用GJ的版本不会
    //todo 7 【已有方案,有工作量】泛型方案
        /*
            用实现接口的类型
            用私有字段类型
         */
    //todo 8 【未开始】getter生成有以is打头的情况，包括测试序列化是否正常


    /*
       【注意事项1】要提取注解内容有两个方案：
            方案1：Symbol.getAnnotation(Class<?>)
            方案2：AttributeUtils.getDeclarationAttributes(Symbol symbol, Class<?> annotationType)

            实例：
            Pojo pojoAnnotation = pojoClassDecl.sym.getAnnotation(Pojo.class);这样写会崩溃
            因为pojoAnnotation.prototypeClass()会导致加载第三方类型定义而崩溃，只能用方案2在符号层面来处理逻辑

            结论：
            凡是注解上含有Class<?>类型都不能用方案1；方案2比方案1臃肿很多，要自己做检查逻辑，对重构是不友好的。
            在不引起崩溃的前提下用方案1

        【注意事项2】
            在同一个module里，Processor不能同时生效，他们是互斥的。比如lombok和buton不同时生效，他们互相看不见对方
            但是跨module是可以的。
            比如 moduleA(有用到buton) 依赖 moduleB（有用到lombok),那么moduleA里面的buton可以访问到moduleB里面lombok的作用结果

        【设计初衷】
        1.坚持对Prototype类型零侵入，绝对不在Prototype上依赖buton和其他的引用，一切逻辑都在Pojo类自身上完成
        2.报错要清晰明了，避免使用者对编译时错误感到困惑
        3.测试用例要覆盖到位

     */

    private class PojoAnnotationInfo {
        Symbol.ClassSymbol protoTypeClass;
        java.util.List<String> ignoreProperties = new ArrayList<>();
    }

    private class PojoPropertyMappingInfo {

        /**
         * 没有mapping的属性
         */
        java.util.List<PropertySymbolInfo> noMapping = new ArrayList<>();

        /**
         * 有效映射的属性,mappingTo作为key
         */
        Map<String, PropertySymbolInfo> validMapping = new HashMap<>();
    }

    private static final String SUPPORTED_ANNOTATION_TYPE_FIELD_VALUE = "value";
    private static final String POJO_FIELD_PROTOTYPE_CLASS = "protoTypeClass";
    private static final String POJO_FIELD_IGNORE_PROPERTIES = "ignoreProperties";
    private static final String POJO_REMARK_FIELD_PROTOTYPE_CLASS_NAME = "protoTypeClassName";
    private static final String POJO_REMARK_FIELD_IGNORE_PROPERTIES = "ignoreProperties";

    /**
     * 收敛处理全部Pojo相关异常
     */
    class ErrorManager {

        private static final String ERROR_POJO_PROTO_TYPE_CLASS_NOT_FOUND = "Pojo protoTypeClass is not found on \"{0}\"";
        private static final String ERROR_POJO_IGNORE_PROPERTIES_NOT_VALID = "Pojo ignoreProperties \"{0}\" on  \"{1}\"''s super classes is not valid";
        private static final String ERROR_POJO_PROPERTY_MAPPING_MAPPING_TO_IGNORE_PROPERTY_NOT_ALLOWED = "PojoPropertyMapping mapping to an ignore property \"{0}\" is not allowed on \"{1}\"";
        private static final String ERROR_SUPPORTED_ANNOTATION_TYPE_VALUE_NOT_FOUND = "SupportedAnnotationTypes value is not found on \"{0}\"";

        private static final String TYPE = "type:";
        private static final String FIELD = "field:";

        private ButonContext butonContext;

        public ErrorManager(ButonContext butonContext) {
            this.butonContext = butonContext;
        }

        void raiseSupportedAnnotationTypeValueNotFound(Symbol.ClassSymbol classSymbol) {
            String message = MessageFormat.format(ERROR_SUPPORTED_ANNOTATION_TYPE_VALUE_NOT_FOUND
                    , TYPE + classSymbol.fullname.toString());
            PojoProcessor.this.stopWithException(butonContext, message);
        }

        void raiseSupportedAnnotationTypeValueNotFound(Symbol fieldSymbol) {
            String message = MessageFormat.format(ERROR_SUPPORTED_ANNOTATION_TYPE_VALUE_NOT_FOUND,
                    FIELD + fieldSymbol.owner.name.toString() + "." + fieldSymbol.name.toString());
            PojoProcessor.this.stopWithException(butonContext, message);
        }

        void raisePojoProtoTypeClassNameNotFound(Symbol.ClassSymbol classSymbol) {
            String message = MessageFormat.format(ERROR_POJO_PROTO_TYPE_CLASS_NOT_FOUND
                    , TYPE + classSymbol.fullname.toString());
            PojoProcessor.this.stopWithException(butonContext, message);
        }

        void raisePojoIgnorePropertiesNotValid(String invalidProperties, Symbol.ClassSymbol classSymbol) {
            String message = MessageFormat.format(ERROR_POJO_IGNORE_PROPERTIES_NOT_VALID, invalidProperties
                    , TYPE + classSymbol.fullname.toString());
            PojoProcessor.this.stopWithException(butonContext, message);
        }

        void raisePojoPropertyMappingMappingToIgnorePropertyNotAllowed(String propertyName, Symbol fieldSymbol) {
            String message = MessageFormat.format(ERROR_POJO_PROPERTY_MAPPING_MAPPING_TO_IGNORE_PROPERTY_NOT_ALLOWED
                    , propertyName
                    , FIELD + fieldSymbol.owner.name.toString() + "." + fieldSymbol.name.toString());
            PojoProcessor.this.stopWithException(butonContext, message);
        }
    }

    @Override
    protected JCTree.Visitor createVisitor(ButonContext butonContext, RoundEnvironment roundEnv) {

        ErrorManager errorManager = new ErrorManager(butonContext);

        JCTree.Visitor result = new TreeTranslator() {
            @Override
            public void visitClassDef(JCTree.JCClassDecl pojoClassDecl) {

                //为什么这样写请查看【注意事项1】
                Attribute.Compound pojoAnnotation = AttributeUtils.getDeclarationAttributes(pojoClassDecl.sym, Pojo.class);
                if (pojoAnnotation != null) {

                    //提取PojoAnnotation内容
                    PojoAnnotationInfo pojoAnnotationInfo = getPojoAnnotationInfo(pojoAnnotation, pojoClassDecl.sym, errorManager);

                    //按ignore策略递归祖先提取prototype所有属性
                    java.util.List<PropertySymbolInfo> prototypeClassPropertySymbolInfosUnderIgnore
                            = getPropertySymbolInfosUnderIgnore(pojoAnnotationInfo.protoTypeClass
                            , pojoAnnotationInfo.ignoreProperties
                            , errorManager);

                    java.util.List<String> prototypePropertyNamesUnderIgnore = prototypeClassPropertySymbolInfosUnderIgnore.stream()
                            .map(o -> o.getName()).collect(Collectors.toList());

                    //获取当前类的属性Mapping信息
                    PojoPropertyMappingInfo pojoPropertyMappingInfo = getPojoPropertyMappingInfo(pojoClassDecl.sym
                            , pojoAnnotationInfo.ignoreProperties
                            , prototypePropertyNamesUnderIgnore
                            , errorManager);

                    //获取类级的注解过滤范围
                    List<Symbol.TypeSymbol> classAnnotationFilters
                            = getSupportedAnnotationTypeAnnotationFromClass(pojoClassDecl.sym, errorManager);

                    //调整类上的注解：按过滤从prototype类上复制，并追加PojoRemark
                    adjustPojoAnnotation(
                            butonContext
                            , pojoClassDecl
                            , pojoAnnotationInfo.protoTypeClass
                            , pojoAnnotationInfo.ignoreProperties
                            , classAnnotationFilters
                    );

                    //计算prototype克隆的字段
                    java.util.List<JCTree.JCVariableDecl> prototypeFields = clonePrototypeFields(
                            butonContext.getTreeMaker()
                            , prototypeClassPropertySymbolInfosUnderIgnore
                            , classAnnotationFilters
                            , pojoPropertyMappingInfo.validMapping.keySet()
                    );

                    //添加从prototype中提取的field,getter,setter
                    appendPropertiesOfPrototype(butonContext, prototypeFields, pojoClassDecl);

                    //为Pojo类的mapping字段填充注解
                    processMappingAnnotation(butonContext.getTreeMaker()
                            , pojoClassDecl
                            , pojoPropertyMappingInfo.validMapping
                            , prototypeClassPropertySymbolInfosUnderIgnore
                            , classAnnotationFilters
                            , errorManager
                    );

                    //为Pojo类的PojoProperty和PojoPropertyMapping字段全部配套getter&setter
                    appendGetterSettersOfPojo(butonContext, pojoClassDecl, pojoPropertyMappingInfo);
                }
                super.visitClassDef(pojoClassDecl);
            }
        };
        return result;
    }

    private PojoAnnotationInfo getPojoAnnotationInfo(Attribute.Compound pojoAnnotation
            , Symbol.ClassSymbol target
            , ErrorManager errorManager) {

        PojoAnnotationInfo result = new PojoAnnotationInfo();

        //提取protoTypeClass字段值
        Attribute protoTypeClassAttribute = AttributeUtils.getAttribute(pojoAnnotation, POJO_FIELD_PROTOTYPE_CLASS);
        if (protoTypeClassAttribute == null) {
            errorManager.raisePojoProtoTypeClassNameNotFound(target);
        } else {
            result.protoTypeClass = (Symbol.ClassSymbol) ((Type.ClassType) protoTypeClassAttribute.getValue()).tsym;
        }

        //提取ignoreProperties字段值

        Attribute ignorePropertiesAttribute = AttributeUtils.getAttribute(pojoAnnotation, POJO_FIELD_IGNORE_PROPERTIES);
        if (ignorePropertiesAttribute != null) {
            for (Attribute.Constant constant : (List<Attribute.Constant>) ignorePropertiesAttribute.getValue()) {
                result.ignoreProperties.add(constant.getValue().toString());
            }
        }
        return result;
    }

    private void appendPropertiesOfPrototype(ButonContext butonContext
            , java.util.List<JCTree.JCVariableDecl> prototypeFields
            , JCTree.JCClassDecl pojoClassDecl) {
        prototypeFields.forEach(jcVariableDecl -> {
                    //prototype中的属性字段全部追加给Pojo类
                    pojoClassDecl.defs =
                            pojoClassDecl.defs.append(jcVariableDecl);
                    //prototype中的属性字段全部配套getter&setter
                    pojoClassDecl.defs =
                            pojoClassDecl.defs.append(JCTreeUtils.createGetter(butonContext, jcVariableDecl));
                    pojoClassDecl.defs =
                            pojoClassDecl.defs.append(JCTreeUtils.createSetter(butonContext, jcVariableDecl));
                }
        );
    }

    private void appendGetterSettersOfPojo(ButonContext butonContext
            , JCTree.JCClassDecl pojoClassDecl
            , PojoPropertyMappingInfo propertyMappingInfo) {

        java.util.List<PropertySymbolInfo> selected = new ArrayList<>();

        //对于noMapping的字段，有PojoProperty注解，纳入范围
        propertyMappingInfo.noMapping.stream().forEach(o -> {
            if (o.getField() != null) {
                //为什么这样写请查看【注意事项1】
                PojoProperty pojoProperty = o.getField().getAnnotation(PojoProperty.class);
                if (pojoProperty != null) {
                    selected.add(o);
                }
            }
        });

        //对于全部mapping字段，都纳入范围
        selected.addAll(propertyMappingInfo.validMapping.values());

        //纳入范围的补全getter setter
        List<JCTree> newGetterSetters = List.nil();

        for (PropertySymbolInfo item : selected) {
            //前提是没有同名getter冲突
            if (item.getGetter().size() == 0) {
                JCTree getter = JCTreeUtils.createGetter(butonContext, item.getName(), item.getType());
                newGetterSetters = newGetterSetters.append(getter);
            }
            //前提是没有同名setter的冲突
            if (item.getSetter().size() == 0) {
                JCTree setter = JCTreeUtils.createSetter(butonContext, item.getName(), item.getType());
                newGetterSetters = newGetterSetters.append(setter);
            }
        }
        pojoClassDecl.defs = pojoClassDecl.defs.appendList(newGetterSetters);
    }

    private void adjustPojoAnnotation(ButonContext butonContext
            , JCTree.JCClassDecl pojo
            , Symbol.ClassSymbol prototypeClass
            , Collection<String> ignoreProperties
            , List<Symbol.TypeSymbol> classAnnotationFilters) {


        Attribute.Compound pojoRemark = AttributeUtils.create(butonContext, PojoRemark.class);

        pojoRemark = AttributeUtils.appendConstantAttribute(pojoRemark
                , POJO_REMARK_FIELD_PROTOTYPE_CLASS_NAME
                , prototypeClass.fullname.toString());

        pojoRemark = AttributeUtils.appendConstantAttribute(pojoRemark
                , POJO_REMARK_FIELD_IGNORE_PROPERTIES
                , ignoreProperties);

        //首先按过滤要求提取类级注解
        List<Attribute.Compound> compoundList =
                AttributeUtils.getDeclarationAttributes(prototypeClass
                        , classAnnotationFilters != null ? classAnnotationFilters : List.nil());

        //然后追加PojoRemark注解到类上
        compoundList = compoundList.append(pojoRemark);
        List<JCTree.JCAnnotation> resultAnnotations = butonContext.getTreeMaker().Annotations(compoundList);

        pojo.getModifiers().annotations = pojo.getModifiers().annotations.appendList(resultAnnotations);

    }

    /**
     * 采取就近原则,为Pojo类的mapping字段填充注解
     *
     * @param treeMaker                                    jcTree生成器
     * @param pojoClassDecl                                pojo类声明
     * @param mappingPropertiesOfPojoClass                 带mapping的字段
     * @param prototypeClassPropertySymbolInfosUnderIgnore 已忽略过滤的prototype属性信息
     * @param classAnnotationFilters                       类级注解过滤范围
     * @param errorManager                                 错误管理器
     */
    private void processMappingAnnotation(TreeMaker treeMaker
            , JCTree.JCClassDecl pojoClassDecl
            , Map<String, PropertySymbolInfo> mappingPropertiesOfPojoClass
            , java.util.List<PropertySymbolInfo> prototypeClassPropertySymbolInfosUnderIgnore
            , List<Symbol.TypeSymbol> classAnnotationFilters
            , ErrorManager errorManager) {

        Map<String, PropertySymbolInfo> prototypeClassPropertySymbolMap = new HashMap<>();
        for (PropertySymbolInfo propertySymbolInfo : prototypeClassPropertySymbolInfosUnderIgnore) {
            prototypeClassPropertySymbolMap.put(propertySymbolInfo.getName(), propertySymbolInfo);
        }

        for (String mappingTo : mappingPropertiesOfPojoClass.keySet()) {
            Symbol.VarSymbol pojoFieldSymbol = mappingPropertiesOfPojoClass.get(mappingTo).getField();
            Symbol.VarSymbol mappingToFieldSymbol = prototypeClassPropertySymbolMap.get(mappingTo).getField();

            List<JCTree.JCAnnotation> resultAnnotations;
            //按就近原则，优先字段上的filter生效，然后是类上的生效，如果都没有，按0个元素来过滤，也是就是完全不同步注解
            List<Symbol.TypeSymbol> actualFilter =
                    getSupportedAnnotationTypeAnnotationWithFromSymbol(pojoFieldSymbol, errorManager);
            if (actualFilter == null) {
                if (classAnnotationFilters != null) {
                    actualFilter = classAnnotationFilters;
                } else {
                    actualFilter = List.nil();
                }
            }

            List<Attribute.Compound> compoundList = AttributeUtils.getDeclarationAttributes(mappingToFieldSymbol, actualFilter);
            resultAnnotations = treeMaker.Annotations(compoundList);

            JCTree.JCVariableDecl fieldDecl = JCTreeUtils.getField(pojoClassDecl, pojoFieldSymbol.name.toString());

            fieldDecl.mods.annotations = fieldDecl.mods.annotations.appendList(resultAnnotations);
        }
    }

    /**
     * 从prototype中克隆满足要求的字段
     *
     * @param parentPropertySymbolInfosUnderIgnore prototype中所有属性（被忽略的除外）
     * @param classAnnotationFilters               需要克隆的注解
     * @param mappingToProperties                  被当前类已经映射过的属性名
     * @return 克隆的字段集合
     */
    private java.util.List<JCTree.JCVariableDecl> clonePrototypeFields(TreeMaker treeMaker
            , java.util.List<PropertySymbolInfo> parentPropertySymbolInfosUnderIgnore
            , List<Symbol.TypeSymbol> classAnnotationFilters
            , Set<String> mappingToProperties) {

        //已经被mapping的不能再采用，故排除掉。actualRange才是要处理的范围
        java.util.List<PropertySymbolInfo> actualRange = parentPropertySymbolInfosUnderIgnore.stream()
                .filter(o -> !mappingToProperties.contains(o.getName())).collect(Collectors.toList());

        java.util.List<JCTree.JCVariableDecl> result = new ArrayList<>();
        List<Symbol.TypeSymbol> actualFilters = classAnnotationFilters != null ? classAnnotationFilters : List.nil();

        for (PropertySymbolInfo propertySymbolInfo : actualRange) {

            //这一段是要解决复制字段符号时，要提取上面的注解的问题
            Symbol.VarSymbol fieldSymbol = propertySymbolInfo.getField();

            //首先是无条件追加PrototypeProperty注解到字段上
            Attribute.Compound prototypeProperty = AttributeUtils.create(butonContext, PrototypeProperty.class);
            List<Attribute.Compound> compoundList = List.nil();
            compoundList = compoundList.append(prototypeProperty);

            List<JCTree.JCAnnotation> resultAnnotations;
            if (fieldSymbol != null) {
                //按类级注解范围要求过滤
                compoundList = compoundList.appendList(
                        AttributeUtils.getDeclarationAttributes(fieldSymbol, actualFilters));
            }
            resultAnnotations = treeMaker.Annotations(compoundList);

            //准备好注解，开始创建一个字段符号
            JCTree.JCVariableDecl resultItem = treeMaker.VarDef(
                    treeMaker.Modifiers((fieldSymbol != null ? fieldSymbol.flags() : Modifier.PRIVATE), resultAnnotations)
                    , butonContext.getNames().fromString(propertySymbolInfo.getName())
                    , treeMaker.Type(propertySymbolInfo.getType())
                    , null);
            result.add(resultItem);
        }
        return result;
    }

    private PojoPropertyMappingInfo getPojoPropertyMappingInfo(Symbol.ClassSymbol pojo
            , java.util.List<String> ignorePropertiesOfPrototypeClass
            , Collection<String> validPropertiesOfPrototypeClass
            , ErrorManager errorManager) {
        PojoPropertyMappingInfo result = new PojoPropertyMappingInfo();
        java.util.List<PropertySymbolInfo> propertySymbolInfos
                = PropertyUtils.getRecursivePropertySymbolInfo(pojo, PropertyUtils.PropertyLevel.FIELD_AT_LEAST);

        //依次提取PojoPropertyMapping
        for (PropertySymbolInfo property : propertySymbolInfos) {
            if (property.getField() != null) {

                //为什么这样写请查看【注意事项1】
                PojoPropertyMapping pojoPropertyMapping = property.getField().getAnnotation(PojoPropertyMapping.class);

                //没有@PojoPropertyMapping的
                if (pojoPropertyMapping == null) {
                    result.noMapping.add(property);
                } else {
                    //有@PojoPropertyMapping的
                    String mappingTo = pojoPropertyMapping.mappingTo();
                    //不可以映射到忽略的属性
                    if (ignorePropertiesOfPrototypeClass.contains(mappingTo)) {
                        errorManager.raisePojoPropertyMappingMappingToIgnorePropertyNotAllowed(mappingTo, property.getField());
                    } else {
                        //添加到
                        if (validPropertiesOfPrototypeClass.contains(mappingTo)) {
                            result.validMapping.put(mappingTo, property);
                        } else {
                            result.noMapping.add(property);
                        }
                    }
                }
            }
        }
        return result;
    }

    private java.util.List<PropertySymbolInfo> getPropertySymbolInfosUnderIgnore(Symbol.ClassSymbol prototype
            , java.util.List<String> ignoreProperties
            , ErrorManager errorManager) {
        java.util.List<PropertySymbolInfo> result = new ArrayList<>();

        //递归提取类属性
        java.util.List<PropertySymbolInfo> symbolInfos
                = PropertyUtils.getRecursivePropertySymbolInfo(prototype, PropertyUtils.PropertyLevel.GETTER_SETTER_BOTH);

        //收集非法的属性名:类中并不存在的属性名
        java.util.List<String> validProperties
                = symbolInfos.stream().map(PropertySymbolInfo::getName).collect(Collectors.toList());

        java.util.List<String> invalidProperties = new ArrayList<>();
        ignoreProperties.stream().forEach(item -> {
            if (!validProperties.contains(item)) {
                invalidProperties.add(item);
            }
        });

        //收集后将非法的属性名抛出异常
        if (invalidProperties.size() > 0) {
            String invalidPropertiesString = String.join(","
                    , invalidProperties.toArray(new String[invalidProperties.size()]));
            errorManager.raisePojoIgnorePropertiesNotValid(invalidPropertiesString, prototype);
        }

        //按ignoreProperties的范围裁剪父类属性
        symbolInfos.forEach(item -> {
            if (!ignoreProperties.contains(item.getName())) {
                result.add(item);
            }
        });
        return result;
    }

    /**
     * 从类符号中获取
     */
    private List<Symbol.TypeSymbol> getSupportedAnnotationTypeAnnotationFromClass(Symbol.ClassSymbol pojo
            , ErrorManager errorManager) {
        List<Symbol.TypeSymbol> result = getSupportedAnnotationTypeAnnotationImpl(pojo,
                () -> errorManager.raiseSupportedAnnotationTypeValueNotFound(pojo));
        return result;
    }

    /**
     * 从字段符号中获取
     */
    private List<Symbol.TypeSymbol> getSupportedAnnotationTypeAnnotationWithFromSymbol(Symbol.VarSymbol pojoField, ErrorManager errorManager) {
        List<Symbol.TypeSymbol> result = getSupportedAnnotationTypeAnnotationImpl(pojoField,
                () -> errorManager.raiseSupportedAnnotationTypeValueNotFound(pojoField));
        return result;
    }

    /**
     * 从符号上提取SupportedAnnotationType注解的value
     *
     * @param symbol                                   符号
     * @param whenSupportedAnnotationTypeValueNotFound 若value不存在则执行这个拉曼达
     * @return SupportedAnnotationType注解的value，如果不存在该注解，返回null，否则返回一个集合(元素可能为0个）
     */
    private List<Symbol.TypeSymbol> getSupportedAnnotationTypeAnnotationImpl(Symbol symbol, Runnable whenSupportedAnnotationTypeValueNotFound) {
        List<Symbol.TypeSymbol> result = List.nil();

        //获取类级定义的SupportedAnnotationTypes注解
        Attribute.Compound supportedAnnotationTypeAnnotationWithClass = AttributeUtils.getDeclarationAttributes(symbol, SupportedAnnotationTypes.class);
        if (supportedAnnotationTypeAnnotationWithClass != null) {
            Attribute supportedAnnotationTypesAttribute = AttributeUtils.getAttribute(supportedAnnotationTypeAnnotationWithClass, SUPPORTED_ANNOTATION_TYPE_FIELD_VALUE);
            //supportedAnnotationTypesAttribute转为List<Symbol.TypeSymbol>用于后续做过滤条件

            if (supportedAnnotationTypesAttribute != null) {
                for (Object annotationTypes : (List) supportedAnnotationTypesAttribute.getValue()) {
                    Attribute.Class attrClz = (Attribute.Class) annotationTypes;
                    if (attrClz.classType.tsym.getKind() == ElementKind.ANNOTATION_TYPE) {
                        result = result.append(attrClz.classType.tsym);
                    }
                }
            } else {
                whenSupportedAnnotationTypeValueNotFound.run();
            }
            return result;
        } else {
            return null;
        }
    }
}