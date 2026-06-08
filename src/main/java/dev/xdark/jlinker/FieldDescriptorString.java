package dev.xdark.jlinker;

import org.jspecify.annotations.NullMarked;

/// A [FieldDescriptor] backed by a JVM field descriptor string.
///
/// ## Example
/// ```java
/// // int field
/// FieldDescriptor d = FieldDescriptorString.of("I");
/// // String field
/// FieldDescriptor d = FieldDescriptorString.of("Ljava/lang/String;");
/// ```
@NullMarked
public final class FieldDescriptorString implements FieldDescriptor {
	private final String descriptor;

	private FieldDescriptorString(String descriptor) {
		this.descriptor = descriptor;
	}

	/// Returns the raw JVM field descriptor string, e.g. `"Ljava/lang/String;"` or `"I"`.
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

	/// Creates a [FieldDescriptorString] from the given JVM field descriptor.
	///
	/// @param descriptor the JVM field descriptor string, e.g. `"Ljava/lang/String;"` or `"I"`
	public static FieldDescriptorString of(String descriptor) {
		return new FieldDescriptorString(descriptor);
	}
}
