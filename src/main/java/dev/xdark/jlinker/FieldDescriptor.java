package dev.xdark.jlinker;

import org.jspecify.annotations.NullMarked;

/// Identifies a field's type.
///
/// The standard implementation is [FieldDescriptorString], which wraps the
/// JVM field descriptor string (e.g. `"Ljava/lang/String;"` or `"I"`).
/// Custom implementations must provide consistent [Object#equals] and
/// [Object#hashCode] so that descriptors can be compared during lookup.
///
/// @see FieldDescriptorString
@NullMarked
public interface FieldDescriptor {
}
