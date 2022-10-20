package dev.xdark.jlinker;

/**
 * Member information necessary for
 * both linker and resolver.
 *
 * @author xDark
 */
public interface MemberInfo<V> {

    /**
     * @return Inner value, e.g. ASM MethodNode/FieldNode.
     */
    V innerValue();

    /**
     * @return Member access flags.
     */
    int accessFlags();

    /**
     * @return Whether this member is a polymorphic member.
     */
    boolean isPolymorphic();
}
