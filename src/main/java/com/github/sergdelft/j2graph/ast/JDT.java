package com.github.sergdelft.j2graph.ast;

import com.github.sergdelft.j2graph.graph.ClassGraph;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.util.Map;

public class JDT {

    public ClassGraph parse(String sourceCode) {
        ASTParser parser = ASTParser.newParser(AST.JLS11);

        parser.setResolveBindings(true);
        parser.setBindingsRecovery(true);

        Map<String, String> options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_11, options);
        parser.setSource(sourceCode.toCharArray());
        CompilationUnit cu = (CompilationUnit) parser.createAST(null);

        JDTVisitor visitor = new JDTVisitor();
        cu.accept(visitor);

        //JDTDebuggingVisitor d = new JDTDebuggingVisitor();
        //cu.accept(d);

        return visitor.buildClassGraph();
    }
}
