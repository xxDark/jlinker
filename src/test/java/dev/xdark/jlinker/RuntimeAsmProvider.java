package dev.xdark.jlinker;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;

final class RuntimeAsmProvider {

	private final Map<String, ClassInfo> classMap = new HashMap<>();

	ClassInfo findClass(String name) {
		if (name.charAt(0) == '[') {
			name = "java/lang/Object";
		}
		Map<String, ClassInfo> classMap = this.classMap;
		ClassInfo info = classMap.get(name);
		if (info == null) {
			ClassReader reader;
			try (InputStream in = ClassLoader.getSystemResourceAsStream(name + ".class")) {
				if (in == null) {
					throw new IllegalStateException(name);
				}
				reader = new ClassReader(in);
			} catch (IOException ex) {
				throw new UncheckedIOException(ex);
			}
			ClassNode node = new ClassNode();
			reader.accept(node, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG);
			info = new MyClassInfo(this, node);
			ClassInfo present = classMap.putIfAbsent(name, info);
			if (present != null) {
				info = present;
			}
		}
		return info;
	}
}
