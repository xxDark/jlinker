package dev.xdark.jlinker;

/// Represents a field in the abstract class model used by [LinkResolver].
///
/// Implementations supply at minimum the access flags via [#accessFlags()].
/// Additional information (owner class, name, descriptor, etc.) may be stored
/// in the concrete type and is transparent to the resolver.
public non-sealed interface FieldModel extends MemberModel {
}
