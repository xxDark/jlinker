package dev.xdark.jlinker;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.lang.module.ModuleFinder;

@DisabledIfEnvironmentVariable(
		named = "CI",
		matches = ".*"
)
public class TestJmods {

	@Test
	public void doTest() throws Exception {
		var classLookup = new ClassLookup();
		var linkResolver = LinkResolver.create();
		var reader = new AsmClassReader();
		for (var moduleReference : ModuleFinder.ofSystem().findAll()) {
			try (var mr = moduleReference.open()) {
				try (var stream = mr.list()) {
					var iterator = stream.iterator();
					while (iterator.hasNext()) {
						var name = iterator.next();
						if (!name.endsWith(".class")) continue;
						ClassReader classReader;
						try (var in = mr.open(name).orElseThrow()) {
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
}
