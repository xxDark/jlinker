package dev.xdark.jlinker;

/// Describes the reason a field resolution attempt failed.
///
/// Returned by [FieldResolutionException#violation()].
public enum FieldResolutionViolation {
	/// No field with the given name and descriptor exists in the hierarchy.
	NO_SUCH_FIELD,
	/// The resolved field is not static, but a static field was required
	/// (`getstatic` / `putstatic`).
	EXPECTED_STATIC_FIELD,
	/// The resolved field is static, but an instance field was required
	/// (`getfield` / `putfield`).
	EXPECTED_VIRTUAL_FIELD,
}
