package dev.xdark.jlinker.runtime;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface ClassLoader {

	Module unnamedModule();

	@Nullable ClassLoader parent();
}
