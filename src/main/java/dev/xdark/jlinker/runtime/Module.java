package dev.xdark.jlinker.runtime;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.lang.module.ModuleDescriptor;

/// An abstract representation of a Java Platform Module System (JPMS) module.
///
/// Implementations model both named modules (those with a `module-info`) and
/// unnamed modules (the class-path fallback).  The resolver uses this to
/// provide module-aware class model context; resolution itself does not
/// enforce module access rules.
@NullMarked
public interface Module {

	/// Returns the layer this module belongs to, or `null` for the unnamed module.
	@Nullable ModuleLayer layer();

	/// Returns the class loader associated with this module, or `null` if the
	/// module is loaded by the bootstrap class loader.
	@Nullable ClassLoader classLoader();

	/// Returns the module descriptor, or `null` for the unnamed module.
	@Nullable ModuleDescriptor descriptor();

	/// Returns the module name, or `null` for the unnamed module.
	@Nullable
	default String name() {
		var descriptor = descriptor();
		return descriptor != null ? descriptor.name() : null;
	}

	/// Returns `true` if this is a named module (has a module descriptor).
	default boolean isNamed() {
		return descriptor() != null;
	}
}
