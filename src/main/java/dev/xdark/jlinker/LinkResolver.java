package dev.xdark.jlinker;

import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface LinkResolver {

	<F extends FieldModel> F resolveStaticField(
			ClassModel<?, ? extends F> refc,
			String name,
			FieldDescriptor descriptor
	) throws FieldResolutionException;

	<F extends FieldModel> F resolveVirtualField(
			ClassModel<?, ? extends F> refc,
			String name,
			FieldDescriptor descriptor
	) throws FieldResolutionException;

	<M extends MethodModel> M resolveStaticMethod(
			ClassModel<? extends M, ?> refc,
			String name,
			MethodDescriptor descriptor
	) throws MethodResolutionException;

	<M extends MethodModel> M resolveVirtualMethod(
			ClassModel<? extends M, ?> refc,
			String name,
			MethodDescriptor descriptor
	) throws MethodResolutionException;

	<M extends MethodModel> M resolveInterfaceMethod(
			ClassModel<? extends M, ?> refc,
			String name,
			MethodDescriptor descriptor
	) throws MethodResolutionException;

	<M extends MethodModel> M resolveSpecialMethod(
			ClassModel<? extends M, ?> refc,
			String name,
			MethodDescriptor descriptor,
			@Nullable ClassModel<? extends M, ?> caller
	) throws MethodResolutionException;

	static LinkResolver create() {
		return new LinkResolverImpl();
	}
}
