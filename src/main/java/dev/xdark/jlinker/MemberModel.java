package dev.xdark.jlinker;

import org.jetbrains.annotations.Contract;

public sealed interface MemberModel permits MethodModel, FieldModel {

	@Contract(pure = true)
	int accessFlags();
}
