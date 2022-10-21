package dev.xdark.jlinker;

import java.util.List;

/**
 * Class information necessary for
 * both linker and resolver.
 *
 * @author xDark
 */
public interface ClassInfo<C> {

    /**
     * @return Inner value, e.g. ASM ClassNode.
     */
    C innerValue();

    /**
     * @return Class access flags.
     */
    int accessFlags();

    /**
     * @return Parent class.
     */
    ClassInfo<C> superClass();

    /**
     * @return Class interfaces.
     */
    List<ClassInfo<C>> interfaces();

    /**
     * Implementation may throw any exception.
     *
     * @param name       Method name.
     * @param descriptor Method descriptor.
     * @return Class method.
     */
    MemberInfo<?> getMethod(String name, String descriptor);

    /**
     * Implementation may throw any exception.
     *
     * @param name       Field name.
     * @param descriptor Field descriptor.
     * @return Class field.
     */
    MemberInfo<?> getField(String name, String descriptor);
}
