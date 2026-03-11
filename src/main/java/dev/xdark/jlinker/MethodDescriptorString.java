package dev.xdark.jlinker;

import org.jspecify.annotations.NullMarked;

@NullMarked
public final class MethodDescriptorString implements MethodDescriptor {
	private final String descriptor;

	private MethodDescriptorString(String descriptor) {
		this.descriptor = descriptor;
	}

	@Override
	public String toString() {
		return descriptor;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof MethodDescriptorString that && descriptor.equals(that.descriptor);
	}

	@Override
	public int hashCode() {
		return descriptor.hashCode();
	}

	public static MethodDescriptorString of(String descriptor) {
		return new MethodDescriptorString(descriptor);
	}
}
