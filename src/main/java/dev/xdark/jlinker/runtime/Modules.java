package dev.xdark.jlinker.runtime;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.lang.module.ModuleDescriptor;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/// Utility methods for creating modified copies of [ModuleDescriptor] instances.
///
/// [ModuleDescriptor] is immutable; these helpers rebuild a descriptor with a
/// single property changed while preserving everything else. All methods
/// return a new [ModuleDescriptor] and leave the original unchanged.
@NullMarked
public final class Modules {
	private Modules() {
	}

	/// Returns a copy of `descriptor` with the module name replaced by `name`.
	public static ModuleDescriptor withName(
			ModuleDescriptor descriptor,
			String name
	) {
		return rebuild(descriptor, new Rebuilder() {
			@Override
			public String name(String __) {
				return name;
			}
		});
	}

	/// Returns a copy of `descriptor` with the module modifiers replaced by `modifiers`.
	public static ModuleDescriptor withModifiers(
			ModuleDescriptor descriptor,
			Set<? extends ModuleDescriptor.Modifier> modifiers
	) {
		return rebuild(descriptor, new Rebuilder() {
			@Override
			public Set<ModuleDescriptor.Modifier> modifiers(Set<ModuleDescriptor.Modifier> __) {
				return Set.copyOf(modifiers);
			}
		});
	}

	/// Returns a copy of `descriptor` with the main class set to `mainClass`,
	/// or cleared if `mainClass` is `null`.
	public static ModuleDescriptor withMainClass(
			ModuleDescriptor descriptor,
			@Nullable String mainClass
	) {
		return rebuild(descriptor, new Rebuilder() {
			@Override
			public Optional<String> mainClass(Optional<String> __) {
				return Optional.ofNullable(mainClass);
			}
		});
	}

	/// Returns a copy of `descriptor` with the version set to `version`,
	/// or cleared if `version` is `null`.
	public static ModuleDescriptor withVersion(
			ModuleDescriptor descriptor,
			ModuleDescriptor.@Nullable Version version
	) {
		return rebuild(descriptor, new Rebuilder() {
			@Override
			public Optional<ModuleDescriptor.Version> version(Optional<ModuleDescriptor.Version> __) {
				return Optional.ofNullable(version);
			}
		});
	}

	/// Returns a copy of `descriptor` with the exports list replaced by `exports`.
	public static ModuleDescriptor withExports(
			ModuleDescriptor descriptor,
			Collection<? extends ModuleDescriptor.Exports> exports
	) {
		return rebuild(descriptor, new Rebuilder() {
			@Override
			public Collection<? extends ModuleDescriptor.Exports> exports(Set<ModuleDescriptor.Exports> __) {
				return exports;
			}
		});
	}

	/// Returns a copy of `descriptor` with the opens list replaced by `opens`.
	public static ModuleDescriptor withOpens(
			ModuleDescriptor descriptor,
			Collection<? extends ModuleDescriptor.Opens> opens
	) {
		return rebuild(descriptor, new Rebuilder() {
			@Override
			public Collection<? extends ModuleDescriptor.Opens> opens(Set<ModuleDescriptor.Opens> __) {
				return opens;
			}
		});
	}

	/// Returns a copy of `descriptor` with the requires list replaced by `requires`.
	public static ModuleDescriptor withRequires(
			ModuleDescriptor descriptor,
			Collection<? extends ModuleDescriptor.Requires> requires
	) {
		return rebuild(descriptor, new Rebuilder() {
			@Override
			public Collection<? extends ModuleDescriptor.Requires> requires(Set<ModuleDescriptor.Requires> __) {
				return requires;
			}
		});
	}

	/// Returns a copy of `descriptor` with an additional qualified export of
	/// `packageName` to `targets`, using the given export `modifiers`.
	public static ModuleDescriptor addExports(
			ModuleDescriptor descriptor,
			Collection<? extends ModuleDescriptor.Exports.Modifier> modifiers,
			String packageName,
			Collection<? extends String> targets
	) {
		return rebuild(descriptor, new Rebuilder() {
			@Override
			public void finalize(ModuleDescriptor.Builder builder) {
				builder.exports(Set.copyOf(modifiers), packageName, Set.copyOf(targets));
			}
		});
	}

	/// Returns a copy of `descriptor` with an additional qualified export of
	/// `packageName` to `targets` (no extra modifiers).
	public static ModuleDescriptor addExports(
			ModuleDescriptor descriptor,
			String packageName,
			Collection<? extends String> targets
	) {
		return addExports(descriptor, Set.of(), packageName, Set.copyOf(targets));
	}

	/// Returns a copy of `descriptor` with an additional unqualified export of
	/// `packageName` (exported to all modules).
	public static ModuleDescriptor addExports(ModuleDescriptor descriptor, String packageName) {
		return addExports(descriptor, packageName, Set.of());
	}

	/// Returns a copy of `descriptor` with an additional qualified opens of
	/// `packageName` to `targets`, using the given opens `modifiers`.
	public static ModuleDescriptor addOpens(
			ModuleDescriptor descriptor,
			Collection<? extends ModuleDescriptor.Opens.Modifier> modifiers,
			String packageName,
			Collection<? extends String> targets
	) {
		return rebuild(descriptor, new Rebuilder() {
			@Override
			public void finalize(ModuleDescriptor.Builder builder) {
				builder.opens(Set.copyOf(modifiers), packageName, Set.copyOf(targets));
			}
		});
	}

	/// Returns a copy of `descriptor` with an additional qualified opens of
	/// `packageName` to `targets` (no extra modifiers).
	public static ModuleDescriptor addOpens(
			ModuleDescriptor descriptor,
			String packageName,
			Collection<? extends String> targets
	) {
		return addOpens(descriptor, Set.of(), packageName, Set.copyOf(targets));
	}

	/// Returns a copy of `descriptor` with an additional unqualified opens of
	/// `packageName` (open to all modules).
	public static ModuleDescriptor addOpens(ModuleDescriptor descriptor, String packageName) {
		return addOpens(descriptor, packageName, Set.of());
	}

	private interface Rebuilder {

		default void finalize(ModuleDescriptor.Builder builder) {
		}

		default String name(String name) {
			return name;
		}

		default Set<ModuleDescriptor.Modifier> modifiers(Set<ModuleDescriptor.Modifier> modifiers) {
			return modifiers;
		}

		default Optional<ModuleDescriptor.Version> version(Optional<ModuleDescriptor.Version> version) {
			return version;
		}

		default Optional<String> mainClass(Optional<String> mainClass) {
			return mainClass;
		}

		default Collection<? extends String> uses(Set<String> uses) {
			return uses;
		}

		default Collection<? extends ModuleDescriptor.Opens> opens(Set<ModuleDescriptor.Opens> opens) {
			return opens;
		}

		default Collection<? extends ModuleDescriptor.Exports> exports(Set<ModuleDescriptor.Exports> exports) {
			return exports;
		}

		default Collection<? extends ModuleDescriptor.Requires> requires(Set<ModuleDescriptor.Requires> requires) {
			return requires;
		}
	}

	private static ModuleDescriptor rebuild(ModuleDescriptor descriptor, Rebuilder rebuilder) {
		var builder = ModuleDescriptor.newModule(rebuilder.name(descriptor.name()), rebuilder.modifiers(descriptor.modifiers()));
		rebuilder.version(descriptor.version()).ifPresent(builder::version);
		rebuilder.mainClass(descriptor.mainClass()).ifPresent(builder::mainClass);
		rebuilder.uses(descriptor.uses()).forEach(builder::uses);
		rebuilder.opens(descriptor.opens()).forEach(builder::opens);
		rebuilder.exports(descriptor.exports()).forEach(builder::exports);
		rebuilder.requires(descriptor.requires()).forEach(builder::requires);
		rebuilder.finalize(builder);
		return builder.build();
	}
}
