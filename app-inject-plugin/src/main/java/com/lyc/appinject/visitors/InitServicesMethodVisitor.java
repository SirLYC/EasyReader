package com.lyc.appinject.visitors;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.Map;

/**
 * Created by Liu Yuchuan on 2020/1/12.
 */
class InitServicesMethodVisitor extends MethodVisitor implements Opcodes {
    private final Map<String, String> serviceImpClasses;

    InitServicesMethodVisitor(MethodVisitor mv, Map<String, String> serviceImpClasses) {
        super(ASM6, mv);
        this.serviceImpClasses = serviceImpClasses;
    }

    @Override
    public void visitCode() {
        serviceImpClasses.forEach((superClass, curClass) -> {
            System.out.println("----------------- Put service: " + superClass + " -> " + curClass);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, "com/lyc/appinject/ModuleApiHolders", "serviceClassMap", "Ljava/util/Map;");
            mv.visitLdcInsn(Type.getType("L" + superClass + ";"));
            mv.visitLdcInsn(Type.getType("L" + curClass + ";"));
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true);
            mv.visitInsn(POP);
        });
        super.visitCode();
    }
}
