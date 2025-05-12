package dev.xdark.jlinker;

import org.objectweb.asm.tree.FieldNode;

final class MyFieldModel implements FieldModel {
	final MyClassModel owner;
	final FieldNode fieldNode;

	MyFieldModel(MyClassModel owner, FieldNode fieldNode) {
		this.owner = owner;
		this.fieldNode = fieldNode;
	}

	@Override
	public int accessFlags() {
		return fieldNode.access;
	}
}
