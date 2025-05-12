package dev.xdark.jlinker;

final class ClassNotFoundException extends RuntimeException {

	ClassNotFoundException(String name) {
		super(name, null, false, false);
	}
}
