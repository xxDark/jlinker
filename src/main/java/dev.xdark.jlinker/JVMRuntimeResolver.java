package dev.xdark.jlinker;

import java.lang.reflect.Modifier;

final class JVMRuntimeResolver<C, M> implements RuntimeResolver<C, M> {
    private final LinkResolver<C, M, ?> linkResolver;

    JVMRuntimeResolver(LinkResolver<C, M, ?> linkResolver) {
        this.linkResolver = linkResolver;
    }

    @Override
    public Result<Resolution<C, M>> resolveStaticMethod(ClassInfo<C> owner, String name, String descriptor, boolean itf) {
        // Here we can just delegate to link resolver
        return linkResolver.resolveStaticMethod(owner, name, descriptor, itf);
    }

    @Override
    public Result<Resolution<C, M>> resolveVirtualMethod(ClassInfo<C> owner, String name, String descriptor) {
        // And here too
        return linkResolver.resolveVirtualMethod(owner, name, descriptor);
    }

    @Override
    public Result<Resolution<C, M>> resolveInterfaceMethod(ClassInfo<C> owner, String name, String descriptor) {
        // But not here :(
        // No checks, should be done by LinkResolver
        // linkResolver must be JVMLinkResolver
        Resolution<C, M> resolution = ((JVMLinkResolver) linkResolver).uncachedLookupMethod(owner, name, descriptor);
        if (resolution == null) {
            resolution = ((JVMLinkResolver) linkResolver).uncachedInterfaceMethod(owner, name, descriptor);
        }
        if (resolution != null) {
            if (!Modifier.isStatic(resolution.member().accessFlags())) {
                return Result.ok(resolution);
            }
            return Result.error(ResolutionError.METHOD_NOT_VIRTUAL);
        }
        return Result.error(ResolutionError.NO_SUCH_METHOD);
    }
}
