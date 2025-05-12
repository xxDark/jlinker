package dev.xdark.jlinker;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

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

	@NotNull
	@Contract(pure = true)
	public static FieldDescriptorString of(@NotNull String descriptor) {
		return new FieldDescriptorString(descriptor);
	}
}
