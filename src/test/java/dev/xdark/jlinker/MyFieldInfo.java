package dev.xdark.jlinker;

import org.objectweb.asm.tree.FieldNode;

final class MyFieldInfo implements FieldInfo {
	final MyClassInfo owner;
	final FieldNode node;

	MyFieldInfo(MyClassInfo owner, FieldNode node) {
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
