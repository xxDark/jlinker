# jlinker
Java resolution library for methods and fields.

## example

```Java
package dev.xdark.jlinker;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Example {
    public static void main(String[] args) {
        Function<String, ClassNode> fn = s -> {
            // Ideally, the resolver should do that conversion,
            // but it doesn't. Yet.
            if (s.charAt(0) == '[') {
                s = "java/lang/Object";
            }
            ClassReader reader;
            try (InputStream in = ClassLoader.getSystemResourceAsStream(s + ".class")) {
                if (in == null) {
                    throw new IllegalStateException(s);
                }
                reader = new ClassReader(in);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            ClassNode node = new ClassNode();
            // We don't care about code or debug information
            reader.accept(node, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG);
            return node;
        };
        // Create link resolver
        LinkResolver<ClassNode, MethodNode, FieldNode> resolver = LinkResolver.jvm();
        // Create class info for java/util/AbstractList
        ClassInfo<ClassNode> classInfo = classInfo(fn.apply("java/util/AbstractList"), fn);
        // Resolve virtual "containsAll" method
        Resolution<ClassNode, MethodNode> resolution = resolver.resolveVirtualMethod(classInfo, "containsAll", "(Ljava/util/Collection;)Z").value();
        ClassNode owner = resolution.owner().innerValue();
        MethodNode m = resolution.member().innerValue();
        // Should be java/util/AbstractCollection.containsAll(Ljava/util/Collection;)Z
        System.out.println(owner.name + '.' + m.name + m.desc);
    }

    // Method information
    private static MemberInfo<MethodNode> methodInfo(MethodNode node) {
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
                // To implement that, one should check for PolymorphicSignature annotation,
                // doesn't matter in this example. Linker does not support polymorphic methods.
                // Yet.
                return false;
            }
        };
    }

    // Field information
    private static MemberInfo<FieldNode> fieldInfo(FieldNode node) {
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

    // Class information
    private static ClassInfo<ClassNode> classInfo(ClassNode node, Function<String, ClassNode> fn) {
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
                String superName = node.superName;
                return superName == null ? null : classInfo(fn.apply(superName), fn);
            }

            @Override
            public List<ClassInfo<ClassNode>> interfaces() {
                return node.interfaces.stream().map(x -> classInfo(fn.apply(x), fn)).collect(Collectors.toList());
            }

            @Override
            public MemberInfo<?> getMethod(String name, String descriptor) {
                for (MethodNode method : node.methods) {
                    if (name.equals(method.name) && descriptor.equals(method.desc)) {
                        return methodInfo(method);
                    }
                }
                return null;
            }

            @Override
            public MemberInfo<?> getField(String name, String descriptor) {
                for (FieldNode field : node.fields) {
                    if (name.equals(field.name) && descriptor.equals(field.desc)) {
                        return fieldInfo(field);
                    }
                }
                return null;
            }
        };
    }
}
```