package com.lyc.appinject.visitors;


import com.lyc.appinject.data.Impl;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Liu Yuchuan on 2020/1/12.
 */
public class InjectCollectorClassVisitor extends ClassVisitor implements Opcodes {

    // ============================== for service ================================ //
    private static final String SERVICE_DESC = "Lcom/lyc/appinject/annotations/Service;";
    private static final String SERVICE_IMPL_DESC = "Lcom/lyc/appinject/annotations/ServiceImpl;";
    private static final String SERVICE_IMPL_SERVICE_PARAM = "service";
    private final Set<String> serviceClasses;
    private final Map<String, Impl> serviceImplClasses;

    // ============================== for extension ================================ //
    private static final String EXTENSION_DESC = "Lcom/lyc/appinject/annotations/Extension;";
    private static final String EXTENSION_IMPL_DESC = "Lcom/lyc/appinject/annotations/ExtensionImpl;";
    private static final String EXTENSION_IMPL_SERVICE_PARAM = "extension";
    private final Set<String> extensionClasses;
    private final Map<String, List<Impl>> extensionImplClasses;

    private static final String CREATE_METHOD_DESC = "Lcom/lyc/appinject/CreateMethod;";
    private static final String CREATE_METHOD_PARAM = "createMethod";

    private String currentSuperName;
    private String currentName;
    private String currentCreateMethod;
    private String[] currentInterfaces;

    public InjectCollectorClassVisitor(Set<String> serviceClasses, Map<String, Impl> serviceImplClasses, Set<String> extensionClasses, Map<String, List<Impl>> extensionImplClasses) {
        super(ASM6);
        this.serviceClasses = serviceClasses;
        this.serviceImplClasses = serviceImplClasses;
        this.extensionClasses = extensionClasses;
        this.extensionImplClasses = extensionImplClasses;
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
            return new ServiceImpAnnotationVisitor(currentName);
        } else if (EXTENSION_DESC.equals(desc)) {
            extensionClasses.add(currentName);
            System.out.println("Find a @Extension: " + currentName);
        } else if (EXTENSION_IMPL_DESC.equals(desc)) {
            return new ExtensionImpAnnotationVisitor(currentName);
        }
        return super.visitAnnotation(desc, visible);
    }

    private class BaseAnnotationVisitor extends AnnotationVisitor {

        final String currentName;

        BaseAnnotationVisitor(String currentName) {
            super(ASM6);
            this.currentName = currentName;
        }

        @Override
        public void visitEnum(String name, String desc, String value) {
            if (CREATE_METHOD_PARAM.equals(name) && CREATE_METHOD_DESC.equals(desc)) {
                currentCreateMethod = value;
            }
            super.visitEnum(name, desc, value);
        }
    }

    private class ServiceImpAnnotationVisitor extends BaseAnnotationVisitor {

        ServiceImpAnnotationVisitor(String currentName) {
            super(currentName);
        }

        @Override
        public void visit(String name, Object value) {
            if (SERVICE_IMPL_SERVICE_PARAM.equals(name)) {
                String desc = String.valueOf(value);
                if (desc.length() > 2) {
                    // remove "L" and ";" from method desc
                    String superName = desc.substring(1, desc.length() - 1);

                    if (serviceImplClasses.containsKey(superName)) {
                        throw new RuntimeException("@Service " + superName + " already has a implementation class " + serviceImplClasses.get(superName) +
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

                    currentSuperName = superName;
                } else {
                    throw new RuntimeException("Cannot recognize implement from witch Service: Name=" + currentName + ", service param=" + value);
                }
            }
            super.visit(name, value);
        }

        @Override
        public void visitEnd() {
            if (currentSuperName != null) {
                serviceImplClasses.put(currentSuperName, new Impl(currentName, currentCreateMethod));
                System.out.println("Find a valid @ServiceImp: super=" + currentSuperName + ", imp=" + currentName);
                currentSuperName = null;
            }
            super.visitEnd();
        }
    }

    private class ExtensionImpAnnotationVisitor extends BaseAnnotationVisitor {

        ExtensionImpAnnotationVisitor(String currentName) {
            super(currentName);
        }

        @Override
        public void visit(String name, Object value) {
            if (EXTENSION_IMPL_SERVICE_PARAM.equals(name)) {
                String desc = String.valueOf(value);
                if (desc.length() > 2) {
                    // remove "L" and ";" from method desc
                    String superName = desc.substring(1, desc.length() - 1);

                    if (currentInterfaces == null) {
                        throw new RuntimeException("currentInterfaces==null! current=" + currentName + ", super=" + superName);
                    }

                    boolean isExtensionImp = false;
                    for (String currentInterface : currentInterfaces) {
                        if (superName.equals(currentInterface)) {
                            isExtensionImp = true;
                            break;
                        }
                    }

                    if (!isExtensionImp) {
                        throw new RuntimeException("Extension " + currentName + " did not implement " + superName + "!!!");
                    }
                    currentSuperName = superName;

                } else {
                    throw new RuntimeException("Cannot recognize implement from witch Extension: Name=" + currentName + ", extension param=" + value);
                }
            }
            super.visit(name, value);
        }

        @Override
        public void visitEnd() {
            if (currentSuperName != null) {
                List<Impl> list = extensionImplClasses.getOrDefault(currentSuperName, new ArrayList<>());
                list.add(new Impl(currentName, currentCreateMethod));
                extensionImplClasses.put(currentSuperName, list);
                System.out.println("Find a valid @ExtensionImp: super=" + currentSuperName + ", imp=" + currentName);
                currentSuperName = null;
            }
            super.visitEnd();
        }
    }
}
