package dev.xdark.jlinker;

import org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

final class AsmClassReader {
	private byte[] buffer = new byte[65536 * 4];

	ClassReader read(InputStream in) throws IOException {
		var buffer = this.buffer;
		int offset = 0;
		while (true) {
			if (offset == buffer.length) {
				buffer = Arrays.copyOf(buffer, offset + 1024);
			}
			int r = in.read(buffer, offset, buffer.length - offset);
			if (r == -1) {
				this.buffer = buffer;
				return new ClassReader(buffer, 0, offset);
			}
			offset += r;
		}
	}
}
