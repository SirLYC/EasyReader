package com.lyc.appinject.visitors;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.List;
import java.util.Map;

/**
 * Created by Liu Yuchuan on 2020/1/12.
 */
public class InjectWriteClassVisitor extends ClassVisitor implements Opcodes {
    private final Map<String, String> serviceImpClasses;
    private final Map<String, List<String>> extensionImpClasses;

    public InjectWriteClassVisitor(ClassVisitor cv, Map<String, String> serviceImpClasses, Map<String, List<String>> extensionImpClasses) {
        super(ASM6, cv);
        this.serviceImpClasses = serviceImpClasses;
        this.extensionImpClasses = extensionImpClasses;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if ("initServices".equals(name)) {
            return new InitServicesMethodVisitor(super.visitMethod(access, name, desc, signature, exceptions), serviceImpClasses);
        } else if ("initExtensions".equals(name)) {
            return new InitExtensionsMethodVisitor(super.visitMethod(access, name, desc, signature, exceptions), extensionImpClasses, access, name, desc);
        }
        return super.visitMethod(access, name, desc, signature, exceptions);
    }
}
