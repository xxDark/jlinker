package dev.xdark.jlinker;

/**
 * Resolution result.
 *
 * @author xDark
 */
public final class Resolution<C, T> {
    private final ClassInfo<C> owner;
    private final MemberInfo<T> member;
    private final boolean forced;

    /**
     * @param owner  Class declaring the member.
     * @param member Resolved member.
     * @param forced Whether this resolution was forced.
     */
    public Resolution(ClassInfo<C> owner, MemberInfo<T> member, boolean forced) {
        this.owner = owner;
        this.member = member;
        this.forced = forced;
    }

    /**
     * @return Member owner.
     */
    public ClassInfo<C> owner() {
        return owner;
    }

    /**
     * @return Class member.
     */
    public MemberInfo<T> member() {
        return member;
    }

    /**
     * @return Whether this resolution was forced.
     * @see <a href="https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-5.html#jvms-5.4.3.4">JVM Spec p. 3</a>
     */
    public boolean forced() {
        return forced;
    }
}
