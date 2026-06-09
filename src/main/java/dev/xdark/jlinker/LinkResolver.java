package dev.xdark.jlinker;

import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;

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
/// Polymorphic signatures — methods annotated with `@PolymorphicSignature`
/// whose descriptor the JVM synthesizes to match each call site, such as
/// [MethodHandle#invoke(Object...)] and [VarHandle#get(Object...)] — are not handled by the resolver.
/// Callers that need to resolve such methods must forge them on the fly and
/// make them available through their [ClassModel] implementation.
///
/// The resolver performs maximally-specific selection of default interface
/// methods: when multiple candidates exist, it picks the most specific one.
/// If more than one non-abstract candidate remains at the same specificity
/// level, resolution fails with
/// [MethodResolutionViolation#AMBIGUOUS_METHOD].
///
/// Overpass (miranda) methods — synthetic methods the JVM generates at class
/// loading time (previously done by the compiler) to bridge inherited default interface method implementations
/// into concrete classes — are not synthesized by the resolver.
/// If callers need these methods to be visible during resolution, they must
/// generate them and expose them through their [ClassModel] implementation.
///
/// ## Miranda stub generation algorithm
///
/// The procedure is a **fixpoint loop**: adding a stub to one class may
/// unblock resolution in another, so we iterate until no further progress
/// is made.
///
/// ```
/// function generateMirandaStubs(classes):
///   repeat
///     progress ← false
///     deferred ← false
///
///     for each class C in classes
///       for each ancestor A of C          // walk supers + interfaces transitively
///         for each method M declared in A
///           if M is static, private, native, or a constructor → skip
///           if C already declares a method with M's signature  → skip
///
///           R ← resolve M from C          // interface or virtual resolution
///
///           if resolution fails with INDIRECT_INTERFACE
///             deferred ← true → skip      // retry once more stubs exist
///
///           create method M' on C with R's access flags and M's descriptor
///           if R is not abstract
///             M'.body ← [load this,
///                         load each parameter,
///                         invokespecial R,
///                         return]
///           progress ← true
///
///     if deferred and not progress
///       error "cycle or unresolvable dependency detected"
///   until not progress
/// ```
///
/// ### Termination guarantees
///
/// Each iteration must generate at least one new stub (`progress`) or the
/// loop exits. If work was deferred but nothing was generated, a dependency
/// cycle exists and the algorithm throws rather than looping forever.
///
/// ### Stub body
///
/// Non-abstract stubs are trivial trampolines: they load `this` and
/// every parameter onto the operand stack, perform an `invokeexact`
/// to the resolved target, and return the result. Abstract stubs are
/// left body-less.
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
