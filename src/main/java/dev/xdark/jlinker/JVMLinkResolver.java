package dev.xdark.jlinker;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.Deque;

final class JVMLinkResolver implements LinkResolver {

	@Override
	public @NotNull Result<MethodInfo> resolveVirtualMethod(@NotNull ClassInfo info, @NotNull String name, @NotNull MethodDescriptor type) {
		if (Modifier.isInterface(info.getAccessFlags())) {
			return Error.of(FailureReason.ACC_INTERFACE_SET);
		}
		MethodInfo method = uncachedLookupMethod(info, name, type);
		if (method == null) {
			method = uncachedInterfaceMethod(info, name, type);
		}
		return checkVirtualMethod(method);
	}

	@Override
	public @NotNull Result<MethodInfo> resolveStaticMethod(@NotNull ClassInfo info, @NotNull String name, @NotNull MethodDescriptor type, boolean itf) {
		MethodInfo method;
		if (itf) {
			if (!Modifier.isInterface(info.getAccessFlags())) {
				return Error.of(FailureReason.ACC_INTERFACE_UNSET);
			}
			method = uncachedInterfaceMethod(info, name, type);
		} else {
			method = uncachedLookupMethod(info, name, type);
		}
		if (method == null) {
			return Error.of(FailureReason.NO_SUCH_METHOD);
		}
		if (!Modifier.isStatic(method.getAccessFlags())) {
			return Error.of(FailureReason.ACC_STATIC_UNSET);
		}
		if (itf != Modifier.isInterface(method.getOwner().getAccessFlags())) {
			return Error.of(FailureReason.ACC_INTERFACE_UNSET);
		}
		return new Success<>(method);
	}

	@Override
	public @NotNull Result<MethodInfo> resolveSpecialMethod(@NotNull ClassInfo info, @NotNull String name, @NotNull MethodDescriptor type, boolean itf) {
		if (Modifier.isInterface(info.getAccessFlags()) != itf) {
			return Error.of(itf ? FailureReason.ACC_INTERFACE_UNSET : FailureReason.ACC_INTERFACE_SET);
		}
		MethodInfo method;
		if (itf || (method = uncachedLookupMethod(info, name, type)) == null) {
			method = uncachedInterfaceMethod(info, name, type);
		}
		return checkVirtualMethod(method);
	}

	@Override
	public @NotNull Result<MethodInfo> resolveInterfaceMethod(@NotNull ClassInfo info, @NotNull String name, @NotNull MethodDescriptor type) {
		if (!Modifier.isInterface(info.getAccessFlags())) {
			return Error.of(FailureReason.ACC_INTERFACE_UNSET);
		}
		MethodInfo method = uncachedInterfaceMethod(info, name, type);
		if (method == null) {
			return Error.of(FailureReason.NO_SUCH_METHOD);
		}
		if (Modifier.isStatic(method.getAccessFlags())) {
			return Error.of(FailureReason.ACC_STATIC_SET);
		}
		return new Success<>(method);
	}

	@Override
	public @NotNull Result<FieldInfo> resolveStaticField(@NotNull ClassInfo owner, @NotNull String name, @NotNull FieldDescriptor type) {
		ClassInfo info = owner;
		FieldInfo field = null;
		while (owner != null) {
			field = owner.getField(name, type);
			if (field != null) {
				info = owner;
				break;
			}
			owner = owner.getSuperclass();
		}
		if (field == null) {
			field = uncachedInterfaceLookup(info, name, type, false, ClassInfo::getField);
		}
		if (field == null) {
			return Error.of(FailureReason.NO_SUCH_FIELD);
		}
		if (!Modifier.isStatic(field.getAccessFlags())) {
			return Error.of(FailureReason.ACC_STATIC_UNSET);
		}
		return new Success<>(field);
	}

	@Override
	public @NotNull Result<FieldInfo> resolveVirtualField(@NotNull ClassInfo info, @NotNull String name, @NotNull FieldDescriptor type) {
		while (info != null) {
			FieldInfo field = info.getField(name, type);
			if (field != null) {
				if (!Modifier.isStatic(field.getAccessFlags())) {
					return new Success<>(field);
				}
				return Error.of(FailureReason.ACC_STATIC_SET);
			}
			info = info.getSuperclass();
		}
		return Error.of(FailureReason.NO_SUCH_FIELD);
	}

	@Nullable
	MethodInfo uncachedLookupMethod(ClassInfo owner, String name, MethodDescriptor descriptor) {
		do {
			MethodInfo method = owner.getMethod(name, descriptor);
			if (method != null)
				return method;
		} while ((owner = owner.getSuperclass()) != null);
		return null;
	}

	MethodInfo uncachedInterfaceMethod(ClassInfo owner, String name, MethodDescriptor descriptor) {
		ClassInfo info = owner;
		MethodInfo resolution = uncachedInterfaceLookup(owner, name, descriptor, true, ClassInfo::getMethod);
		if (resolution != null) {
			return resolution;
		}
		// We have corner case when we have an interface
		// that looks like that:
		// interface Foo { int hashCode(); }
		info = info.getSuperclass();
		while (info != null) {
			ClassInfo superClass = info.getSuperclass();
			if (superClass == null) {
				break;
			}
			info = superClass;
		}
		// Null in case info is java/lang/Object or an annotation (?), apparently
		if (info == null) {
			return null;
		}
		MethodInfo method = info.getMethod(name, descriptor);
		if (method != null) {
			int accessFlags = method.getAccessFlags();
			if (Modifier.isStatic(accessFlags) || !Modifier.isPublic(accessFlags)) {
				method = null;
			}
		}
		return method;
	}

	@Nullable
	private <V extends MemberInfo, T extends Descriptor> V uncachedInterfaceLookup(ClassInfo info, String name, T desc, boolean guessAbstract, UncachedResolve<V, T> resolve) {
		V guess = null;
		Deque<ClassInfo> queue = new ArrayDeque<>();
		do {
			if (Modifier.isInterface(info.getAccessFlags())) {
				// Only check field/method if it's an interface.
				V value = resolve.find(info, name, desc);
				if (value != null) {
					if (!guessAbstract || !Modifier.isAbstract(value.getAccessFlags())) {
						return value;
					}
					if (guess == null) {
						guess = value;
					}
				}
			} else {
				// Push super class for later check of it's interfaces too.
				ClassInfo superClass = info.getSuperclass();
				if (superClass != null) {
					queue.push(superClass);
				}
			}
			// Push sub-interfaces of the class
			for (ClassInfo iface : info.getInterfaces()) {
				queue.push(iface);
			}
		} while ((info = queue.poll()) != null);
		return guess;
	}

	@NotNull
	private Result<MethodInfo> checkVirtualMethod(MethodInfo info) {
		if (info != null) {
			int flags = info.getAccessFlags();
			if (Modifier.isStatic(flags)) {
				return Error.of(FailureReason.ACC_STATIC_SET);
			}
			if (Modifier.isAbstract(flags) && !Modifier.isAbstract(info.getOwner().getAccessFlags())) {
				return Error.of(FailureReason.ACC_ABSTRACT_UNSET);
			}
			return new Success<>(info);
		}
		return Error.of(FailureReason.NO_SUCH_METHOD);
	}

	private interface UncachedResolve<M, T> {
		M find(ClassInfo info, String name, T desc);
	}
}
