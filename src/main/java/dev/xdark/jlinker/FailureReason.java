package dev.xdark.jlinker;

/**
 * Resolution errors.
 *
 * @author xDark
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-5.html#jvms-5.4.3.3">JVM specification</a>
 */
public enum FailureReason {
    ACC_INTERFACE_SET,
    ACC_ABSTRACT_SET,
    ACC_STATIC_SET,
    ACC_INTERFACE_UNSET,
    ACC_ABSTRACT_UNSET,
    ACC_STATIC_UNSET,
    NO_SUCH_METHOD,
    NO_SUCH_FIELD
}
