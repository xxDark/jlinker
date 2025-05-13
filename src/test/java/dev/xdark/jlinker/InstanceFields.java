package dev.xdark.jlinker;

import org.objectweb.asm.Opcodes;

final class InstanceFields {
	private int field1;

	public static final class Case1 implements Opcodes {
		int ASTORE = 0;
	}

	public static class Case2 {
		int ASTORE = 0;
	}

	public static class Case2Child extends Case2 implements Opcodes {
	}
}
