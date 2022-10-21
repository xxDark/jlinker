package dev.xdark.jlinker;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

final class RuntimeAsmProvider {

    private final Map<String, ClassInfo<ClassNode>> classMap = new HashMap<>();

    ClassInfo<ClassNode> findClass(String name) {
        if (name.charAt(0) == '[') {
            name = "java/lang/Object";
        }
        Map<String, ClassInfo<ClassNode>> classMap = this.classMap;
        ClassInfo<ClassNode> info = classMap.get(name);
        if (info == null) {
            ClassReader reader;
            try (InputStream in = ClassLoader.getSystemResourceAsStream(name + ".class")) {
                if (in == null) {
                    throw new IllegalStateException(name);
                }
                reader = new ClassReader(in);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            ClassNode node = new ClassNode();
            reader.accept(node, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG);
            info = makeClassInfo(node);
            ClassInfo<ClassNode> present = classMap.putIfAbsent(name, info);
            if (present != null) {
                info = present;
            }
        }
        return info;
    }

    private ClassInfo<ClassNode> makeClassInfo(ClassNode node) {
        return new ClassInfo<ClassNode>() {
            @Override
            public ClassNode innerValue() {
                return node;
            }

            @Override
            public int accessFlags() {
                return node.access;
            }

            @Override
            public ClassInfo<ClassNode> superClass() {
                String name = node.superName;
                return name == null ? null : findClass(name);
            }

            @Override
            public List<ClassInfo<ClassNode>> interfaces() {
                return node.interfaces.stream().map(RuntimeAsmProvider.this::findClass).collect(Collectors.toList());
            }

            @Override
            public MemberInfo<?> getMethod(String name, String descriptor) {
                for (MethodNode m : node.methods) {
                    if (name.equals(m.name) && descriptor.equals(m.desc)) {
                        return makeMethodInfo(m);
                    }
                }
                return null;
            }

            @Override
            public MemberInfo<?> getField(String name, String descriptor) {
                for (FieldNode f : node.fields) {
                    if (name.equals(f.name) && descriptor.equals(f.desc)) {
                        return makeFieldNode(f);
                    }
                }
                return null;
            }
        };
    }

    private static MemberInfo<MethodNode> makeMethodInfo(MethodNode node) {
        return new MemberInfo<MethodNode>() {
            @Override
            public MethodNode innerValue() {
                return node;
            }

            @Override
            public int accessFlags() {
                return node.access;
            }

            @Override
            public boolean isPolymorphic() {
                List<AnnotationNode> annotations = node.visibleAnnotations;
                return annotations != null && annotations.stream().anyMatch(x -> "Ljava/lang/invoke/MethodHandle$PolymorphicSignature;".equals(x.desc));
            }
        };
    }

    private static MemberInfo<FieldNode> makeFieldNode(FieldNode node) {
        return new MemberInfo<FieldNode>() {
            @Override
            public FieldNode innerValue() {
                return node;
            }

            @Override
            public int accessFlags() {
                return node.access;
            }

            @Override
            public boolean isPolymorphic() {
                return false;
            }
        };
    }
}
