package dev.xdark.jlinker;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

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

	@NotNull
	@Contract(pure = true)
	public static MethodDescriptorString of(@NotNull String descriptor) {
		return new MethodDescriptorString(descriptor);
	}
}
