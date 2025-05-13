package dev.xdark.jlinker;

import org.objectweb.asm.Type;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

final class PolymorphicMethods {
	private static final Map<String, Set<String>> polymorphicMethods;

	private PolymorphicMethods() {
	}

	static boolean isPolymorphicMethod(String owner, String name, String descriptor) {
		// Resolution of polymorphic signature methods is not supported.
		var set = polymorphicMethods.get(owner);
		return set != null && set.contains(name);
	}

	private static void putPolymorphicMethods(Map<String, Set<String>> map, Class<?> holder) {
		var set = new HashSet<String>();
		loop:
		for (var m : holder.getDeclaredMethods()) {
			for (var declaredAnnotation : m.getDeclaredAnnotations()) {
				if ("PolymorphicSignature".equals(declaredAnnotation.annotationType().getSimpleName())) {
					set.add(m.getName());
					continue loop;
				}
			}
		}
		if (!set.isEmpty()) {
			map.put(Type.getInternalName(holder), set);
		}
	}

	static {
		var map = new HashMap<String, Set<String>>();
		putPolymorphicMethods(map, MethodHandle.class);
		putPolymorphicMethods(map, VarHandle.class);
		polymorphicMethods = map;
	}
}
