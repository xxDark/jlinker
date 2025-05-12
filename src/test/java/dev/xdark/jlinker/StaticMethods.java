package dev.xdark.jlinker;

public class StaticMethods {

	public static void foo() {
	}

	public static final class Child extends StaticMethods {
	}

	public static final class Child2 extends StaticMethods {
		public static void foo() {
		}
	}
}
