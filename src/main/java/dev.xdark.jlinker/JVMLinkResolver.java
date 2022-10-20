package dev.xdark.jlinker;

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
    public Result<Resolution<C, M>> resolveStaticMethod(ClassInfo<C> owner, String name, String descriptor, boolean itf) {
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
            if (!method.forced() && itf != Modifier.isInterface(method.owner().accessFlags())) {
                return Result.error(ResolutionError.CLASS_MUST_BE_INTERFACE);
            }
            return Result.ok(method);
        }
        return Result.error(ResolutionError.NO_SUCH_METHOD);
    }

    @Override
    public Result<Resolution<C, M>> resolveVirtualMethod(ClassInfo<C> owner, String name, String descriptor) {
        if (Modifier.isInterface(owner.accessFlags())) {
            return Result.error(ResolutionError.CLASS_MUST_NOT_BE_INTERFACE);
        }
        Resolution<C, M> method = uncachedLookupMethod(owner, name, descriptor);
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

    @Override
    public Result<Resolution<C, M>> resolveInterfaceMethod(ClassInfo<C> owner, String name, String descriptor) {
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
        try (Arena<ClassInfo<C>> arena = classArenaAllocator.push()) {
            arena.push(owner);
            while ((owner = arena.poll()) != null) {
                MemberInfo<M> member = (MemberInfo<M>) owner.getMethod(name, descriptor);
                if (member != null) {
                    return new Resolution<>(owner, member, false);
                }
                arena.push(owner.interfaces());
            }
        }
        // We have corner case when a compiler can generate interface call
        // to java/lang/Object. This cannot happen with javac, but the spec
        // allows so.
        // TODO optimize
        info = info.superClass();
        while (info != null) {
            ClassInfo<C> superClass = info.superClass();
            if (superClass == null) {
                break;
            }
            info = superClass;
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
}
