package dev.xdark.jlinker;

import org.jspecify.annotations.NullMarked;

/// A [MethodDescriptor] backed by a JVM method descriptor string.
///
/// ## Example
/// ```java
/// // void method(String s)
/// MethodDescriptor d = MethodDescriptorString.of("(Ljava/lang/String;)V");
/// ```
@NullMarked
public final class MethodDescriptorString implements MethodDescriptor {
	private final String descriptor;

	private MethodDescriptorString(String descriptor) {
		this.descriptor = descriptor;
	}

	/// Returns the raw JVM method descriptor string, e.g. `"(Ljava/lang/String;)V"`.
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

	/// Creates a [MethodDescriptorString] from the given JVM method descriptor.
	///
	/// @param descriptor the JVM method descriptor string, e.g. `"(Ljava/lang/String;)V"`
	public static MethodDescriptorString of(String descriptor) {
		return new MethodDescriptorString(descriptor);
	}
}
