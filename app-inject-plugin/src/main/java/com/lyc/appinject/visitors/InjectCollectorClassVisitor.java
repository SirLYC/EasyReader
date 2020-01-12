package com.lyc.appinject.visitors;


import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Map;
import java.util.Set;

/**
 * Created by Liu Yuchuan on 2020/1/12.
 */
public class InjectCollectorClassVisitor extends ClassVisitor implements Opcodes {
    private static final String SERVICE_DESC = "Lcom/lyc/appinject/annotations/Service;";
    private static final String SERVICE_IMPL_DESC = "Lcom/lyc/appinject/annotations/ServiceImp;";
    private static final String SERVICE_IMPL_SERVICE_PARAM = "service";
    private final Set<String> serviceClasses;
    private final Map<String, String> serviceImpClasses;
    private String currentName;
    private String[] currentInterfaces;

    public InjectCollectorClassVisitor(Set<String> serviceClasses, Map<String, String> serviceImpClasses) {
        super(ASM6);
        this.serviceClasses = serviceClasses;
        this.serviceImpClasses = serviceImpClasses;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        currentName = name;
        currentInterfaces = interfaces;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (SERVICE_DESC.equals(desc)) {
            serviceClasses.add(currentName);
            System.out.println("Find a @Service: " + currentName);
        } else if (SERVICE_IMPL_DESC.equals(desc)) {
            return new ServiceImpAnnotationVisitor();
        }
        return super.visitAnnotation(desc, visible);
    }

    public Set<String> getServiceClasses() {
        return serviceClasses;
    }

    public Map<String, String> getServiceImpClasses() {
        return serviceImpClasses;
    }

    private class ServiceImpAnnotationVisitor extends AnnotationVisitor {

        ServiceImpAnnotationVisitor() {
            super(Opcodes.ASM6);
        }

        @Override
        public void visit(String name, Object value) {
            if (SERVICE_IMPL_SERVICE_PARAM.equals(name)) {
                String desc = String.valueOf(value);
                if (desc.length() > 2) {
                    // remove "L" and ";" from method desc
                    String superName = desc.substring(1, desc.length() - 1);

                    if (serviceImpClasses.containsKey(superName)) {
                        throw new RuntimeException("@Service " + superName + " already has a implementation class " + serviceImpClasses.get(superName) +
                                " ! Consider use @Extension if you want your interface to have more than 1 implementations.");
                    }

                    if (currentInterfaces == null) {
                        throw new RuntimeException("currentInterfaces==null! current=" + currentName + ", super=" + superName);
                    }

                    boolean isServiceImp = false;
                    for (String currentInterface : currentInterfaces) {
                        if (superName.equals(currentInterface)) {
                            isServiceImp = true;
                            break;
                        }
                    }

                    if (!isServiceImp) {
                        throw new RuntimeException("Service " + currentName + " did not implement " + superName + "!!!");
                    }

                    serviceImpClasses.put(superName, currentName);
                    System.out.println("Find a valid @ServiceImp: super=" + superName + ", imp=" + currentName);
                } else {
                    throw new RuntimeException("Cannot recognize implement from witch Service: Name=" + currentName + ", service param=" + value);
                }
            }
            super.visit(name, value);
        }

        @Override
        public void visitEnd() {
            super.visitEnd();
        }
    }
}
