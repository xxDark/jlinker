package dev.xdark.jlinker;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class LinkResolverTest {

	private static RuntimeAsmProvider provider;
	private static LinkResolver linkResolver;

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
		doTest(TrickyField.class, "ALOAD", "I", Opcodes.class, LinkResolver::resolveStaticField);
		doTest(OpcodesB.class, "ALOAD", "I", Opcodes.class, LinkResolver::resolveStaticField);
	}

	@Test
	public void testVirtualField() {
		doTest(ArrayList.class, "size", "I", ArrayList.class, LinkResolver::resolveVirtualField);
		doTest(InstanceA.class, "field", "J", InstanceA.class, LinkResolver::resolveVirtualField);
		doTest(InstanceB.class, "field", "J", InstanceA.class, LinkResolver::resolveVirtualField);
		doTest(TrickyField.class, "ASTORE", "I", TrickyField.class, LinkResolver::resolveVirtualField);
	}

	private <M extends MemberInfo> void doTest(String owner, String name, String desc, String expected, Resolve<M> resolve) {
		Result<M> result = resolve.resolve(linkResolver, provider.findClass(owner), name, new DescriptorString(desc));
		testResult(result);
		testMatch(result.value(), expected);
	}

	private <M extends MemberInfo> void doTest(Class<?> owner, String name, String desc, String expected, Resolve<M> resolve) {
		doTest(Type.getInternalName(owner), name, desc, expected, resolve);
	}

	private <M extends MemberInfo> void doTest(Class<?> owner, String name, String desc, Class<?> expected, Resolve<M> resolve) {
		doTest(owner, name, desc, Type.getInternalName(expected), resolve);
	}

	private static void testResult(Result<?> result) {
		if (result instanceof Error) {
			fail(result.failureReason().name());
		}
	}

	private static void testMatch(MemberInfo member, String expected) {
		assertEquals(expected, ((MyClassInfo) member.getOwner()).node.name);
	}

	@FunctionalInterface
	private interface Resolve<V> {
		Result<V> resolve(LinkResolver resolver, ClassInfo info, String name, DescriptorString desc);
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

	private static final class TrickyField implements Opcodes {
		int ASTORE;
	}

	private interface Iface {
		int X = Integer.valueOf(5);
	}

	private static class OpcodesA implements Opcodes, Iface {
	}

	private static final class OpcodesB extends OpcodesA {
	}
}
