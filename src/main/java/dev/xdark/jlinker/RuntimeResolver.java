package dev.xdark.jlinker;

import org.jetbrains.annotations.NotNull;

/**
 * Runtime resolver.
 *
 * @author xDark
 */
public interface RuntimeResolver<C, M> {

    /**
     * Resolves static method.
     *
     * @param owner      Method owner.
     * @param name       Method name.
     * @param descriptor Method descriptor.
     * @param itf        Whether the owner is an interface class.
     * @return Resolution result.
     */
    @NotNull Result<Resolution<C, M>> resolveStaticMethod(@NotNull ClassInfo<C> owner, @NotNull String name, @NotNull String descriptor, boolean itf);

    /**
     * Resolves virtual method.
     *
     * @param owner      Method owner.
     * @param name       Method name.
     * @param descriptor Method descriptor.
     * @return Resolution result.
     */
    @NotNull Result<Resolution<C, M>> resolveVirtualMethod(@NotNull ClassInfo<C> owner, @NotNull String name, @NotNull String descriptor);

    /**
     * Resolves interface method.
     *
     * @param owner      Method owner.
     * @param name       Method name.
     * @param descriptor Method descriptor.
     * @return Resolution result.
     */
    @NotNull Result<Resolution<C, M>> resolveInterfaceMethod(@NotNull ClassInfo<C> owner, @NotNull String name, @NotNull String descriptor);

    /**
     * @param linkResolver JVM link resolver.
     * @return JVM runtime resolver.
     * @throws IllegalArgumentException if provided linker is not a JVM linker.
     */
    static <C, M> RuntimeResolver<C, M> jvm(LinkResolver<C, M, ?> linkResolver) {
        if (!(linkResolver instanceof JVMLinkResolver)) {
            throw new IllegalArgumentException("Not a JVM link resolver");
        }
        return new JVMRuntimeResolver<>(linkResolver);
    }
}
