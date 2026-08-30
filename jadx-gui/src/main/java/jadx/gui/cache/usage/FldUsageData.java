package jadx.gui.cache.usage;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import jadx.core.dex.nodes.MethodNode;

final class FldUsageData {
	private final FldRef fldRef;
	private List<MthRef> usage;
	private @Nullable List<MethodNode> resolvedUsage;

	public FldUsageData(FldRef fldRef) {
		this.fldRef = fldRef;
	}

	public FldRef getFldRef() {
		return fldRef;
	}

	public List<MthRef> getUsage() {
		return usage;
	}

	public void setUsage(List<MthRef> usage) {
		this.usage = usage;
	}

	public @Nullable List<MethodNode> getResolvedUsage() {
		return resolvedUsage;
	}

	public void setResolvedUsage(List<MethodNode> resolvedUsage) {
		this.resolvedUsage = resolvedUsage;
	}
}
