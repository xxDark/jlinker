package dev.xdark.jlinker.runtime;

import java.lang.module.ModuleDescriptor;

public record RuntimeModule(ModuleLayer layer, ClassLoader classLoader, ModuleDescriptor descriptor) implements Module {
}
