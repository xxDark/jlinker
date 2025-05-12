package dev.xdark.jlinker;

final class StaticFields {

	static class Case1 {
		static final int FIELD = 1;
	}

	static class Case1Child extends Case1 {
	}

	interface Case2 {
		int FIELD = 1;
	}

	static class Case2Child implements Case2 {
	}

	interface Case3 {
		int FIELD = 1;
	}

	interface Case3Child1 extends Case3 {
	}

	interface Case3Child2 extends Case3Child1 {
	}
}
