package dev.xdark.jlinker;

public final class DescriptorString implements MethodDescriptor, FieldDescriptor {
	private final String value;

	public DescriptorString(String value) {
		this.value = value;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof DescriptorString &&
				value.equals(((DescriptorString) o).value);
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public String toString() {
		return value;
	}
}
