package com.lyc.appinject.visitors;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Map;

/**
 * Created by Liu Yuchuan on 2020/1/12.
 */
public class InjectWriteClassVisitor extends ClassVisitor implements Opcodes {
    private final Map<String, String> serviceImpClasses;

    public InjectWriteClassVisitor(ClassVisitor cv, Map<String, String> serviceImpClasses) {
        super(ASM6, cv);
        this.serviceImpClasses = serviceImpClasses;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if ("initFields".equals(name)) {
            return new ModuleApiHoldersConstructorVisitor(super.visitMethod(access, name, desc, signature, exceptions), serviceImpClasses);
        }
        return super.visitMethod(access, name, desc, signature, exceptions);
    }
}
