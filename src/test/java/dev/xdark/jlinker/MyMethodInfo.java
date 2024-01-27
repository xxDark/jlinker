package dev.xdark.jlinker;

import org.objectweb.asm.tree.MethodNode;

final class MyMethodInfo implements MethodInfo {
	final MyClassInfo owner;
	final MethodNode node;

	MyMethodInfo(MyClassInfo owner, MethodNode node) {
		this.owner = owner;
		this.node = node;
	}

	@Override
	public ClassInfo getOwner() {
		return owner;
	}

	@Override
	public int getAccessFlags() {
		return node.access;
	}
}
