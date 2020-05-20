package com.wuxian.buton.core;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.model.JavacTypes;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;

public class ButonContext {

    private JavacTrees trees;
    private Names names;
    private TreeMaker treeMaker;
    private Messager messager;
    private JavacTypes javacTypes;
    private JavacElements javacElements;

    private JavacProcessingEnvironment javacProcessingEnvironment;

    public JavacTrees getTrees() {
        return trees;
    }

    public Names getNames() {
        return names;
    }

    public TreeMaker getTreeMaker() {
        return treeMaker;
    }

    public Messager getMessager() {
        return messager;
    }

    public JavacElements getJavacElements() {
        return javacElements;
    }

    public JavacTypes getJavacTypes() {
        return javacTypes;
    }

    public JavacProcessingEnvironment getJavacProcessingEnvironment() {
        return javacProcessingEnvironment;
    }

    public ButonContext(ProcessingEnvironment processingEnv) {

        this.trees = JavacTrees.instance(processingEnv);

        this.javacProcessingEnvironment = (JavacProcessingEnvironment) processingEnv;

        Context context = javacProcessingEnvironment.getContext();

        this.javacElements = javacProcessingEnvironment.getElementUtils();

        this.javacTypes = javacProcessingEnvironment.getTypeUtils();

        this.names = Names.instance(context);

        this.treeMaker = TreeMaker.instance(context);

        this.messager = this.getJavacProcessingEnvironment().getMessager();
    }
}
