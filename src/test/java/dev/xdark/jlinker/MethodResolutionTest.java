package dev.xdark.jlinker;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Type;

import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class MethodResolutionTest {
	private static ClassLookup classLookup;
	private static LinkResolver linkResolver;

	@BeforeAll
	public static void setup() {
		classLookup = new ClassLookup();
		linkResolver = LinkResolver.create();
	}

	@Test
	public void testStaticMethods() {
		testMethodOk(() -> linkResolver.resolveStaticMethod(cls(System.class), "nanoTime", mdesc(long.class)), cls(System.class));
		testMethodOk(() -> linkResolver.resolveStaticMethod(cls(StaticMethods.Child.class), "foo", mdesc(void.class)), cls(StaticMethods.class));
		testMethodOk(() -> linkResolver.resolveStaticMethod(cls(StaticMethods.Child2.class), "foo", mdesc(void.class)), cls(StaticMethods.Child2.class));
		testMethodError(() -> linkResolver.resolveStaticMethod(cls(System.class), "(" /* illegal method name */, mdesc(long.class)), MethodResolutionViolation.NO_SUCH_METHOD);
	}

	@Test
	public void testVirtualMethods() {
		testMethodError(() -> linkResolver.resolveVirtualMethod(cls(System.class), "nanoTime", mdesc(long.class)), MethodResolutionViolation.EXPECTED_VIRTUAL_METHOD);
		testMethodError(() -> linkResolver.resolveInterfaceMethod(cls(Cloneable.class), "clone", mdesc(Object.class)), MethodResolutionViolation.NO_SUCH_METHOD);
		testMethodError(() -> linkResolver.resolveInterfaceMethod(cls(Collection.class), "clone", mdesc(Object.class)), MethodResolutionViolation.NO_SUCH_METHOD);
		testMethodOk(() -> linkResolver.resolveVirtualMethod(cls(ArrayList.class), "clone", mdesc(Object.class)), cls(ArrayList.class));
		testMethodOk(() -> linkResolver.resolveVirtualMethod(cls(Object.class), "clone", mdesc(Object.class)), cls(Object.class));
		testMethodOk(() -> linkResolver.resolveVirtualMethod(cls(String.class), "clone", mdesc(Object.class)), cls(Object.class));

		testMethodOk(() -> linkResolver.resolveInterfaceMethod(cls(Stream.class), "close", mdesc(void.class)));
		testMethodOk(() -> linkResolver.resolveVirtualMethod(cls(EnumSet.class), "forEach", mdesc(void.class, Consumer.class)));

		testMethodOk(() -> linkResolver.resolveSpecialMethod(cls(InstanceMethods.Case1Child.class), "foo", mdesc(void.class), cls(InstanceMethods.Case1ChildChild.class)));
	}

	@FunctionalInterface
	private interface ModelSupplier {

		MyMethodModel get() throws MethodResolutionException;
	}

	private static void testMethodOk(ModelSupplier modelSupplier) {
		try {
			modelSupplier.get();
		} catch (MethodResolutionException ex) {
			fail(ex);
		}
	}

	private static void testMethodOk(ModelSupplier modelSupplier, MyClassModel expected) {
		MyMethodModel method;
		try {
			method = modelSupplier.get();
		} catch (MethodResolutionException ex) {
			fail(ex);
			return;
		}
		assertEquals(expected, method.owner);
	}

	private static void testMethodError(ModelSupplier modelSupplier, MethodResolutionViolation violation) {
		try {
			modelSupplier.get();
		} catch (MethodResolutionException ex) {
			assertEquals(violation, ex.violation());
			return;
		} catch (Throwable t) {
			fail("Expected %s".formatted(violation), t);
		}
		fail("Expected %s".formatted(violation));
	}

	private static MyClassModel cls(Class<?> c) {
		return classLookup.findClass(Type.getInternalName(c));
	}

	private static MyClassModel cls(String name) {
		return classLookup.findClass(name);
	}

	private static MethodDescriptorString mdesc(Class<?> returnType, Class<?>... argumentTypes) {
		return MethodDescriptorString.of(MethodType.methodType(returnType, argumentTypes).descriptorString());
	}
}
