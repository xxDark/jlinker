package dev.xdark.jlinker;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Modifier;

final class JVMRuntimeResolver<C, M> implements RuntimeResolver<C, M> {
    private final LinkResolver<C, M, ?> linkResolver;

    JVMRuntimeResolver(LinkResolver<C, M, ?> linkResolver) {
        this.linkResolver = linkResolver;
    }

    @Override
    public @NotNull Result<Resolution<C, M>> resolveStaticMethod(@NotNull ClassInfo<C> owner, @NotNull String name, @NotNull String descriptor, boolean itf) {
        // Here we can just delegate to link resolver
        return linkResolver.resolveStaticMethod(owner, name, descriptor, itf);
    }

    @Override
    public @NotNull Result<Resolution<C, M>> resolveVirtualMethod(@NotNull ClassInfo<C> owner, @NotNull String name, @NotNull String descriptor) {
        Result<Resolution<C, M>> result = linkResolver.resolveVirtualMethod(owner, name, descriptor);
        if (result.isSuccess()) {
            if (Modifier.isAbstract(result.value().member().accessFlags())) {
                return Result.error(ResolutionError.METHOD_IS_ABSTRACT);
            }
        }
        return result;
    }

    @Override
    public @NotNull Result<Resolution<C, M>> resolveInterfaceMethod(@NotNull ClassInfo<C> owner, @NotNull String name, @NotNull String descriptor) {
        // No checks, should be done by LinkResolver
        // linkResolver must be JVMLinkResolver
        Resolution<C, M> resolution = ((JVMLinkResolver) linkResolver).uncachedLookupMethod(owner, name, descriptor);
        if (resolution == null) {
            resolution = ((JVMLinkResolver) linkResolver).uncachedInterfaceMethod(owner, name, descriptor);
        }
        if (resolution != null) {
            int accessFlags = resolution.member().accessFlags();
            if (Modifier.isStatic(accessFlags)) {
                return Result.error(ResolutionError.METHOD_NOT_VIRTUAL);
            }
            if (Modifier.isAbstract(accessFlags)) {
                return Result.error(ResolutionError.METHOD_IS_ABSTRACT);
            }
            return Result.ok(resolution);
        }
        return Result.error(ResolutionError.NO_SUCH_METHOD);
    }
}
