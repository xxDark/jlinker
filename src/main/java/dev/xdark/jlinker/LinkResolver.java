package dev.xdark.jlinker;

import java.lang.reflect.Modifier;

/**
 * Link time resolver.
 *
 * @author xDark
 */
public interface LinkResolver<C, M, F> {

    /**
     * Resolves static method.
     *
     * @param owner      Method owner.
     * @param name       Method name.
     * @param descriptor Method descriptor.
     * @param itf        Whether the owner is an interface class.
     * @return Resolution result.
     */
    Result<Resolution<C, M>> resolveStaticMethod(ClassInfo<C> owner, String name, String descriptor, boolean itf);

    /**
     * Resolves static method.
     *
     * @param owner      Method owner.
     * @param name       Method name.
     * @param descriptor Method descriptor.
     * @return Resolution result.
     */
    default Result<Resolution<C, M>> resolveStaticMethod(ClassInfo<C> owner, String name, String descriptor) {
        return resolveStaticMethod(owner, name, descriptor, Modifier.isInterface(owner.accessFlags()));
    }

    /**
     * Resolves virtual method.
     *
     * @param owner      Method owner.
     * @param name       Method name.
     * @param descriptor Method descriptor.
     * @return Resolution result.
     */
    Result<Resolution<C, M>> resolveVirtualMethod(ClassInfo<C> owner, String name, String descriptor);

    /**
     * Resolves interface method.
     *
     * @param owner      Method owner.
     * @param name       Method name.
     * @param descriptor Method descriptor.
     * @return Resolution result.
     */
    Result<Resolution<C, M>> resolveInterfaceMethod(ClassInfo<C> owner, String name, String descriptor);

    /**
     * Resolves static field.
     *
     * @param owner      Field owner.
     * @param name       Field name.
     * @param descriptor Field descriptor.
     * @return Resolution result.
     */
    Result<Resolution<C, F>> resolveStaticField(ClassInfo<C> owner, String name, String descriptor);

    /**
     * Resolves virtual field.
     *
     * @param owner      Field owner.
     * @param name       Field name.
     * @param descriptor Field descriptor.
     * @return Resolution result.
     */
    Result<Resolution<C, F>> resolveVirtualField(ClassInfo<C> owner, String name, String descriptor);

    /**
     * @param classArenaAllocator Class arena allocator.
     * @return JVM link resolver.
     */
    static <C, M, F> LinkResolver<C, M, F> jvm(ArenaAllocator<ClassInfo<C>> classArenaAllocator) {
        return new JVMLinkResolver<>(classArenaAllocator);
    }

    /**
     * The returned link resolver is not thread-safe.
     *
     * @return JVM link resolver.
     */
    static <C, M, F> LinkResolver<C, M, F> jvm() {
        return jvm(new FrameArenaAllocator<>());
    }
}
