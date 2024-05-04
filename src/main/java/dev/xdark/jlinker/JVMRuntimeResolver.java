package dev.xdark.jlinker;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Modifier;

final class JVMRuntimeResolver implements RuntimeResolver {
    private final LinkResolver linkResolver;

    JVMRuntimeResolver(LinkResolver linkResolver) {
        this.linkResolver = linkResolver;
    }

    @Override
    public @NotNull Result<MethodInfo> resolveStaticMethod(@NotNull ClassInfo owner, @NotNull String name, @NotNull MethodDescriptor descriptor, boolean itf) {
        // Here we can just delegate to link resolver
        return linkResolver.resolveStaticMethod(owner, name, descriptor, itf);
    }

    @Override
    public @NotNull Result<MethodInfo> resolveVirtualMethod(@NotNull ClassInfo owner, @NotNull String name, @NotNull MethodDescriptor descriptor) {
        Result<MethodInfo> result = linkResolver.resolveVirtualMethod(owner, name, descriptor);
        if (result instanceof Success) {
            if (Modifier.isAbstract(result.value().getAccessFlags())) {
                return Error.of(FailureReason.ACC_ABSTRACT_SET);
            }
        }
        return result;
    }

    @Override
    public @NotNull Result<MethodInfo> resolveInterfaceMethod(@NotNull ClassInfo owner, @NotNull String name, @NotNull MethodDescriptor descriptor) {
        // No checks, should be done by LinkResolver
        // linkResolver must be JVMLinkResolver
        MethodInfo method = ((JVMLinkResolver) linkResolver).uncachedLookupMethod(owner, name, descriptor);
        if (method == null) {
            method = ((JVMLinkResolver) linkResolver).uncachedInterfaceMethod(owner, name, descriptor);
        }
        if (method != null) {
            int accessFlags = method.getAccessFlags();
            if (Modifier.isStatic(accessFlags)) {
                return Error.of(FailureReason.ACC_STATIC_SET);
            }
            if (Modifier.isAbstract(accessFlags)) {
                return Error.of(FailureReason.ACC_ABSTRACT_SET);
            }
            return new Success<>(method);
        }
        return Error.of(FailureReason.NO_SUCH_METHOD);
    }

    @Override
    public @NotNull Result<FieldInfo> resolveStaticField(@NotNull ClassInfo owner, @NotNull String name, @NotNull FieldDescriptor descriptor) {
        return linkResolver.resolveStaticField(owner, name, descriptor);
    }

    @Override
    public @NotNull Result<FieldInfo> resolveVirtualField(@NotNull ClassInfo owner, @NotNull String name, @NotNull FieldDescriptor descriptor) {
        return linkResolver.resolveVirtualField(owner, name, descriptor);
    }
}
