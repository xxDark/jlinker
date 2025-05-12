package dev.xdark.jlinker;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.ClassNode;

import java.lang.module.ModuleFinder;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.fail;
import static org.objectweb.asm.Opcodes.*;

@DisabledIfEnvironmentVariable(
		named = "CI",
		matches = ".*"
)
public class TestJmods {

	@Test
	public void doTest() throws Exception {
		var classLookup = new ClassLookup();
		var linkResolver = LinkResolver.create();
		var buffer = new byte[16384];
		for (var moduleReference : ModuleFinder.ofSystem().findAll()) {
			try (var mr = moduleReference.open()) {
				try (var stream = mr.list()) {
					var iterator = stream.iterator();
					while (iterator.hasNext()) {
						var name = iterator.next();
						if (!name.endsWith(".class")) continue;
						ClassReader classReader;
						try (var in = mr.open(name).orElseThrow()) {
							int offset = 0;
							while (true) {
								if (offset == buffer.length) {
									buffer = Arrays.copyOf(buffer, offset + 1024);
								}
								int r = in.read(buffer, offset, buffer.length - offset);
								if (r == -1) {
									classReader = new ClassReader(buffer, 0, offset);
									break;
								}
								offset += r;
							}
						}
						var node = new ClassNode();
						classReader.accept(node, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG);
						var caller = classLookup.obtrude(node);
						classReader.accept(new ClassVisitor(ASM9) {

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
												case GETSTATIC, PUTSTATIC ->
														linkResolver.resolveStaticField(cm, name, desc);
												case GETFIELD, PUTFIELD ->
														linkResolver.resolveVirtualField(cm, name, desc);
											}
										} catch (ClassNotFoundException ignored) {
										} catch (FieldResolutionException ex) {
											fail(ex);
										}
									}

									@Override
									public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
										if (shouldMethodBeSkipped(owner, name, descriptor))
											return;
										var cm = classLookup.findClassOrNull(owner);
										if (cm == null) return;
										var desc = MethodDescriptorString.of(descriptor);
										try {
											switch (opcode) {
												case INVOKESTATIC -> linkResolver.resolveStaticMethod(cm, name, desc);
												case INVOKEVIRTUAL -> linkResolver.resolveVirtualMethod(cm, name, desc);
												case INVOKEINTERFACE ->
														linkResolver.resolveInterfaceMethod(cm, name, desc);
												case INVOKESPECIAL ->
														linkResolver.resolveSpecialMethod(cm, name, desc, caller);
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
						}, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
					}
				}
			}
		}
	}

	private static boolean shouldMethodBeSkipped(String owner, String name, String descriptor) {
		// Resolution of polymorphic signature methods is not supported.
		if (!(owner.equals("java/lang/invoke/MethodHandle") || owner.equals("java/lang/invoke/VarHandle")))
			return false;
		return true;
	}
}
