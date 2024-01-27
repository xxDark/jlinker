package dev.xdark.jlinker;

/**
 * Member information necessary for
 * both linker and resolver.
 *
 * @author xDark
 */
public interface MemberInfo {

    /**
     * @return Member owner
     */
    ClassInfo getOwner();

    /**
     * @return Member access flags.
     */
    int getAccessFlags();
}
