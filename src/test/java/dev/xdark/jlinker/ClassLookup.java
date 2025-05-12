package dev.xdark.jlinker;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

final class ClassLookup {
	private final Map<String, MyClassModel> models = new ConcurrentHashMap<>();

	MyClassModel obtrude(ClassNode node) {
		MyClassModel cm;
		return Objects.requireNonNullElse(models.putIfAbsent(node.name, cm = new MyClassModel(this, node)), cm);
	}

	MyClassModel findClassOrNull(String name) {
		if (name.charAt(0) == '[') {
			name = "java/lang/Object";
		}
		MyClassModel cm;
		if ((cm = models.get(name)) != null)
			return cm;
		ClassNode node;
		try (var in = ClassLoader.getSystemResourceAsStream(name + ".class")) {
			if (in == null) {
				return null;
			}
			new ClassReader(in).accept(node = new ClassNode(), ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG);
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
		return obtrude(node);
	}

	MyClassModel findClass(String name) {
		MyClassModel cm;
		if ((cm = findClassOrNull(name)) == null)
			throw new ClassNotFoundException(name);
		return cm;
	}
}
