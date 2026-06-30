package dev.xdark.jlinker;

import dev.xdark.jlinker.runtime.Module;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Modifier;

/// An abstract representation of a Java class or interface used by [LinkResolver].
///
/// Implementations describe a class's hierarchy, access modifiers, and member
/// lookup without coupling the resolver to any specific bytecode library or
/// reflection API. Implement this interface for your own class representation
/// and pass instances to [LinkResolver] methods.
///
/// ## Implementation notes
/// - [#name()] must return the **internal binary name** (slash-separated), e.g.
///   `"java/lang/String"`.
/// - [#accessFlags()] must return the raw `ACC_*` flags as defined by the class
///   file format; the resolver uses [Modifier] constants to
///   test them.
/// - [#findMethod] and [#findField] should search only the **directly declared**
///   members of this class; the resolver handles inheritance traversal itself.
///
/// @param <METHOD> the concrete method model type
/// @param <FIELD>  the concrete field model type
@NullMarked
public interface ClassModel<METHOD extends MethodModel, FIELD extends FieldModel> {

	/// Returns the module that contains this class, used for module-aware resolution.
	Module module();

	/// Returns the internal binary name of this class (slash-separated),
	/// e.g. `"java/lang/String"`.
	String name();

	/// Returns the raw access flags of this class as defined by the class file format.
	///
	/// The resolver uses [Modifier] constants (e.g.
	/// [Modifier#isInterface(int)]) to interpret these flags.
	int accessFlags();

	/// Returns the direct superclass of this class, or `null` for `java/lang/Object`.
	///
	/// Interfaces are a deliberate special case. In the class file an interface's
	/// `super_class` index is `0`, but this method **must** return the
	/// `java/lang/Object` model for an interface — not `null`. The resolver relies
	/// on this to expose [Object]'s public instance methods during interface method
	/// resolution (JVMS §5.4.3.4); see [LinkResolver] for the full rationale.
	@Nullable ClassModel<METHOD, FIELD> superClass();

	/// Returns the directly implemented or extended interfaces of this class.
	Iterable<? extends ClassModel<METHOD, FIELD>> interfaces();

	/// Looks up a method declared **directly** on this class.
	///
	/// The resolver calls this while traversing the hierarchy; implementations
	/// must **not** walk superclasses or interfaces themselves.
	///
	/// @param name       the simple method name
	/// @param descriptor the method descriptor
	/// @return the method model, or `null` if not found on this class
	@Nullable METHOD findMethod(String name, MethodDescriptor descriptor);

	/// Looks up a field declared **directly** on this class.
	///
	/// The resolver calls this while traversing the hierarchy; implementations
	/// must **not** walk superclasses or interfaces themselves.
	///
	/// @param name       the simple field name
	/// @param descriptor the field descriptor
	/// @return the field model, or `null` if not found on this class
	@Nullable FIELD findField(String name, FieldDescriptor descriptor);
}
