package dev.xdark.jlinker;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Modifier;

/**
 * Link time resolver.
 *
 * @author xDark
 */
public interface LinkResolver {

	/**
	 * Resolves static method.
	 *
	 * @param owner      Method owner.
	 * @param name       Method name.
	 * @param descriptor Method descriptor.
	 * @param itf        Whether the owner is an interface class.
	 * @return Resolution result.
	 */
	Result<MethodInfo> resolveStaticMethod(@NotNull ClassInfo owner, @NotNull String name, @NotNull MethodDescriptor descriptor, boolean itf);

	/**
	 * Resolves static method.
	 *
	 * @param owner      Method owner.
	 * @param name       Method name.
	 * @param descriptor Method descriptor.
	 * @return Resolution result.
	 */
	default Result<MethodInfo> resolveStaticMethod(@NotNull ClassInfo owner, @NotNull String name, @NotNull MethodDescriptor descriptor) {
		return resolveStaticMethod(owner, name, descriptor, Modifier.isInterface(owner.getAccessFlags()));
	}

	/**
	 * Resolves special method.
	 *
	 * @param owner      Method owner.
	 * @param name       Method name.
	 * @param descriptor Method descriptor.
	 * @param itf        Whether the owner is an interface class.
	 * @return Resolution result.
	 */
	Result<MethodInfo> resolveSpecialMethod(@NotNull ClassInfo owner, @NotNull String name, @NotNull MethodDescriptor descriptor, boolean itf);

	/**
	 * Resolves special method.
	 *
	 * @param owner      Method owner.
	 * @param name       Method name.
	 * @param descriptor Method descriptor.
	 * @return Resolution result.
	 */
	default Result<MethodInfo> resolveSpecialMethod(@NotNull ClassInfo owner, @NotNull String name, @NotNull MethodDescriptor descriptor) {
		return resolveSpecialMethod(owner, name, descriptor, Modifier.isInterface(owner.getAccessFlags()));
	}

	/**
	 * Resolves virtual method.
	 *
	 * @param owner      Method owner.
	 * @param name       Method name.
	 * @param descriptor Method descriptor.
	 * @return Resolution result.
	 */
	Result<MethodInfo> resolveVirtualMethod(@NotNull ClassInfo owner, @NotNull String name, @NotNull MethodDescriptor descriptor);

	/**
	 * Resolves interface method.
	 *
	 * @param owner      Method owner.
	 * @param name       Method name.
	 * @param descriptor Method descriptor.
	 * @return Resolution result.
	 */
	Result<MethodInfo> resolveInterfaceMethod(@NotNull ClassInfo owner, @NotNull String name, @NotNull MethodDescriptor descriptor);

	/**
	 * Resolves static field.
	 *
	 * @param owner      Field owner.
	 * @param name       Field name.
	 * @param descriptor Field descriptor.
	 * @return Resolution result.
	 */
	Result<FieldInfo> resolveStaticField(@NotNull ClassInfo owner, @NotNull String name, @NotNull FieldDescriptor descriptor);

	/**
	 * Resolves virtual field.
	 *
	 * @param owner      Field owner.
	 * @param name       Field name.
	 * @param descriptor Field descriptor.
	 * @return Resolution result.
	 */
	Result<FieldInfo> resolveVirtualField(@NotNull ClassInfo owner, @NotNull String name, @NotNull FieldDescriptor descriptor);

	/**
	 * @return JVM link resolver.
	 */
	static LinkResolver jvm() {
		return new JVMLinkResolver();
	}
}
