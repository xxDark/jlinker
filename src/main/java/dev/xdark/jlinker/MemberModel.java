package dev.xdark.jlinker;

public sealed interface MemberModel permits MethodModel, FieldModel {

	int accessFlags();
}
