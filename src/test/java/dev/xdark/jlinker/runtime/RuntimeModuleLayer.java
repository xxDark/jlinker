package dev.xdark.jlinker.runtime;

import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@NullMarked
public final class RuntimeModuleLayer implements ModuleLayer {
	private final List<? extends ModuleLayer> parents;
	private final Map<String, Module> byName;

	public RuntimeModuleLayer(RuntimeClassLoaders classLoaders, java.lang.ModuleLayer layer) {
		parents = layer.parents()
				.stream()
				.map(l -> new RuntimeModuleLayer(classLoaders, l))
				.toList();
		byName = layer.modules()
				.stream()
				.map(module -> new RuntimeModule(this, classLoaders.mappingFor(module.getClassLoader()), module.getDescriptor()))
				.collect(Collectors.toUnmodifiableMap(runtimeModule -> Objects.requireNonNull(runtimeModule.name()), Function.identity()));
	}

	public RuntimeModuleLayer(java.lang.ModuleLayer layer) {
		this(new RuntimeClassLoaders(), layer);
	}

	@Override
	public @Unmodifiable List<? extends ModuleLayer> parents() {
		return parents;
	}

	@Override
	public @Nullable Module findModule(String name) {
		return byName.get(name);
	}

	@Override
	public @Unmodifiable Collection<? extends Module> modules() {
		return byName.values();
	}
}
