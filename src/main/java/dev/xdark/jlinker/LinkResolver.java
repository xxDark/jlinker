package dev.xdark.jlinker;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface LinkResolver {

	@NotNull
	@Contract(pure = true)
	<F extends FieldModel> F resolveStaticField(
			@NotNull ClassModel<?, F> refc,
			@NotNull String name,
			@NotNull FieldDescriptor descriptor
	) throws FieldResolutionException;

	@NotNull
	@Contract(pure = true)
	<F extends FieldModel> F resolveVirtualField(
			@NotNull ClassModel<?, F> refc,
			@NotNull String name,
			@NotNull FieldDescriptor descriptor
	) throws FieldResolutionException;

	@NotNull
	@Contract(pure = true)
	<M extends MethodModel> M resolveStaticMethod(
			@NotNull ClassModel<M, ?> refc,
			@NotNull String name,
			@NotNull MethodDescriptor descriptor
	) throws MethodResolutionException;

	@NotNull
	@Contract(pure = true)
	<M extends MethodModel> M resolveVirtualMethod(
			@NotNull ClassModel<M, ?> refc,
			@NotNull String name,
			@NotNull MethodDescriptor descriptor
	) throws MethodResolutionException;

	@NotNull
	@Contract(pure = true)
	<M extends MethodModel> M resolveInterfaceMethod(
			@NotNull ClassModel<M, ?> refc,
			@NotNull String name,
			@NotNull MethodDescriptor descriptor
	) throws MethodResolutionException;

	@NotNull
	@Contract(pure = true)
	<M extends MethodModel> M resolveSpecialMethod(
			@NotNull ClassModel<M, ?> refc,
			@NotNull String name,
			@NotNull MethodDescriptor descriptor,
			@Nullable ClassModel<M, ?> caller
	) throws MethodResolutionException;

	@NotNull
	@Contract(pure = true)
	static LinkResolver create() {
		return new LinkResolverImpl();
	}
}
