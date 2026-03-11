package dev.xdark.jlinker;

import dev.xdark.jlinker.runtime.Module;
import dev.xdark.jlinker.runtime.RuntimeModule;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.tree.ClassNode;

import java.util.Iterator;

@NullMarked
final class MyClassModel implements ClassModel<MyMethodModel, MyFieldModel> {
	private static final Module UNNAMED_MODULE = new RuntimeModule(null, null, null);
	private final ClassLookup classLookup;
	final ClassNode classNode;

	MyClassModel(ClassLookup classLookup, ClassNode classNode) {
		this.classLookup = classLookup;
		this.classNode = classNode;
	}

	@Override
	public Module module() {
		return UNNAMED_MODULE;
	}

	@Override
	public String name() {
		return classNode.name;
	}

	@Override
	public int accessFlags() {
		return classNode.access;
	}

	@Override
	public @Nullable ClassModel<MyMethodModel, MyFieldModel> superClass() {
		var sp = classNode.superName;
		return sp == null ? null : classLookup.findClass(sp);
	}

	@Override
	public @Unmodifiable Iterable<? extends ClassModel<MyMethodModel, MyFieldModel>> interfaces() {
		var interfaces = classNode.interfaces;
		var classLookup = this.classLookup;
		return () -> new Iterator<>() {
			final Iterator<String> iterator = interfaces.iterator();

			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public ClassModel<MyMethodModel, MyFieldModel> next() {
				return classLookup.findClass(iterator.next());
			}
		};
	}

	@Override
	public @Nullable MyMethodModel findMethod(String name, MethodDescriptor descriptor) {
		var rawDesc = descriptor.toString();
		for (var m : classNode.methods) {
			if (name.equals(m.name) && rawDesc.equals(m.desc)) {
				return new MyMethodModel(this, m);
			}
		}
		return null;
	}

	@Override
	public @Nullable MyFieldModel findField(String name, FieldDescriptor descriptor) {
		var rawDesc = descriptor.toString();
		for (var f : classNode.fields) {
			if (name.equals(f.name) && rawDesc.equals(f.desc)) {
				return new MyFieldModel(this, f);
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return classNode.name;
	}
}
