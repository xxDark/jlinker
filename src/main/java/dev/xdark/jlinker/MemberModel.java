package dev.xdark.jlinker;

import java.lang.reflect.Modifier;

/// Base model for a class member (method or field).
///
/// Sealed to exactly [MethodModel] and [FieldModel]; use exhaustive pattern
/// matching to distinguish them.
public sealed interface MemberModel permits MethodModel, FieldModel {

	/// Returns the raw access flags of this member as defined by the class file format.
	///
	/// The resolver uses [Modifier] constants to interpret these
	/// flags (e.g. [Modifier#isStatic(int)], [Modifier#isPublic(int)]).
	int accessFlags();
}
