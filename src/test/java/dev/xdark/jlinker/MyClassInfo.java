package dev.xdark.jlinker;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;
import java.util.stream.Collectors;

final class MyClassInfo implements ClassInfo {
	private final RuntimeAsmProvider provider;
	final ClassNode node;

	public MyClassInfo(RuntimeAsmProvider provider, ClassNode node) {
		this.provider = provider;
		this.node = node;
	}

	@Override
	public int getAccessFlags() {
		return node.access;
	}

	@Override
	public @Nullable ClassInfo getSuperclass() {
		String sp = node.superName;
		return sp == null ? null : provider.findClass(sp);
	}

	@Override
	public @NotNull List<? extends @NotNull ClassInfo> getInterfaces() {
		return node.interfaces.stream().map(provider::findClass).collect(Collectors.toList());
	}

	@Override
	public @Nullable MethodInfo getMethod(String name, MethodDescriptor descriptor) {
		String descriptorString = descriptor.toString();
		for (MethodNode m : node.methods) {
			if (name.equals(m.name) && descriptorString.equals(m.desc)) {
				return new MyMethodInfo(this, m);
			}
		}
		return null;
	}

	@Override
	public @Nullable FieldInfo getField(String name, FieldDescriptor descriptor) {
		String descriptorString = descriptor.toString();
		for (FieldNode f : node.fields) {
			if (name.equals(f.name) && descriptorString.equals(f.desc)) {
				return new MyFieldInfo(this, f);
			}
		}
		return null;
	}
}
