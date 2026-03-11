package dev.xdark.jlinker.runtime;

import java.util.IdentityHashMap;
import java.util.Map;

public final class RuntimeClassLoaders {
	private record MyClassLoader(ClassLoader parent, Module unnamedModule) implements ClassLoader {
		MyClassLoader {
			unnamedModule = new RuntimeModule(null, this, null);
		}
	}

	private final Map<java.lang.ClassLoader, ClassLoader> mapping = new IdentityHashMap<>();

	public ClassLoader mappingFor(java.lang.ClassLoader loader) {
		if (loader == null) return null;
		var parent = mappingFor(loader.getParent());
		var mapped = mapping.get(loader);
		if (mapped == null) {
			mapped = new MyClassLoader(parent, null);
			mapping.put(loader, mapped);
		}
		return mapped;
	}
}
