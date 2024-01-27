package dev.xdark.jlinker;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class RuntimeResolverTest {

	private static RuntimeAsmProvider provider;
	private static RuntimeResolver resolver;

	@BeforeAll
	public static void setup() {
		provider = new RuntimeAsmProvider();
		resolver = RuntimeResolver.jvm(LinkResolver.jvm());
	}

	@Test
	public void testInterfaceMethod() {
		doTest("java/util/EnumMap", "putIfAbsent", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", "java/util/Map", RuntimeResolver::resolveInterfaceMethod);
	}

	private void doTest(String owner, String name, String desc, String expected, Resolve<MethodInfo> resolve) {
		Result<MethodInfo> result = resolve.resolve(resolver, provider.findClass(owner), name, new DescriptorString(desc));
		testResult(result);
		testMatch(result.getValue(), expected);
	}

	private void doTest(Class<?> owner, String name, String desc, String expected, Resolve<MethodInfo> resolve) {
		doTest(Type.getInternalName(owner), name, desc, expected, resolve);
	}

	private void doTest(Class<?> owner, String name, String desc, Class<?> expected, Resolve<MethodInfo> resolve) {
		doTest(owner, name, desc, Type.getInternalName(expected), resolve);
	}

	private static void testResult(Result<?> result) {
		if (result instanceof Error) {
			fail(result.getFailureReason().name());
		}
	}

	private static void testMatch(MemberInfo member, String expected) {
		assertEquals(expected, ((MyClassInfo) member.getOwner()).node.name);
	}

	@FunctionalInterface
	private interface Resolve<V> {
		Result<V> resolve(RuntimeResolver resolver, ClassInfo info, String name, MethodDescriptor desc);
	}

}
