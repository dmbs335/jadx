package jadx.gui.cache.usage;

import org.jetbrains.annotations.Nullable;

import jadx.core.dex.nodes.ClassNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.nodes.RootNode;

final class MthRef {
	private final String cls;
	private final String shortId;
	private final @Nullable ClassNode resolvedClass;
	private @Nullable MethodNode resolved;

	MthRef(String cls, String shortId) {
		this(cls, shortId, null);
	}

	MthRef(String cls, String shortId, @Nullable ClassNode resolvedClass) {
		this.cls = cls;
		this.shortId = shortId;
		this.resolvedClass = resolvedClass;
	}

	MthRef(MethodNode method) {
		this.cls = method.getParentClass().getRawName();
		this.shortId = method.getMethodInfo().getShortId();
		this.resolvedClass = method.getParentClass();
		this.resolved = method;
	}

	public String getCls() {
		return cls;
	}

	public String getShortId() {
		return shortId;
	}

	/**
	 * Usage-cache lists reuse the same {@code MthRef} instance for every edge that
	 * points to a method. Resolve that shared reference once instead of repeating
	 * a class-map lookup and a declaring-class method lookup for each
	 * edge while restoring a large project.
	 */
	public MethodNode resolve(RootNode root) {
		MethodNode method = resolved;
		if (method == null) {
			ClassNode clsNode = resolvedClass;
			if (clsNode == null) {
				method = root.resolveDirectMethod(cls, shortId);
			} else {
				method = clsNode.searchMethodByShortId(shortId);
				if (method == null) {
					throw new RuntimeException("Method not found: " + cls + '.' + shortId);
				}
			}
			resolved = method;
		}
		return method;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof MthRef)) {
			return false;
		}
		MthRef other = (MthRef) o;
		return cls.equals(other.cls)
				&& shortId.equals(other.shortId);
	}

	@Override
	public int hashCode() {
		return 31 * cls.hashCode() + shortId.hashCode();
	}
}
