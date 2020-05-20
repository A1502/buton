/*
          List<JCVariableDecl> jcVariableDeclList = List.nil();

          List<JCVariableDecl> copyFromJcVariableDeclList = List.nil();

          TreeMaker treeMaker = butonContext.getTreeMaker();

          List<JCTree.JCAnnotation> annotations = jcClassDecl.getModifiers().getAnnotations();
          for (JCTree.JCAnnotation annotation : annotations) {
              if (Pojo.class.getCanonicalName().equals(annotation.type.toString())) {
                  //获取到pojo注解上的copy的类
                  JCTree.JCAssign jcAssign = (JCTree.JCAssign) annotation.getArguments().get(0);
                  JCTree.JCFieldAccess jcFieldAccess = (JCTree.JCFieldAccess) jcAssign.rhs;
                  //获取到pojo注解上的支持copy的注解
                  JCTree.JCAssign jcAssign1 = (JCTree.JCAssign) annotation.getArguments().get(2);
                  List<JCTree.JCExpression> supportedAnnotationTypes = ((JCTree.JCNewArray) jcAssign1.rhs).elems;

                  List<JCTree.JCFieldAccess> annotationTypeList = List.nil();
                  for (JCTree.JCExpression supportedAnnotationType : supportedAnnotationTypes) {
                      JCTree.JCFieldAccess jcFieldAccess1 = (JCTree.JCFieldAccess) supportedAnnotationType;
                      if ((jcFieldAccess1).sym.enclClass().getKind() == ElementKind.ANNOTATION_TYPE) {
                          annotationTypeList = annotationTypeList.append(jcFieldAccess1);
                      }
                  }

                  //递归获取父类所有的字段
                  java.util.List<Symbol> symbolList = ClassSymbolUtils.getSymbolList(jcFieldAccess.sym.enclClass());
                  for (Symbol symbol : symbolList) {

                      //非字段不处理
                      if (!ElementKind.FIELD.equals(symbol.getKind())) {
                          continue;
                      }
                      //FINAL和STATIC的字段不处理
                      if (symbol.getModifiers().contains(Modifier.FINAL)
                              || symbol.getModifiers().contains(Modifier.STATIC)) {
                          continue;
                      }

                      if (symbol.getAnnotation(CopyIgnore.class) == null) {

                          if ("FIELD".equals(symbol.getKind().toString()) && !(symbol.getModifiers().toString().toUpperCase().contains("FINAL"))
                                  && !(symbol.getModifiers().toString().toUpperCase().contains("STATIC"))) {
                              //原字段(保留一切)
                              // JCVariableDecl jcVariableDecl = treeMaker.VarDef((Symbol.VarSymbol) symbol, null);
                              //筛选注解后的字段
                              List<Attribute.Compound> compoundList = selectAnnotation(symbol, annotationTypeList);
                              List<JCTree.JCAnnotation> annotations1 = treeMaker.Annotations(compoundList);
                              JCVariableDecl jcVariableDecl = treeMaker.VarDef(treeMaker.Modifiers(symbol.flags(), annotations1), symbol.baseField1, treeMaker.Type(symbol.type), null);
                              //外键转换后的字段
                              Fk fk = symbol.getAnnotation(Fk.class);
                              if (fk != null) {
                                  TypeTag typeTag = fk.value();
                                  String fkId = fk.fkId();
                                  //生成自定义注解FkFrom
                                  JCTree.JCAnnotation fkFrom = treeMaker.Annotation(memberAccess("com.wuxian.opensource.buton.annotation.FkFrom"),
                                          List.of(treeMaker.Assign(memberAccess("from"), treeMaker.Literal(symbol.baseField1.toString()))));

                                  annotations1 = annotations1.append(fkFrom);

                                  JCVariableDecl jcVariableDec2 =
                                          treeMaker.VarDef(treeMaker.Modifiers(symbol.flags(), annotations1),
                                                  butonContext.getNames().fromString(fkId),
                                                  treeMaker.TypeIdent(typeTag), null);
                                  treeMaker.at(jcVariableDec2.pos);
                                  copyFromJcVariableDeclList = copyFromJcVariableDeclList.append(jcVariableDec2);
                                  continue;
                              }
                              copyFromJcVariableDeclList = copyFromJcVariableDeclList.append(jcVariableDecl);

                          }
                      }
                  }
              }
          }
          //添加复制的到jcVariableDeclList
          jcVariableDeclList = jcVariableDeclList.appendList(copyFromJcVariableDeclList);

          //添加本身的到jcVariableDeclList
          for (JCTree tree : jcClassDecl.defs) {
              if (tree.getKind().equals(Tree.Kind.VARIABLE)) {
                  JCVariableDecl jcVariableDecl =
                          (JCVariableDecl) tree;
                  jcVariableDeclList =
                          jcVariableDeclList.append(jcVariableDecl);
              }
          }

          //复制的全部加入到jcClassDecl.defs
          copyFromJcVariableDeclList.forEach(jcVariableDecl -> {
              note(jcVariableDecl.getName() + " has been copied");
              jcClassDecl.defs =
                      jcClassDecl.defs.append(jcVariableDecl);
          });

          //jcVariableDeclList的getter和setter全部加入到jcClassDecl.defs
          jcVariableDeclList.forEach(jcVariableDecl -> {
              note(jcVariableDecl.getName() + " has been processed");
              jcClassDecl.defs =
                      jcClassDecl.defs.append(makeGetterMethodDecl(jcVariableDecl));
              treeMaker.at(jcClassDecl.pos);
              jcClassDecl.defs =
                      jcClassDecl.defs.append(makeSetterMethodDecl(jcVariableDecl));
          });
          note("<<=== generated code ===>>: " + jcClassDecl.toString());
          super.visitClassDef(jcClassDecl);
      }
  };
  return result;
}

*/
/*    public static ExpressionTree createAnnotationArgument(TreeMaker make, String argumentName,
                                                          Object argumentValue) {
        ExpressionTree argumentValueTree = make.Literal(argumentValue);
        return make.Assignment(make.Identifier(argumentName), argumentValueTree);
    }


        private Name getNameFromString (String s){
            return butonContext.getNames().fromString(s);
        }

        private JCTree.JCExpression memberAccess (String components){
            String[] componentArray = components.split("\\.");
            JCTree.JCExpression expr = butonContext.getTreeMaker().Ident(getNameFromString(componentArray[0]));
            for (int i = 1; i < componentArray.length; i++) {
                expr = butonContext.getTreeMaker().Select(expr, getNameFromString(componentArray[i]));
            }
            return expr;
        }


        private void note (String msg){

            butonContext.getJavacProcessingEnvironment().getMessager().printMessage(Diagnostic.Kind.NOTE, msg);
        }


        private JCTree.JCMethodDecl makeGetterMethodDecl (JCVariableDecl jcVariableDecl){

            TreeMaker treeMaker = butonContext.getTreeMaker();

            ListBuffer<JCTree.JCStatement> statements = new ListBuffer<>();
            statements.append(treeMaker.Return(treeMaker.Select(treeMaker.Ident(butonContext.getNames().fromString("this")),
                    jcVariableDecl.getName())));
            JCTree.JCBlock body = treeMaker.Block(0, statements.toList());
            return treeMaker.MethodDef(treeMaker.Modifiers(Flags.PUBLIC),
                    getNewMethodName(jcVariableDecl.getName()), jcVariableDecl.vartype,
                    List.nil(), List.nil(), List.nil(), body, null);
        }


        private JCTree.JCMethodDecl makeSetterMethodDecl (JCVariableDecl jcVariableDecl){
            try {

                TreeMaker treeMaker = butonContext.getTreeMaker();

                //方法的访问级别
                JCTree.JCModifiers modifiers = treeMaker.Modifiers(Flags.PUBLIC);
                //定义方法名
                Name methodName = setMethodName(jcVariableDecl.getName());
                //定义返回值类型
                JCTree.JCExpression returnMethodType = treeMaker.Type(new com.sun.tools.javac.code.Type.JCVoidType());
                ListBuffer<JCTree.JCStatement> statements = new ListBuffer<>();
                statements.append(treeMaker.Exec(treeMaker.Assign(treeMaker.Select(treeMaker.Ident(butonContext.getNames().fromString("this")), jcVariableDecl.getName()), treeMaker.Ident(jcVariableDecl.getName()))));
                //定义方法体
                JCTree.JCBlock methodBody = treeMaker.Block(0, statements.toList());
                List<JCTree.JCTypeParameter> methodGenericParams = List.nil();
                //定义入参
                JCVariableDecl param = treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER, List.nil()), jcVariableDecl.baseField1, jcVariableDecl.vartype, null);
                //设置入参
                List<JCVariableDecl> parameters = List.of(param);
                List<JCTree.JCExpression> throwsClauses = List.nil();
                //构建新方法
                return treeMaker.MethodDef(modifiers, methodName, returnMethodType, methodGenericParams, parameters, throwsClauses, methodBody, null);

            } catch (Exception e) {
                note(e.getMessage());
            }
            return null;
        }


        private Name getNewMethodName (Name baseField1){
            String s = baseField1.toString();
            return butonContext.getNames().fromString("get" + s.substring(0, 1).toUpperCase() + s.substring(1, baseField1.length()));
        }


        private Name setMethodName (Name baseField1){
            String s = baseField1.toString();
            return butonContext.getNames().fromString("set" + s.substring(0, 1).toUpperCase() + s.substring(1, baseField1.length()));
        }


        private List<Attribute.Compound> selectAnnotation (Symbol
        symbol, List < JCTree.JCFieldAccess > annotationTypeList){
            List<Attribute.Compound> declarationAttributes = List.nil();
            List<Attribute.Compound> declarationAttributes1 = symbol.getDeclarationAttributes();
            for (Attribute.Compound compound : declarationAttributes1) {
                for (JCTree.JCFieldAccess annotationType : annotationTypeList) {
                    if (compound.type.tsym.enclClass().fullname.toString().equals(annotationType.sym.enclClass().fullname.toString())) {
                        declarationAttributes = declarationAttributes.append(compound);
                    }
                }
            }
            return declarationAttributes;
        }

    }
    */