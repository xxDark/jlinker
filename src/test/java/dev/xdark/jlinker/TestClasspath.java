package dev.xdark.jlinker;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;
import java.util.zip.ZipFile;

@DisabledIfEnvironmentVariable(
		named = "CI",
		matches = ".*"
)
public class TestClasspath {

	@Test
	public void doTest() throws Exception {
		var classLookup = new ClassLookup();
		var linkResolver = LinkResolver.create();
		var reader = new AsmClassReader();
		for (var cp : System.getProperty("java.class.path").split(File.pathSeparator)) {
			if (cp.isEmpty()) continue;
			JarFile zf;
			try {
				zf = new JarFile(new File(cp), false, ZipFile.OPEN_READ, JarFile.runtimeVersion());
			} catch (IOException ignored) {
				continue;
			}
			try (zf) {
				var iterator = zf.versionedStream().iterator();
				while (iterator.hasNext()) {
					var ze = iterator.next();
					if (!ze.getName().endsWith(".class")) continue;
					ClassReader classReader;
					try (var in = zf.getInputStream(ze)) {
						classReader = reader.read(in);
					}
					var node = new ClassNode();
					classReader.accept(node, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG);
					var caller = classLookup.obtrude(node);
					classReader.accept(new AssertingClassVisitor(classLookup, linkResolver, caller), ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
				}
			}
		}
	}
}
