package dev.xdark.jlinker;

import org.objectweb.asm.tree.MethodNode;

final class MyMethodModel implements MethodModel {
	final MyClassModel owner;
	final MethodNode methodNode;

	MyMethodModel(MyClassModel owner, MethodNode methodNode) {
		this.owner = owner;
		this.methodNode = methodNode;
	}

	@Override
	public int accessFlags() {
		return methodNode.access;
	}
}
