package dev.xdark.jlinker;

import org.jspecify.annotations.NullMarked;

/// Identifies a method's parameter and return types.
///
/// The standard implementation is [MethodDescriptorString], which wraps the
/// JVM method descriptor string (e.g. `"(Ljava/lang/String;)V"`).
/// Custom implementations must provide consistent [Object#equals] and
/// [Object#hashCode] so that descriptors can be compared during lookup.
///
/// @see MethodDescriptorString
@NullMarked
public interface MethodDescriptor {

	/// Returns the string form of this descriptor, e.g. `"(Ljava/lang/String;)V"`.
	String toString();
}
