package dev.xdark.jlinker;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class LinkResolverTest {

    private static RuntimeAsmProvider provider;
    private static LinkResolver<ClassNode, MethodNode, FieldInsnNode> linkResolver;

    @BeforeAll
    public static void setup() {
        provider = new RuntimeAsmProvider();
        linkResolver = LinkResolver.jvm();
    }

    @Test
    public void testStaticMethod() {
        doTest(System.class, "nanoTime", "()J", System.class, LinkResolver::resolveStaticMethod);
        doTest(Stream.class, "empty", "()Ljava/util/stream/Stream;", Stream.class, LinkResolver::resolveStaticMethod);
    }

    @Test
    public void testVirtualMethod() {
        doTest(ArrayList.class, "stream", "()Ljava/util/stream/Stream;", Collection.class, LinkResolver::resolveVirtualMethod);
        doTest(ArrayList.class, "containsAll", "(Ljava/util/Collection;)Z", AbstractCollection.class, LinkResolver::resolveVirtualMethod);
    }

    @Test
    public void testInterfaceMethod() {
        doTest(List.class, "add", "(Ljava/lang/Object;)Z", List.class, LinkResolver::resolveInterfaceMethod);
        doTest(List.class, "stream", "()Ljava/util/stream/Stream;", Collection.class, LinkResolver::resolveInterfaceMethod);
    }

    @Test
    public void testStaticField() {
        doTest(DummyClass.class, "ALOAD", "I", Opcodes.class, LinkResolver::resolveStaticField);
        doTest(DummyClass.class, "value", "I", DummyClassBase.class, LinkResolver::resolveStaticField);
    }

    @Test
    public void testVirtualField() {
        doTest(ArrayList.class, "size", "I", ArrayList.class, LinkResolver::resolveVirtualField);
        doTest(InstanceA.class, "field", "J", InstanceA.class, LinkResolver::resolveVirtualField);
        doTest(InstanceB.class, "field", "J", InstanceA.class, LinkResolver::resolveVirtualField);
    }

    private void doTest(String owner, String name, String desc, String expected, Resolve<MethodNode> resolve) {
        Result<Resolution<ClassNode, MethodNode>> result = resolve.resolve((LinkResolver) linkResolver, provider.findClass(owner), name, desc);
        testResult(result);
        testMatch(result.value(), expected);
    }

    private void doTest(Class<?> owner, String name, String desc, String expected, Resolve<MethodNode> resolve) {
        doTest(Type.getInternalName(owner), name, desc, expected, resolve);
    }

    private void doTest(Class<?> owner, String name, String desc, Class<?> expected, Resolve<MethodNode> resolve) {
        doTest(owner, name, desc, Type.getInternalName(expected), resolve);
    }

    private static void testResult(Result<?> result) {
        if (!result.isSuccess()) {
            fail(result.error().name());
        }
    }

    private static void testMatch(Resolution<ClassNode, ?> resolution, String expected) {
        assertEquals(expected, resolution.owner().innerValue().name);
    }

    @FunctionalInterface
    private interface Resolve<V> {
        Result<Resolution<ClassNode, V>> resolve(LinkResolver<ClassNode, V, V> resolver, ClassInfo<ClassNode> info, String name, String desc);
    }

    private static class InstanceA {
        long field;
    }

    private static final class InstanceB extends InstanceA {
    }

    private static class DummyClassBase {
        static int value;
    }

    private static final class DummyClass extends DummyClassBase implements Opcodes {
    }
}
