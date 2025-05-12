package dev.xdark.jlinker;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

public interface ClassModel<METHOD extends MemberModel, FIELD extends FieldModel> {

	@NotNull
	@Contract(pure = true)
	String name();

	@Contract(pure = true)
	int accessFlags();

	@Nullable
	@Contract(pure = true)
	ClassModel<METHOD, FIELD> superClass();

	@NotNull
	@Unmodifiable
	@Contract(pure = true)
	Iterable<? extends ClassModel<METHOD, FIELD>> interfaces();

	@Nullable
	@Contract(pure = true)
	METHOD findMethod(@NotNull String name, @NotNull MethodDescriptor descriptor);

	@Nullable
	@Contract(pure = true)
	FIELD findField(@NotNull String name, @NotNull FieldDescriptor descriptor);
}
