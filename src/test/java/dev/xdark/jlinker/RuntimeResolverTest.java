package dev.xdark.jlinker;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.EnumMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class RuntimeResolverTest {

    private static RuntimeAsmProvider provider;
    private static RuntimeResolver<ClassNode, MethodNode> resolver;

    @BeforeAll
    public static void setup() {
        provider = new RuntimeAsmProvider();
        resolver = RuntimeResolver.jvm(LinkResolver.jvm());
    }

    @Test
    public  void testInterfaceMethod() {
        doTest("java/util/EnumMap", "putIfAbsent", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", "java/util/Map", RuntimeResolver::resolveInterfaceMethod);
    }

    private void doTest(String owner, String name, String desc, String expected, Resolve<MethodNode> resolve) {
        Result<Resolution<ClassNode, MethodNode>> result = resolve.resolve(resolver, provider.findClass(owner), name, desc);
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
        Result<Resolution<ClassNode, V>> resolve(RuntimeResolver<ClassNode, V> resolver, ClassInfo<ClassNode> info, String name, String desc);
    }

}
