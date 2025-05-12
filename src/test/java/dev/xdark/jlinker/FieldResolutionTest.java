package dev.xdark.jlinker;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Type;

import java.io.PrintStream;

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

	private static void testStaticFieldOk(ClassOrName refc, String name, FieldDescriptorString descriptor, ClassOrName expected) {
		testStaticFieldOk(refc.resolve(classLookup), name, descriptor, expected.resolve(classLookup));
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
