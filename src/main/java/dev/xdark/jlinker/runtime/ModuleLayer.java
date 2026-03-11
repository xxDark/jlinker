package dev.xdark.jlinker.runtime;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

@NullMarked
public interface ModuleLayer {

	List<? extends ModuleLayer> parents();

	@Nullable Module findModule(String name);

	/// @throws NoSuchElementException If the module is not found.
	default Module findModuleOrThrow(String name) {
		Module m;
		if ((m = findModule(name)) == null)
			throw new NoSuchElementException(name);
		return m;
	}

	Collection<? extends Module> modules();
}
