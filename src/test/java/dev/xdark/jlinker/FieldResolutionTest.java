package dev.xdark.jlinker;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Type;

import java.io.FilterOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.zip.CheckedOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class FieldResolutionTest {
	private static ClassLookup classLookup;
	private static LinkResolver linkResolver;

	@BeforeAll
	public static void setup() {
		classLookup = new ClassLookup();
		linkResolver = LinkResolver.create();
	}

	@Test
	public void testStaticFields() {
		testStaticFieldOk(cls(System.class), "out", fdesc(PrintStream.class), cls(System.class));

		testStaticFieldOk(cls(StaticFields.Case1.class), "FIELD", fdesc(int.class), cls(StaticFields.Case1.class));
		testStaticFieldOk(cls(StaticFields.Case1Child.class), "FIELD", fdesc(int.class), cls(StaticFields.Case1.class));
		testStaticFieldOk(cls(StaticFields.Case2Child.class), "FIELD", fdesc(int.class), cls(StaticFields.Case2.class));

		testStaticFieldOk(cls(StaticFields.Case3Child1.class), "FIELD", fdesc(int.class), cls(StaticFields.Case3.class));
		testStaticFieldOk(cls(StaticFields.Case3Child2.class), "FIELD", fdesc(int.class), cls(StaticFields.Case3.class));

		testStaticFieldError(cls(InstanceFields.class), "field1", fdesc(int.class), FieldResolutionViolation.EXPECTED_STATIC_FIELD);
		testStaticFieldError(cls(System.class), "out_", fdesc(PrintStream.class), FieldResolutionViolation.NO_SUCH_FIELD);
	}

	@Test
	public void testVirtualFields() {
		testVirtualFieldOk(cls(CheckedOutputStream.class), "out", fdesc(OutputStream.class), cls(FilterOutputStream.class));
		testVirtualFieldError(cls(System.class), "out", fdesc(PrintStream.class), FieldResolutionViolation.EXPECTED_VIRTUAL_FIELD);
		testVirtualFieldOk(cls(InstanceFields.Case1.class), "ASTORE", fdesc(int.class), cls(InstanceFields.Case1.class));
		testVirtualFieldError(cls(InstanceFields.Case2Child.class), "ASTORE", fdesc(int.class), FieldResolutionViolation.EXPECTED_VIRTUAL_FIELD);
	}

	private static void testStaticFieldOk(MyClassModel refc, String name, FieldDescriptorString descriptor, MyClassModel expected) {
		MyFieldModel field;
		try {
			field = linkResolver.resolveStaticField(refc, name, descriptor);
		} catch (FieldResolutionException ex) {
			fail(ex);
			return;
		}
		assertEquals(expected, field.owner);
	}

	private static void testStaticFieldError(MyClassModel refc, String name, FieldDescriptorString descriptor, FieldResolutionViolation violation) {
		try {
			var unused = linkResolver.resolveStaticField(refc, name, descriptor);
		} catch (FieldResolutionException ex) {
			assertEquals(violation, ex.violation());
			return;
		} catch (Throwable t) {
			fail("Expected %s".formatted(violation), t);
		}
		fail("Expected %s".formatted(violation));
	}

	private static void testVirtualFieldOk(MyClassModel refc, String name, FieldDescriptorString descriptor, MyClassModel expected) {
		MyFieldModel field;
		try {
			field = linkResolver.resolveVirtualField(refc, name, descriptor);
		} catch (FieldResolutionException ex) {
			fail(ex);
			return;
		}
		assertEquals(expected, field.owner);
	}

	private static void testVirtualFieldError(MyClassModel refc, String name, FieldDescriptorString descriptor, FieldResolutionViolation violation) {
		try {
			var unused = linkResolver.resolveVirtualField(refc, name, descriptor);
		} catch (FieldResolutionException ex) {
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

	private static FieldDescriptorString fdesc(Class<?> type) {
		return FieldDescriptorString.of(Type.getDescriptor(type));
	}
}
