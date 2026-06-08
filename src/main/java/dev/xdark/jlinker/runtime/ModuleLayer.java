package dev.xdark.jlinker.runtime;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

/// An abstract representation of a JPMS module layer.
///
/// A module layer groups a set of named [Module]s and references zero or more
/// parent layers, mirroring the structure of [java.lang.ModuleLayer].
@NullMarked
public interface ModuleLayer {

	/// Returns the parent layers of this layer, in search order.
	List<? extends ModuleLayer> parents();

	/// Finds a module by name in this layer (does not search parent layers).
	///
	/// @param name the module name
	/// @return the module, or `null` if not found in this layer
	@Nullable Module findModule(String name);

	/// Finds a module by name in this layer, throwing if absent.
	///
	/// @param name the module name
	/// @return the module
	/// @throws NoSuchElementException if the module is not found in this layer
	default Module findModuleOrThrow(String name) {
		Module m;
		if ((m = findModule(name)) == null)
			throw new NoSuchElementException(name);
		return m;
	}

	/// Returns all modules contained in this layer.
	Collection<? extends Module> modules();
}
