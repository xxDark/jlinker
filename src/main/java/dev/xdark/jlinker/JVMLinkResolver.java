package dev.xdark.jlinker;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Modifier;

final class JVMLinkResolver<C, M, F> implements LinkResolver<C, M, F> {
    private final ArenaAllocator<ClassInfo<C>> classArenaAllocator;

    /**
     * @param classArenaAllocator Class arena allocator.
     */
    JVMLinkResolver(ArenaAllocator<ClassInfo<C>> classArenaAllocator) {
        this.classArenaAllocator = classArenaAllocator;
    }

    @Override
    public Result<Resolution<C, M>> resolveStaticMethod(@NotNull ClassInfo<C> owner, @NotNull String name, @NotNull String descriptor, boolean itf) {
        Resolution<C, M> method;
        if (itf) {
            if (!Modifier.isInterface(owner.accessFlags())) {
                return Result.error(ResolutionError.CLASS_MUST_BE_INTERFACE);
            }
            method = uncachedInterfaceMethod(owner, name, descriptor);
        } else {
            method = uncachedLookupMethod(owner, name, descriptor);
        }
        if (method != null) {
            if (!Modifier.isStatic(method.member().accessFlags())) {
                return Result.error(ResolutionError.METHOD_NOT_STATIC);
            }
            if (itf != Modifier.isInterface(method.owner().accessFlags())) {
                return Result.error(ResolutionError.CLASS_MUST_BE_INTERFACE);
            }
            return Result.ok(method);
        }
        return Result.error(ResolutionError.NO_SUCH_METHOD);
    }

    @Override
    public Result<Resolution<C, M>> resolveSpecialMethod(@NotNull ClassInfo<C> owner, @NotNull String name, @NotNull String descriptor, boolean itf) {
        if (Modifier.isInterface(owner.accessFlags()) != itf) {
            return Result.error(itf ? ResolutionError.CLASS_MUST_BE_INTERFACE : ResolutionError.CLASS_MUST_NOT_BE_INTERFACE);
        }
        Resolution<C, M> method = itf ? uncachedInterfaceMethod(owner, name, descriptor) : uncachedLookupMethod(owner, name, descriptor);
        return checkVirtualMethod(method);
    }

    @Override
    public Result<Resolution<C, M>> resolveVirtualMethod(@NotNull ClassInfo<C> owner, @NotNull String name, @NotNull String descriptor) {
        if (Modifier.isInterface(owner.accessFlags())) {
            return Result.error(ResolutionError.CLASS_MUST_NOT_BE_INTERFACE);
        }
        Resolution<C, M> method = uncachedLookupMethod(owner, name, descriptor);
        if (method == null) {
            method = uncachedInterfaceMethod(owner, name, descriptor);
        }
        return checkVirtualMethod(method);
    }

    @Override
    public Result<Resolution<C, M>> resolveInterfaceMethod(@NotNull ClassInfo<C> owner, @NotNull String name, @NotNull String descriptor) {
        if (!Modifier.isInterface(owner.accessFlags())) {
            return Result.error(ResolutionError.CLASS_MUST_BE_INTERFACE);
        }
        Resolution<C, M> resolution = uncachedInterfaceMethod(owner, name, descriptor);
        if (resolution == null) {
            return Result.error(ResolutionError.NO_SUCH_METHOD);
        }
        if (Modifier.isStatic(resolution.member().accessFlags())) {
            return Result.error(ResolutionError.METHOD_NOT_VIRTUAL);
        }
        return Result.ok(resolution);
    }

    @Override
    public Result<Resolution<C, F>> resolveStaticField(@NotNull ClassInfo<C> owner, @NotNull String name, @NotNull String descriptor) {
        ClassInfo<C> info = owner;
        MemberInfo<F> field = null;
        while (owner != null) {
            field = (MemberInfo<F>) owner.getField(name, descriptor);
            if (field != null) {
                info = owner;
                break;
            }
            owner = owner.superClass();
        }
        if (field == null) {
            Resolution<C, F> resolution = uncachedInterfaceLookup(info, name, descriptor, false, ClassInfo::getField);
            if (resolution != null) {
                if (!Modifier.isStatic(resolution.member().accessFlags())) {
                    return Result.error(ResolutionError.FIELD_NOT_STATIC);
                }
                return Result.ok(resolution);
            }
        }
        if (field == null) {
            return Result.error(ResolutionError.NO_SUCH_FIELD);
        }
        if (!Modifier.isStatic(field.accessFlags())) {
            return Result.error(ResolutionError.FIELD_NOT_STATIC);
        }
        return Result.ok(new Resolution<>(info, field, false));
    }

    @Override
    public Result<Resolution<C, F>> resolveVirtualField(@NotNull ClassInfo<C> owner, @NotNull String name, @NotNull String descriptor) {
        while (owner != null) {
            MemberInfo<F> field = (MemberInfo<F>) owner.getField(name, descriptor);
            if (field != null) {
                if (!Modifier.isStatic(field.accessFlags())) {
                    return Result.ok(new Resolution<>(owner, field, false));
                }
                return Result.error(ResolutionError.FIELD_NOT_VIRTUAL);
            }
            owner = owner.superClass();
        }
        return Result.error(ResolutionError.NO_SUCH_FIELD);
    }

    Resolution<C, M> uncachedLookupMethod(ClassInfo<C> owner, String name, String descriptor) {
        do {
            MemberInfo<M> member = (MemberInfo<M>) owner.getMethod(name, descriptor);
            if (member != null) {
                return new Resolution<>(owner, member, false);
            }
        } while ((owner = owner.superClass()) != null);
        return null;
    }

    Resolution<C, M> uncachedInterfaceMethod(ClassInfo<C> owner, String name, String descriptor) {
        ClassInfo<C> info = owner;
        Resolution<C, M> resolution = uncachedInterfaceLookup(owner, name, descriptor, true, ClassInfo::getMethod);
        if (resolution != null) {
            return resolution;
        }
        // We have corner case when we have an interface
        // that looks like that:
        // interface Foo { int hashCode(); }
        // TODO optimize
        info = info.superClass();
        while (info != null) {
            ClassInfo<C> superClass = info.superClass();
            if (superClass == null) {
                break;
            }
            info = superClass;
        }
        // Null in case info is java/lang/Object or an annotation (?), apparently
        if (info == null) {
            return null;
        }
        MemberInfo<M> member = (MemberInfo<M>) info.getMethod(name, descriptor);
        if (member != null) {
            int accessFlags = member.accessFlags();
            if (Modifier.isStatic(accessFlags) || !Modifier.isPublic(accessFlags)) {
                member = null;
            }
        }
        return member == null ? null : new Resolution<>(info, member, true);
    }

    private <V> Resolution<C, V> uncachedInterfaceLookup(ClassInfo<C> info, String name, String desc, boolean guessAbstract, UncachedResolve resolve) {
        Resolution<C, V> guess = null;
        try (Arena<ClassInfo<C>> arena = classArenaAllocator.push()) {
            arena.push(info); // Push interface/class to the arena
            while ((info = arena.poll()) != null) {
                if (Modifier.isInterface(info.accessFlags())) {
                    // Only check field if it's an interface.
                    MemberInfo<V> value = (MemberInfo<V>) resolve.find(info, name, desc);
                    if (value != null) {
                        Resolution<C, V> resolution = new Resolution<>(info, value, false);
                        if (!guessAbstract || !Modifier.isAbstract(value.accessFlags())) {
                            return resolution;
                        }
                        if (guess == null) {
                            guess = resolution;
                        }
                    }
                } else {
                    // Push super class for later check of it's interfaces too.
                    ClassInfo<C> superClass = info.superClass();
                    if (superClass != null) {
                        arena.push(superClass);
                    }
                }
                // Push sub-interfaces of the class
                arena.push(info.interfaces());
            }
        }
        return guess;
    }

    @NotNull
    private Result<Resolution<C, M>> checkVirtualMethod(Resolution<C, M> method) {
        if (method != null) {
            int flags = method.member().accessFlags();
            if (Modifier.isStatic(flags)) {
                return Result.error(ResolutionError.METHOD_NOT_VIRTUAL);
            }
            if (Modifier.isAbstract(flags) && !Modifier.isAbstract(method.owner().accessFlags())) {
                return Result.error(ResolutionError.CLASS_NOT_ABSTRACT);
            }
            return Result.ok(method);
        }
        return Result.error(ResolutionError.NO_SUCH_METHOD);
    }

    private interface UncachedResolve {
        MemberInfo<?> find(ClassInfo<?> info, String name, String desc);
    }
}
