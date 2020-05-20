package com.wuxian.buton.core;

import com.sun.tools.javac.tree.JCTree;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;

public abstract class ProcessorBase<V extends JCTree.Visitor> extends AbstractProcessor {

    protected ButonContext butonContext;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        butonContext = new ButonContext(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement element : annotations) {
            processTypeElement(element, roundEnv);
        }
        return true;
    }

    private void processTypeElement(TypeElement annotation, RoundEnvironment roundEnv) {
        Set<? extends Element> set = roundEnv.getElementsAnnotatedWith(annotation);
        for (Element element : set) {
            JCTree jcTree = butonContext.getTrees().getTree(element);
            jcTree.accept(createVisitor(butonContext, roundEnv));
        }
    }

    protected void stopWithException(ButonContext butonContext, String message) {
        butonContext.getMessager().printMessage(Diagnostic.Kind.ERROR, message);
        throw new ButonException(message);
    }

    protected abstract V createVisitor(ButonContext butonContext, RoundEnvironment roundEnv);
}
