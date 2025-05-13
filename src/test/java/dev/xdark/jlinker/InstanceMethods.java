package dev.xdark.jlinker;

public final class InstanceMethods {

	public interface Case1 {
		default void foo() {
		}
	}

	public static abstract class Case1Child implements Case1 {
	}

	public static final class Case1ChildChild extends Case1Child {
		@Override
		public void foo() {
			super.foo();
		}
	}
}
