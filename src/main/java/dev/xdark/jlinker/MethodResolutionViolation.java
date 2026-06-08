package dev.xdark.jlinker;

/// Describes the reason a method resolution attempt failed.
///
/// Returned by [MethodResolutionException#violation()].
public enum MethodResolutionViolation {
	/// No method with the given name and descriptor exists in the hierarchy.
	NO_SUCH_METHOD,
	/// [LinkResolver#resolveInterfaceMethod] was called with a class reference
	/// instead of an interface.
	EXPECTED_INTERFACE,
	/// [LinkResolver#resolveVirtualMethod] was called with an interface reference
	/// instead of a class.
	EXPECTED_CLASS,
	/// The resolved method is not static, but a static method was required
	/// (`invokestatic`).
	EXPECTED_STATIC_METHOD,
	/// The resolved method is static, but a virtual/special method was required.
	EXPECTED_VIRTUAL_METHOD,
	/// A private interface method was found via `invokevirtual`; `invokespecial`
	/// must be used instead.
	REQUIRES_SPECIAL,
	/// An `invokespecial` on an interface method was attempted from a caller that
	/// does not directly implement the interface (JVMS §5.4.3.4).
	INDIRECT_INTERFACE,
}
