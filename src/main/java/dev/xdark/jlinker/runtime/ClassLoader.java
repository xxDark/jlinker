package dev.xdark.jlinker.runtime;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// An abstract representation of a Java class loader.
///
/// Provides the minimal class-loader metadata needed by the module model:
/// each class loader owns an unnamed [Module] and may delegate to a parent.
@NullMarked
public interface ClassLoader {

	/// Returns the unnamed module owned by this class loader.
	Module unnamedModule();

	/// Returns the parent class loader, or `null` for the bootstrap class loader.
	@Nullable ClassLoader parent();
}
