package dev.xdark.jlinker;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Class information necessary for
 * both linker and resolver.
 *
 * @author xDark
 */
public interface ClassInfo {

    /**
     * @return Class access flags.
     */
    int getAccessFlags();

    /**
     * @return Parent class.
     */
    @Nullable ClassInfo getSuperclass();

    /**
     * @return Class interfaces.
     */
    @NotNull List<? extends @NotNull ClassInfo> getInterfaces();

    /**
     * Implementation may throw any exception.
     *
     * @param name       Method name.
     * @param descriptor Method descriptor.
     * @return Class method.
     */
    @Nullable MethodInfo getMethod(String name, MethodDescriptor descriptor);

    /**
     * Implementation may throw any exception.
     *
     * @param name       Field name.
     * @param descriptor Field descriptor.
     * @return Class field.
     */
    @Nullable FieldInfo getField(String name, FieldDescriptor descriptor);
}
