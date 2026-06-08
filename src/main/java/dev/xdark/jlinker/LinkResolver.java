package dev.xdark.jlinker;

import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

/// Resolves methods and fields according to the JVM specification (JVMS §5.4.3).
///
/// Callers supply an abstract view of the class hierarchy through [ClassModel],
/// [MethodModel], and [FieldModel]; this interface performs the resolution
/// algorithm on top of that model without depending on any particular bytecode
/// or reflection framework.
///
/// A single instance is stateless, cheap to create and thread-safe.
/// Every call performs a fresh traversal of the class hierarchy;
/// the implementation will never maintain internal state or caches.
/// Callers that need caching or other bookkeeping should build their own
/// wrapper around this interface.
///
/// ## Quick example
/// ```java
/// LinkResolver resolver = LinkResolver.create();
/// MyMethod m = resolver.resolveVirtualMethod(myClass, "toString",
///         MethodDescriptorString.of("()Ljava/lang/String;"));
/// ```
@NullMarked
public interface LinkResolver {

	/// Resolves a static field lookup (`getstatic` / `putstatic`).
	///
	/// Searches `refc`, its superclasses, and its superinterfaces in the order
	/// defined by JVMS §5.4.3.2.
	///
	/// @param refc       the class or interface that declares or inherits the field
	/// @param name       the simple field name
	/// @param descriptor the field descriptor
	/// @return the resolved field model
	/// @throws FieldResolutionException with [FieldResolutionViolation#NO_SUCH_FIELD]
	///                                  if the field cannot be found, or
	///                                  [FieldResolutionViolation#EXPECTED_STATIC_FIELD]
	///                                  if the resolved field is not static
	<F extends FieldModel> F resolveStaticField(
			ClassModel<?, ? extends F> refc,
			String name,
			FieldDescriptor descriptor
	) throws FieldResolutionException;

	/// Resolves an instance field lookup (`getfield` / `putfield`).
	///
	/// Searches `refc`, its superclasses, and its superinterfaces in the order
	/// defined by JVMS §5.4.3.2.
	///
	/// @param refc       the class or interface that declares or inherits the field
	/// @param name       the simple field name
	/// @param descriptor the field descriptor
	/// @return the resolved field model
	/// @throws FieldResolutionException with [FieldResolutionViolation#NO_SUCH_FIELD]
	///                                  if the field cannot be found, or
	///                                  [FieldResolutionViolation#EXPECTED_VIRTUAL_FIELD]
	///                                  if the resolved field is static
	<F extends FieldModel> F resolveVirtualField(
			ClassModel<?, ? extends F> refc,
			String name,
			FieldDescriptor descriptor
	) throws FieldResolutionException;

	/// Resolves a static method lookup (`invokestatic`).
	///
	/// Works for both class and interface reference classes, following JVMS §5.4.3.3
	/// and §5.4.3.4 respectively.
	///
	/// @param refc       the class or interface named in the instruction
	/// @param name       the method name
	/// @param descriptor the method descriptor
	/// @return the resolved method model
	/// @throws MethodResolutionException with [MethodResolutionViolation#NO_SUCH_METHOD]
	///                                   if the method cannot be found, or
	///                                   [MethodResolutionViolation#EXPECTED_STATIC_METHOD]
	///                                   if the resolved method is not static
	<M extends MethodModel> M resolveStaticMethod(
			ClassModel<? extends M, ?> refc,
			String name,
			MethodDescriptor descriptor
	) throws MethodResolutionException;

	/// Resolves a virtual method lookup (`invokevirtual`).
	///
	/// `refc` must be a class, not an interface (JVMS §5.4.3.3).  Default
	/// interface methods reachable through the class hierarchy are included.
	///
	/// @param refc       the class named in the instruction (must not be an interface)
	/// @param name       the method name
	/// @param descriptor the method descriptor
	/// @return the resolved method model
	/// @throws MethodResolutionException with [MethodResolutionViolation#EXPECTED_CLASS]
	///                                   if `refc` is an interface,
	///                                   [MethodResolutionViolation#NO_SUCH_METHOD] if
	///                                   the method cannot be found, or
	///                                   [MethodResolutionViolation#EXPECTED_VIRTUAL_METHOD]
	///                                   if the resolved method is static
	<M extends MethodModel> M resolveVirtualMethod(
			ClassModel<? extends M, ?> refc,
			String name,
			MethodDescriptor descriptor
	) throws MethodResolutionException;

	/// Resolves an interface method lookup (`invokeinterface`).
	///
	/// `refc` must be an interface (JVMS §5.4.3.4).
	///
	/// @param refc       the interface named in the instruction
	/// @param name       the method name
	/// @param descriptor the method descriptor
	/// @return the resolved method model
	/// @throws MethodResolutionException with [MethodResolutionViolation#EXPECTED_INTERFACE]
	///                                   if `refc` is not an interface, or
	///                                   [MethodResolutionViolation#NO_SUCH_METHOD] if
	///                                   the method cannot be found
	<M extends MethodModel> M resolveInterfaceMethod(
			ClassModel<? extends M, ?> refc,
			String name,
			MethodDescriptor descriptor
	) throws MethodResolutionException;

	/// Resolves a special method lookup (`invokespecial`).
	///
	/// Handles constructors, superclass calls, and private interface method calls.
	/// When `caller` is provided and `refc` is an interface, the caller must directly
	/// implement `refc`; indirect interface inheritance is rejected with
	/// [MethodResolutionViolation#INDIRECT_INTERFACE] (JVMS §5.4.3.3 / §5.4.3.4).
	///
	/// @param refc       the class or interface named in the instruction
	/// @param name       the method name (use `"<init>"` for constructors)
	/// @param descriptor the method descriptor
	/// @param caller     the class containing the `invokespecial` instruction,
	///                   used to validate interface method access; may be `null`
	///                   to skip that check
	/// @return the resolved method model
	/// @throws MethodResolutionException if resolution fails; the violation kind
	///                                   indicates the specific constraint that was
	///                                   violated
	<M extends MethodModel> M resolveSpecialMethod(
			ClassModel<? extends M, ?> refc,
			String name,
			MethodDescriptor descriptor,
			@Nullable ClassModel<? extends M, ?> caller
	) throws MethodResolutionException;

	/// Creates a new, stateless [LinkResolver] instance.
	static LinkResolver create() {
		return new LinkResolverImpl();
	}
}
