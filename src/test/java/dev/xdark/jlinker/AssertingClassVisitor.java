package dev.xdark.jlinker;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.junit.jupiter.api.Assertions.fail;
import static org.objectweb.asm.Opcodes.*;

final class AssertingClassVisitor extends ClassVisitor {
	private final ClassLookup classLookup;
	private final LinkResolver linkResolver;
	private final MyClassModel caller;

	AssertingClassVisitor(ClassLookup classLookup, LinkResolver linkResolver, MyClassModel caller) {
		super(Opcodes.ASM9);
		this.classLookup = classLookup;
		this.linkResolver = linkResolver;
		this.caller = caller;
	}

	@Override
	public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
		return null;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
		return new MethodVisitor(ASM9) {

			private void testMethodHandle(int opcode, Handle handle) {
				visitMethodInsn(opcode, handle.getOwner(), handle.getName(), handle.getDesc(), handle.isInterface());
			}

			private void testFieldHandle(int opcode, Handle handle) {
				visitFieldInsn(opcode, handle.getOwner(), handle.getName(), handle.getDesc());
			}

			private void testXHandle(Handle handle) {
				int tag = handle.getTag();
				int opcode = switch (tag) {
					case H_GETFIELD -> GETFIELD;
					case H_GETSTATIC -> GETSTATIC;
					case H_PUTFIELD -> PUTFIELD;
					case H_PUTSTATIC -> PUTSTATIC;
					case H_INVOKEVIRTUAL -> INVOKEVIRTUAL;
					case H_INVOKESTATIC -> INVOKESTATIC;
					case H_INVOKESPECIAL, H_NEWINVOKESPECIAL -> INVOKESPECIAL;
					case H_INVOKEINTERFACE -> INVOKEINTERFACE;
					default -> throw new IllegalStateException();
				};
				if (tag < H_INVOKEVIRTUAL) {
					testFieldHandle(opcode, handle);
					return;
				}
				testMethodHandle(opcode, handle);
			}

			private void testCst(Object cst) {
				if (cst instanceof Handle handle) {
					testXHandle(handle);
					return;
				}
				if (!(cst instanceof ConstantDynamic constantDynamic))
					return;
				for (int i = constantDynamic.getBootstrapMethodArgumentCount(); i != 0; ) {
					testCst(constantDynamic.getBootstrapMethodArgument(--i));
				}
			}

			@Override
			public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
				var cm = classLookup.findClassOrNull(owner);
				if (cm == null) return;
				var desc = FieldDescriptorString.of(descriptor);
				try {
					switch (opcode) {
						case GETSTATIC, PUTSTATIC -> linkResolver.resolveStaticField(cm, name, desc);
						case GETFIELD, PUTFIELD -> linkResolver.resolveVirtualField(cm, name, desc);
					}
				} catch (ClassNotFoundException ignored) {
				} catch (FieldResolutionException ex) {
					fail(ex);
				}
			}

			@Override
			public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
				if (PolymorphicMethods.isPolymorphicMethod(owner, name, descriptor))
					return;
				var cm = classLookup.findClassOrNull(owner);
				if (cm == null) return;
				var desc = MethodDescriptorString.of(descriptor);
				try {
					switch (opcode) {
						case INVOKESTATIC -> linkResolver.resolveStaticMethod(cm, name, desc);
						case INVOKEVIRTUAL -> linkResolver.resolveVirtualMethod(cm, name, desc);
						case INVOKEINTERFACE -> linkResolver.resolveInterfaceMethod(cm, name, desc);
						case INVOKESPECIAL -> linkResolver.resolveSpecialMethod(cm, name, desc, caller);
					}
				} catch (ClassNotFoundException ignored) {
				} catch (MethodResolutionException t) {
					fail(t);
				}
			}

			@Override
			public void visitLdcInsn(Object value) {
				testCst(value);
			}

			@Override
			public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
				testXHandle(bootstrapMethodHandle);
				for (var arg : bootstrapMethodArguments) {
					testCst(arg);
				}
			}
		};
	}
}
