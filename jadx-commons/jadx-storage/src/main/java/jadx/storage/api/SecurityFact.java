package jadx.storage.api;

import java.util.Objects;

public final class SecurityFact {
	private final String kind;
	private final String value;
	private final int line;
	private final String detail;

	public SecurityFact(String kind, String value, int line, String detail) {
		this.kind = Objects.requireNonNull(kind);
		this.value = Objects.requireNonNull(value);
		this.line = line;
		this.detail = Objects.requireNonNull(detail);
	}

	public String getKind() {
		return kind;
	}

	public String getValue() {
		return value;
	}

	public int getLine() {
		return line;
	}

	public String getDetail() {
		return detail;
	}
}
