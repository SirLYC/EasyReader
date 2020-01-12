package com.lyc.appinject.visitors;


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
    private static final String SERVICE_IMPL_DESC = "Lcom/lyc/appinject/annotations/ServiceImp;";
    private static final String SERVICE_IMPL_SERVICE_PARAM = "service";
    private final Set<String> serviceClasses;
    private final Map<String, String> serviceImpClasses;

    // ============================== for extension ================================ //
    private static final String EXTENSION_DESC = "Lcom/lyc/appinject/annotations/Extension;";
    private static final String EXTENSION_IMPL_DESC = "Lcom/lyc/appinject/annotations/ExtensionImp;";
    private static final String EXTENSION_IMPL_SERVICE_PARAM = "extension";
    private final Set<String> extensionClasses;
    private final Map<String, List<String>> extensionImpClasses;

    private String currentName;
    private String[] currentInterfaces;

    public InjectCollectorClassVisitor(Set<String> serviceClasses, Map<String, String> serviceImpClasses, Set<String> extensionClasses, Map<String, List<String>> extensionImpClasses) {
        super(ASM6);
        this.serviceClasses = serviceClasses;
        this.serviceImpClasses = serviceImpClasses;
        this.extensionClasses = extensionClasses;
        this.extensionImpClasses = extensionImpClasses;
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
        } else if (EXTENSION_DESC.equals(desc)) {
            extensionClasses.add(currentName);
            System.out.println("Find a @Extension: " + currentName);
        } else if (EXTENSION_IMPL_DESC.equals(desc)) {
            return new ExtensionImpAnnotationVisitor();
        }
        return super.visitAnnotation(desc, visible);
    }

    private class ServiceImpAnnotationVisitor extends AnnotationVisitor {

        ServiceImpAnnotationVisitor() {
            super(ASM6);
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
    }

    private class ExtensionImpAnnotationVisitor extends AnnotationVisitor {

        ExtensionImpAnnotationVisitor() {
            super(ASM6);
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

                    List<String> list = extensionImpClasses.getOrDefault(superName, new ArrayList<>());
                    list.add(currentName);
                    extensionImpClasses.put(superName, list);
                    System.out.println("Find a valid @ExtensionImp: super=" + superName + ", imp=" + currentName);
                } else {
                    throw new RuntimeException("Cannot recognize implement from witch Extension: Name=" + currentName + ", extension param=" + value);
                }
            }
            super.visit(name, value);
        }
    }
}
