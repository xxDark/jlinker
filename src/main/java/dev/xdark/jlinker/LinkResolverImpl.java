package dev.xdark.jlinker;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Modifier;
import java.util.ArrayDeque;

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
		if (!Modifier.isStatic(refc.accessFlags())) {
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
	public <M extends MethodModel> @NotNull M resolveSpecialMethod(@NotNull ClassModel<M, ?> refc, @NotNull String name, @NotNull MethodDescriptor descriptor) throws MethodResolutionException {
		MethodLookupResult<M> result;
		if (!Modifier.isStatic(refc.accessFlags())) {
			result = resolveMethod(MethodResolutionType.SPECIAL, refc, name, descriptor);
		} else {
			result = resolveInterfaceMethod(MethodResolutionType.SPECIAL, refc, name, descriptor);
		}
		if ("<init>".equals(name) && result.refc != refc) {
			throw new MethodResolutionException(MethodResolutionViolation.NO_SUCH_METHOD);
		}
		// TODO compiler does not generate miranda methods anymore...
		// Should there be indirect interface check?
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
			result = null;
		}
		if (result == null) {
			return lookupDefaultMethod(refc, name, descriptor);
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
		var stack = new ArrayDeque<Iterable<? extends ClassModel<M, ?>>>();
		var interfaces = refc.interfaces();
		do {
			for (var iface : interfaces) {
				stack.push(iface.interfaces());
				M method;
				if ((method = iface.findMethod(name, descriptor)) == null)
					continue;
				int accessFlags = method.accessFlags();
				if (!Modifier.isPublic(accessFlags)) continue;
				if (Modifier.isStatic(accessFlags)) continue;
				if (!Modifier.isAbstract(accessFlags))
					return new MethodLookupResult<>(iface, method);
			}
		} while ((interfaces = stack.poll()) != null);
		return null;
	}

	@Nullable
	private static <M extends MethodModel> MethodLookupResult<M> lookupMethodInInterfaces(ClassModel<M, ?> refc, String name, MethodDescriptor descriptor) {
		var stack = new ArrayDeque<Iterable<? extends ClassModel<M, ?>>>();
		var interfaces = refc.interfaces();
		do {
			for (var iface : interfaces) {
				stack.push(iface.interfaces());
				M method;
				if ((method = iface.findMethod(name, descriptor)) == null)
					continue;
				int accessFlags = method.accessFlags();
				if (Modifier.isPublic(accessFlags) && !Modifier.isStatic(accessFlags)) {
					return new MethodLookupResult<>(iface, method);
				}
			}
		} while ((interfaces = stack.poll()) != null);
		return null;
	}

	private record MethodLookupResult<M extends MethodModel>(ClassModel<M, ?> refc, M method) {
	}

	private enum MethodResolutionType {
		VIRTUAL,
		STATIC,
		INTERFACE,
		SPECIAL,
	}
}
