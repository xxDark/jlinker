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

	MyClassModel findClass(String name) {
		if (name.charAt(0) == '[') {
			name = "java/lang/Object";
		}
		var models = this.models;
		MyClassModel cm;
		if ((cm = models.get(name)) != null)
			return cm;
		ClassNode node;
		try (var in = ClassLoader.getSystemResourceAsStream(name + ".class")) {
			if (in == null) {
				throw new IllegalStateException("Class not found %s".formatted(name));
			}
			new ClassReader(in).accept(node = new ClassNode(), ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG);
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
		return Objects.requireNonNullElse(models.putIfAbsent(name, cm = new MyClassModel(this, node)), cm);
	}
}
