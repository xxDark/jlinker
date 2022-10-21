package dev.xdark.jlinker;

/**
 * Resolution errors.
 *
 * @author xDark
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-5.html#jvms-5.4.3.3">JVM specification</a>
 */
public enum ResolutionError {
    CLASS_MUST_NOT_BE_INTERFACE,
    CLASS_MUST_BE_INTERFACE,
    CLASS_NOT_ABSTRACT,
    NO_SUCH_METHOD,
    METHOD_NOT_STATIC,
    METHOD_NOT_VIRTUAL,
    NO_SUCH_FIELD,
    METHOD_IS_ABSTRACT,
    FIELD_NOT_STATIC,
    FIELD_NOT_VIRTUAL,
}
