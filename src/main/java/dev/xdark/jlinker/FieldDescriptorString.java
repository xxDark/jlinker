package dev.xdark.jlinker;

import org.jspecify.annotations.NullMarked;

@NullMarked
public final class FieldDescriptorString implements FieldDescriptor {
	private final String descriptor;

	private FieldDescriptorString(String descriptor) {
		this.descriptor = descriptor;
	}

	@Override
	public String toString() {
		return descriptor;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof FieldDescriptorString that && descriptor.equals(that.descriptor);
	}

	@Override
	public int hashCode() {
		return descriptor.hashCode();
	}

	public static FieldDescriptorString of(String descriptor) {
		return new FieldDescriptorString(descriptor);
	}
}
