package dev.xdark.jlinker;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.function.Predicate;

@NullMarked
final class LinkResolverImpl implements LinkResolver {

	@Override
	public <F extends FieldModel> F resolveStaticField(
			ClassModel<?, ? extends F> refc,
			String name,
			FieldDescriptor descriptor
	) throws FieldResolutionException {
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
	public <F extends FieldModel> F resolveVirtualField(
			ClassModel<?, ? extends F> refc,
			String name,
			FieldDescriptor descriptor
	) throws FieldResolutionException {
		var field = lookupField(refc, name, descriptor);
		if (field == null) {
			throw new FieldResolutionException(FieldResolutionViolation.NO_SUCH_FIELD);
		}
		if (Modifier.isStatic(field.accessFlags())) {
			throw new FieldResolutionException(FieldResolutionViolation.EXPECTED_VIRTUAL_FIELD);
		}
		return field;
	}

	@Override
	public <M extends MethodModel> M resolveStaticMethod(
			ClassModel<? extends M, ?> refc,
			String name,
			MethodDescriptor descriptor
	) throws MethodResolutionException {
		MethodLookupResult<? extends M> result;
		if (Modifier.isInterface(refc.accessFlags())) {
			result = resolveInterfaceMethodImpl(refc, name, descriptor);
		} else {
			result = resolveMethod(refc, name, descriptor);
		}
		var method = result.method;
		if (!Modifier.isStatic(method.accessFlags())) {
			throw new MethodResolutionException(MethodResolutionViolation.EXPECTED_STATIC_METHOD);
		}
		return method;
	}

	@Override
	public <M extends MethodModel> M resolveVirtualMethod(
			ClassModel<? extends M, ?> refc,
			String name,
			MethodDescriptor descriptor
	) throws MethodResolutionException {
		if (Modifier.isInterface(refc.accessFlags())) {
			throw new MethodResolutionException(MethodResolutionViolation.EXPECTED_CLASS);
		}
		var method = resolveMethod(refc, name, descriptor).method;
		if (Modifier.isStatic(method.accessFlags())) {
			throw new MethodResolutionException(MethodResolutionViolation.EXPECTED_VIRTUAL_METHOD);
		}
		return method;
	}

	@Override
	public <M extends MethodModel> M resolveInterfaceMethod(
			ClassModel<? extends M, ?> refc,
			String name,
			MethodDescriptor descriptor
	) throws MethodResolutionException {
		if (!Modifier.isInterface(refc.accessFlags())) {
			throw new MethodResolutionException(MethodResolutionViolation.EXPECTED_INTERFACE);
		}
		var method = resolveInterfaceMethodImpl(refc, name, descriptor).method;
		int accessFlags = method.accessFlags();
		if (Modifier.isStatic(accessFlags)) {
			throw new MethodResolutionException(MethodResolutionViolation.EXPECTED_VIRTUAL_METHOD);
		}
		if (Modifier.isPrivate(accessFlags)) {
			throw new MethodResolutionException(MethodResolutionViolation.REQUIRES_SPECIAL);
		}
		return method;
	}

	@Override
	public <M extends MethodModel> M resolveSpecialMethod(
			ClassModel<? extends M, ?> refc,
			String name,
			MethodDescriptor descriptor,
			@Nullable ClassModel<? extends M, ?> caller
	) throws MethodResolutionException {
		MethodLookupResult<? extends M> result;
		var isInterface = Modifier.isInterface(refc.accessFlags());
		if (isInterface) {
			result = resolveInterfaceMethodImpl(refc, name, descriptor);
		} else {
			result = resolveMethod(refc, name, descriptor);
		}
		var method = result.method;
		if (Modifier.isStatic(method.accessFlags())) {
			throw new MethodResolutionException(MethodResolutionViolation.EXPECTED_VIRTUAL_METHOD);
		}
		if ("<init>".equals(name) && !refc.equals(result.refc)) {
			throw new MethodResolutionException(MethodResolutionViolation.NO_SUCH_METHOD);
		}
		if (caller != null && isInterface && !isSameOrDirectInterface(caller, refc)) {
			throw new MethodResolutionException(MethodResolutionViolation.INDIRECT_INTERFACE);
		}
		return method;
	}

	private static <F extends FieldModel> @Nullable F lookupField(ClassModel<?, ? extends F> refc, String name,
	                                                              FieldDescriptor descriptor) {
		F field;
		do {
			if ((field = refc.findField(name, descriptor)) != null)
				return field;
			if ((field = lookupFieldInInterfaces(refc, name, descriptor)) != null)
				return field;
		} while ((refc = refc.superClass()) != null);
		return null;
	}

	private static <F extends FieldModel> @Nullable F lookupFieldInInterfaces(ClassModel<?, ? extends F> refc, String name,
	                                                                          FieldDescriptor descriptor) {
		for (var iface : refc.interfaces()) {
			F field;
			if ((field = iface.findField(name, descriptor)) != null)
				return field;
			if ((field = lookupFieldInInterfaces(iface, name, descriptor)) != null)
				return field;
		}
		return null;
	}

	private static <M extends MethodModel> MethodLookupResult<M> resolveMethod(
			ClassModel<M, ?> refc,
			String name,
			MethodDescriptor descriptor
	) throws MethodResolutionException {
		// assert !Modifier.isInterface(refc.accessFlags());
		var result = lookupMethodInClasses(refc, name, descriptor, false);
		if (result == null) {
			result = lookupMethodInInterfaces(refc, name, descriptor);
		}
		if (result == null) {
			throw new MethodResolutionException(MethodResolutionViolation.NO_SUCH_METHOD);
		}
		return result;
	}

	private static <M extends MethodModel> MethodLookupResult<M> resolveInterfaceMethodImpl(
			ClassModel<M, ?> refc,
			String name,
			MethodDescriptor descriptor
	) throws MethodResolutionException {
		// assert Modifier.isInterface(refc.accessFlags());
		var result = lookupMethodInClasses(refc, name, descriptor, true);
		if (result == null) {
			result = lookupMethodInInterfaces(refc, name, descriptor);
		}
		if (result == null) {
			throw new MethodResolutionException(MethodResolutionViolation.NO_SUCH_METHOD);
		}
		return result;
	}

	private static <M extends MethodModel> @Nullable MethodLookupResult<M> lookupMethodInClasses(
			ClassModel<M, ?> refc,
			String name,
			MethodDescriptor descriptor,
			boolean isInterface
	) throws MethodResolutionException {
		var uncachedLookup = uncachedLookupMethod(refc, name, descriptor);
		M result;
		if (uncachedLookup != null) {
			result = uncachedLookup.method;
		} else {
			result = null;
		}
		int flags;
		if (isInterface &&
				result != null &&
				(Modifier.isStatic(flags = result.accessFlags()) || !Modifier.isPublic(flags) || Modifier.isAbstract(flags)) &&
				"java/lang/Object".equals(uncachedLookup.refc.name())) {
			uncachedLookup = null;
		}
		if (uncachedLookup == null) {
			uncachedLookup = lookupDefaultMethod(refc, name, descriptor);
		}
		return uncachedLookup;
	}

	private static <M extends MethodModel> @Nullable MethodLookupResult<M> uncachedLookupMethod(ClassModel<M, ?> refc,
	                                                                                            String name, MethodDescriptor descriptor) {
		do {
			M method;
			if ((method = refc.findMethod(name, descriptor)) != null)
				return new MethodLookupResult<>(refc, method);
		} while ((refc = refc.superClass()) != null);
		return null;
	}

	private static <M extends MethodModel> @Nullable MethodLookupResult<M> lookupDefaultMethod(ClassModel<M, ?> refc, String name,
	                                                                                           MethodDescriptor descriptor) throws MethodResolutionException {
		return lookupMethodInInterfaces(refc, name, descriptor, m -> !Modifier.isAbstract(m.accessFlags()));
	}

	private static <M extends MethodModel> @Nullable MethodLookupResult<M> lookupMethodInInterfaces(ClassModel<M, ?> refc,
	                                                                                                String name, MethodDescriptor descriptor) throws MethodResolutionException {
		return lookupMethodInInterfaces(refc, name, descriptor, __ -> true);
	}

	private static <M extends MethodModel> @Nullable MethodLookupResult<M> lookupMethodInInterfaces(
			ClassModel<M, ?> refc, String name, MethodDescriptor descriptor,
			Predicate<? super M> tester) throws MethodResolutionException {

		var candidates = new ArrayList<MethodLookupResult<M>>();
		var visited = new HashSet<ClassModel<M, ?>>();
		var queue = new ArrayDeque<ClassModel<M, ?>>();

		var c = refc;
		do {
			for (var iface : c.interfaces())
				if (visited.add(iface)) queue.add(iface);
		} while ((c = c.superClass()) != null);

		ClassModel<M, ?> iface;
		while ((iface = queue.poll()) != null) {
			for (var parent : iface.interfaces())
				if (visited.add(parent)) queue.add(parent);

			M method = iface.findMethod(name, descriptor);
			if (method == null) continue;
			int flags = method.accessFlags();
			if (!Modifier.isPublic(flags) || Modifier.isStatic(flags)) continue;
			if (!tester.test(method)) continue;
			candidates.add(new MethodLookupResult<>(iface, method));
		}

		if (candidates.isEmpty()) return null;
		if (candidates.size() == 1) return candidates.get(0);

		var maximal = new ArrayList<MethodLookupResult<M>>(candidates.size());
		var isStrictSubinterface = new IsStrictSubinterface();
		for (var candidate : candidates) {
			var dominated = false;
			for (var other : candidates) {
				if (other != candidate && isStrictSubinterface.wellIsIt(other.refc(), candidate.refc())) {
					dominated = true;
					break;
				}
			}
			if (!dominated) maximal.add(candidate);
		}

		MethodLookupResult<M> result = null;
		MethodLookupResult<M> abstract_ = null;
		for (var candidate : maximal) {
			if (Modifier.isAbstract(candidate.method.accessFlags())) {
				abstract_ = candidate;
				continue;
			}
			if (result == null) {
				result = candidate;
				continue;
			}
			throw new MethodResolutionException(MethodResolutionViolation.AMBIGUOUS_METHOD);
		}
		return result != null ? result : abstract_;
	}

	private static final class IsStrictSubinterface extends ArrayDeque<ClassModel<?, ?>> {
		IsStrictSubinterface() {
			super(4);
		}

		boolean wellIsIt(ClassModel<?, ?> sub, ClassModel<?, ?> sup) {
			clear();
			for (var parent : sub.interfaces()) push(parent);
			ClassModel<?, ?> cur;
			while ((cur = poll()) != null) {
				if (cur.equals(sup)) return true;
				for (var parent : cur.interfaces()) push(parent);
			}
			return false;
		}
	}

	private static boolean isSameOrDirectInterface(ClassModel<?, ?> me, ClassModel<?, ?> that) {
		if (me.equals(that)) return true;
		for (var iface : me.interfaces()) {
			if (iface.equals(that)) return true;
		}
		return false;
	}

	private record MethodLookupResult<M extends MethodModel>(ClassModel<M, ?> refc, M method) {
	}
}
