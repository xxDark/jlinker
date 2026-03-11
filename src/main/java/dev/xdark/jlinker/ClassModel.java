package dev.xdark.jlinker;

import dev.xdark.jlinker.runtime.Module;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface ClassModel<METHOD extends MemberModel, FIELD extends FieldModel> {

	Module module();

	String name();

	int accessFlags();

	@Nullable ClassModel<METHOD, FIELD> superClass();

	Iterable<? extends ClassModel<METHOD, FIELD>> interfaces();

	@Nullable METHOD findMethod(String name, MethodDescriptor descriptor);

	@Nullable FIELD findField(String name, FieldDescriptor descriptor);
}
