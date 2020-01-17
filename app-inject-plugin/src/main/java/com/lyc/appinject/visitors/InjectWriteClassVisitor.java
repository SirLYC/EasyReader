package com.lyc.appinject.visitors;

import com.lyc.appinject.data.Impl;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.List;
import java.util.Map;

/**
 * Created by Liu Yuchuan on 2020/1/12.
 */
public class InjectWriteClassVisitor extends ClassVisitor implements Opcodes {
    private final Map<String, Impl> serviceImpClasses;
    private final Map<String, List<Impl>> extensionImpClasses;

    public InjectWriteClassVisitor(ClassVisitor cv, Map<String, Impl> serviceImpClasses, Map<String, List<Impl>> extensionImpClasses) {
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
