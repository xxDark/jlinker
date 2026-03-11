package dev.xdark.jlinker.runtime;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.lang.module.ModuleDescriptor;

@NullMarked
public interface Module {

	@Nullable ModuleLayer layer();

	@Nullable ClassLoader classLoader();

	@Nullable ModuleDescriptor descriptor();

	@Nullable
	default String name() {
		var descriptor = descriptor();
		return descriptor != null ? descriptor.name() : null;
	}

	default boolean isNamed() {
		return descriptor() != null;
	}
}
