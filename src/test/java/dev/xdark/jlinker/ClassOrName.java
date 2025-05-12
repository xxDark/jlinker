package dev.xdark.jlinker;

import org.objectweb.asm.Type;

@SuppressWarnings("ClassEscapesDefinedScope")
sealed interface ClassOrName {

	record Cls(Class<?> c) implements ClassOrName {

		@Override
		public MyClassModel resolve(ClassLookup lookup) {
			return lookup.findClass(Type.getInternalName(c));
		}
	}

	record Name(String name) implements ClassOrName {
		@Override
		public MyClassModel resolve(ClassLookup lookup) {
			return lookup.findClass(name);
		}
	}

	MyClassModel resolve(ClassLookup lookup);

	static Cls of(Class<?> cls) {
		return new Cls(cls);
	}

	static Name of(String name) {
		return new Name(name);
	}
}
