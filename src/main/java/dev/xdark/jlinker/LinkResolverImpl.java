package dev.xdark.jlinker;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.function.Predicate;

final class LinkResolverImpl implements LinkResolver {

	@Override
	public <F extends FieldModel> @NotNull F resolveStaticField(@NotNull ClassModel<?, F> refc, @NotNull String name, @NotNull FieldDescriptor descriptor) throws FieldResolutionException {
		var field = lookupField(refc, name, descriptor);
		if (field == null) {
			throw new FieldResolutionException(FieldResolutionViolation.NO_SUCH_FIELD);
		}
		if (!Modifier.isStatic(field.accessFlags())) {
			throw new FieldResolutionException(FieldResolutionViolation.EXPECTED_STATIC_FIELD);
		}
		return field;
	}

	@Override
	public <F extends FieldModel> @NotNull F resolveVirtualField(@NotNull ClassModel<?, F> refc, @NotNull String name, @NotNull FieldDescriptor descriptor) throws FieldResolutionException {
		var field = lookupField(refc, name, descriptor);
		if (field == null) {
			throw new FieldResolutionException(FieldResolutionViolation.NO_SUCH_FIELD);
		}
		if (Modifier.isStatic(field.accessFlags())) {
			throw new FieldResolutionException(FieldResolutionViolation.EXPECTED_VIRTUAL_FIELD);
		}
		return field;
	}

	private static <F extends FieldModel> F lookupField(ClassModel<?, F> refc, String name, FieldDescriptor descriptor) {
		F field;
		if ((field = refc.findField(name, descriptor)) != null)
			return field;
		if ((field = lookupFieldInInterfaces(refc, name, descriptor)) != null)
			return field;
		if ((refc = refc.superClass()) != null)
			return lookupField(refc, name, descriptor);
		return null;
	}

	private static <F extends FieldModel> F lookupFieldInInterfaces(ClassModel<?, F> refc, String name, FieldDescriptor descriptor) {
		for (var iface : refc.interfaces()) {
			F field;
			if ((field = iface.findField(name, descriptor)) != null)
				return field;
			if ((field = lookupFieldInInterfaces(iface, name, descriptor)) != null)
				return field;
		}
		return null;
	}

	@Override
	public <M extends MethodModel> @NotNull M resolveStaticMethod(@NotNull ClassModel<M, ?> refc, @NotNull String name, @NotNull MethodDescriptor descriptor) throws MethodResolutionException {
		MethodLookupResult<M> result;
		if (!Modifier.isInterface(refc.accessFlags())) {
			result = resolveMethod(MethodResolutionType.STATIC, refc, name, descriptor);
		} else {
			result = resolveInterfaceMethod(MethodResolutionType.STATIC, refc, name, descriptor);
		}
		var method = result.method;
		if (!Modifier.isStatic(method.accessFlags())) {
			throw new MethodResolutionException(MethodResolutionViolation.EXPECTED_STATIC_METHOD);
		}
		return method;
	}

	@Override
	public <M extends MethodModel> @NotNull M resolveVirtualMethod(@NotNull ClassModel<M, ?> refc, @NotNull String name, @NotNull MethodDescriptor descriptor) throws MethodResolutionException {
		var result = resolveMethod(MethodResolutionType.VIRTUAL, refc, name, descriptor);
		var method = result.method;
		if (Modifier.isInterface(refc.accessFlags()) && Modifier.isPrivate(method.accessFlags())) {
			throw new MethodResolutionException(MethodResolutionViolation.REQUIRES_SPECIAL);
		}
		if (Modifier.isStatic(method.accessFlags())) {
			throw new MethodResolutionException(MethodResolutionViolation.EXPECTED_VIRTUAL_METHOD);
		}
		return method;
	}

	@Override
	public <M extends MethodModel> @NotNull M resolveInterfaceMethod(@NotNull ClassModel<M, ?> refc, @NotNull String name, @NotNull MethodDescriptor descriptor) throws MethodResolutionException {
		var result = resolveInterfaceMethod(MethodResolutionType.INTERFACE, refc, name, descriptor);
		return result.method;
	}

	@Override
	public <M extends MethodModel> @NotNull M resolveSpecialMethod(@NotNull ClassModel<M, ?> refc, @NotNull String name, @NotNull MethodDescriptor descriptor, @Nullable ClassModel<M, ?> caller) throws MethodResolutionException {
		MethodLookupResult<M> result;
		if (!Modifier.isInterface(refc.accessFlags())) {
			result = resolveMethod(MethodResolutionType.SPECIAL, refc, name, descriptor);
		} else {
			result = resolveInterfaceMethod(MethodResolutionType.SPECIAL, refc, name, descriptor);
		}
		if ("<init>".equals(name) && result.refc != refc) {
			throw new MethodResolutionException(MethodResolutionViolation.NO_SUCH_METHOD);
		}
		if (caller != null && Modifier.isInterface(refc.accessFlags())) {
			if (!isSameOrDirectInterface(caller, refc)) {
				throw new MethodResolutionException(MethodResolutionViolation.INDIRECT_INTERFACE);
			}
		}
		var method = result.method;
		if (Modifier.isStatic(method.accessFlags())) {
			throw new MethodResolutionException(MethodResolutionViolation.EXPECTED_VIRTUAL_METHOD);
		}
		return method;
	}

	@NotNull
	private static <M extends MethodModel> MethodLookupResult<M> resolveMethod(MethodResolutionType resolutionType, ClassModel<M, ?> refc, String name, MethodDescriptor descriptor) throws MethodResolutionException {
		if (resolutionType == MethodResolutionType.VIRTUAL && Modifier.isInterface(refc.accessFlags())) {
			throw new MethodResolutionException(MethodResolutionViolation.EXPECTED_CLASS);
		}
		var result = lookupMethodInClasses(refc, name, descriptor, false);
		if (result == null) {
			result = lookupMethodInInterfaces(refc, name, descriptor);
		}
		if (result == null) {
			throw new MethodResolutionException(MethodResolutionViolation.NO_SUCH_METHOD);
		}
		return result;
	}

	@NotNull
	private static <M extends MethodModel> MethodLookupResult<M> resolveInterfaceMethod(MethodResolutionType resolutionType, ClassModel<M, ?> refc, String name, MethodDescriptor descriptor) throws MethodResolutionException {
		if (!Modifier.isInterface(refc.accessFlags())) {
			throw new MethodResolutionException(MethodResolutionViolation.EXPECTED_INTERFACE);
		}
		var result = lookupMethodInClasses(refc, name, descriptor, true);
		if (result == null) {
			result = lookupMethodInInterfaces(refc, name, descriptor);
		}
		if (result == null) {
			throw new MethodResolutionException(MethodResolutionViolation.NO_SUCH_METHOD);
		}
		if (resolutionType != MethodResolutionType.STATIC && Modifier.isStatic(result.method.accessFlags())) {
			throw new MethodResolutionException(MethodResolutionViolation.EXPECTED_VIRTUAL_METHOD);
		}
		return result;
	}

	@Nullable
	private static <M extends MethodModel> MethodLookupResult<M> lookupMethodInClasses(
			ClassModel<M, ?> refc,
			String name,
			MethodDescriptor descriptor,
			boolean inResolve
	) {
		var uncachedLookup = uncachedLookupMethod(refc, name, descriptor);
		M result;
		if (uncachedLookup != null) {
			result = uncachedLookup.method;
		} else {
			result = null;
		}
		if (inResolve &&
				result != null &&
				Modifier.isInterface(refc.accessFlags()) &&
				(Modifier.isStatic(result.accessFlags()) || !Modifier.isPublic(result.accessFlags())) &&
				"java/lang/Object".equals(uncachedLookup.refc.name())) {
			uncachedLookup = null;
		}
		if (uncachedLookup == null) {
			uncachedLookup = lookupDefaultMethod(refc, name, descriptor);
		}
		return uncachedLookup;
	}

	@Nullable
	private static <M extends MethodModel> MethodLookupResult<M> uncachedLookupMethod(ClassModel<M, ?> refc, String name, MethodDescriptor descriptor) {
		do {
			M method;
			if ((method = refc.findMethod(name, descriptor)) != null)
				return new MethodLookupResult<>(refc, method);
		} while ((refc = refc.superClass()) != null);
		return null;
	}

	@Nullable
	private static <M extends MethodModel> MethodLookupResult<M> lookupDefaultMethod(ClassModel<M, ?> refc, String name, MethodDescriptor descriptor) {
		return lookupMethodInInterfaces(refc, name, descriptor, m -> !Modifier.isAbstract(m.accessFlags()));
	}

	@Nullable
	private static <M extends MethodModel> MethodLookupResult<M> lookupMethodInInterfaces(ClassModel<M, ?> refc, String name, MethodDescriptor descriptor) {
		return lookupMethodInInterfaces(refc, name, descriptor, __ -> true);
	}

	@Nullable
	private static <M extends MethodModel> MethodLookupResult<M> lookupMethodInInterfaces(ClassModel<M, ?> refc, String name, MethodDescriptor descriptor, Predicate<? super M> tester) {
		var stack = new ArrayDeque<Iterable<? extends ClassModel<M, ?>>>();
		do {
			stack.push(refc.interfaces());
		} while ((refc = refc.superClass()) != null);
		Iterable<? extends ClassModel<M, ?>> interfaces;
		while ((interfaces = stack.poll()) != null) {
			for (var iface : interfaces) {
				stack.push(iface.interfaces());
				M method;
				if ((method = iface.findMethod(name, descriptor)) == null)
					continue;
				int accessFlags = method.accessFlags();
				if (!Modifier.isPublic(accessFlags)) continue;
				if (Modifier.isStatic(accessFlags)) continue;
				if (!tester.test(method))
					continue;
				return new MethodLookupResult<>(iface, method);
			}
		}
		return null;
	}

	private static boolean isSameOrDirectInterface(ClassModel<?, ?> me, ClassModel<?, ?> that) {
		if (me == that) return true;
		for (var iface : me.interfaces()) {
			if (iface == that) return true;
		}
		return false;
	}

	private record MethodLookupResult<M extends MethodModel>(@NotNull ClassModel<M, ?> refc, @NotNull M method) {
	}

	private enum MethodResolutionType {
		VIRTUAL,
		STATIC,
		INTERFACE,
		SPECIAL,
	}
}
