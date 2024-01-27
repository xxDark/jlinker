package dev.xdark.jlinker;

import org.jetbrains.annotations.NotNull;

/**
 * Runtime resolver.
 *
 * @author xDark
 */
public interface RuntimeResolver {

	/**
	 * Resolves static method.
	 *
	 * @param owner      Method owner.
	 * @param name       Method name.
	 * @param descriptor Method descriptor.
	 * @param itf        Whether the owner is an interface class.
	 * @return Resolution result.
	 */
	@NotNull Result<MethodInfo> resolveStaticMethod(@NotNull ClassInfo owner, @NotNull String name, @NotNull MethodDescriptor descriptor, boolean itf);

	/**
	 * Resolves virtual method.
	 *
	 * @param owner      Method owner.
	 * @param name       Method name.
	 * @param descriptor Method descriptor.
	 * @return Resolution result.
	 */
	@NotNull Result<MethodInfo> resolveVirtualMethod(@NotNull ClassInfo owner, @NotNull String name, @NotNull MethodDescriptor descriptor);

	/**
	 * Resolves interface method.
	 *
	 * @param owner      Method owner.
	 * @param name       Method name.
	 * @param descriptor Method descriptor.
	 * @return Resolution result.
	 */
	@NotNull Result<MethodInfo> resolveInterfaceMethod(@NotNull ClassInfo owner, @NotNull String name, @NotNull MethodDescriptor descriptor);

	/**
	 * Resolves static field.
	 *
	 * @param owner      Field owner.
	 * @param name       Field name.
	 * @param descriptor Field descriptor.
	 * @return Resolution result.
	 */
	@NotNull Result<FieldInfo> resolveStaticField(@NotNull ClassInfo owner, @NotNull String name, @NotNull FieldDescriptor descriptor);

	/**
	 * Resolves virtual field.
	 *
	 * @param owner      Field owner.
	 * @param name       Field name.
	 * @param descriptor Field descriptor.
	 * @return Resolution result.
	 */
	@NotNull Result<FieldInfo> resolveVirtualField(@NotNull ClassInfo owner, @NotNull String name, @NotNull FieldDescriptor descriptor);

	/**
	 * @param linkResolver JVM link resolver.
	 * @return JVM runtime resolver.
	 * @throws IllegalArgumentException if provided linker is not a JVM linker.
	 */
	static RuntimeResolver jvm(LinkResolver linkResolver) {
		if (!(linkResolver instanceof JVMLinkResolver)) {
			throw new IllegalArgumentException("Not a JVM link resolver");
		}
		return new JVMRuntimeResolver(linkResolver);
	}
}
