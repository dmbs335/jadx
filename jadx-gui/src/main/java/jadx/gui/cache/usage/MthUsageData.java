package jadx.gui.cache.usage;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import jadx.api.plugins.input.data.IMethodRef;
import jadx.core.dex.info.MethodInfo;
import jadx.core.dex.nodes.MethodNode;

final class MthUsageData {
	private final MthRef mthRef;
	private List<MthRef> usage;
	private List<MthRef> uses;
	private List<IMethodRef> unresolvedUsage;
	private @Nullable MethodNode resolvedMethod;
	private @Nullable List<MethodNode> resolvedUsage;
	private @Nullable List<MethodNode> resolvedUses;
	private @Nullable List<MethodInfo> resolvedUnresolvedUsage;
	private boolean callsSelf;

	public MthUsageData(MthRef mthRef) {
		this.mthRef = mthRef;
	}

	public MthRef getMthRef() {
		return mthRef;
	}

	public List<MthRef> getUsage() {
		return usage;
	}

	public void setUsage(List<MthRef> usage) {
		this.usage = usage;
	}

	public List<MthRef> getUses() {
		return uses;
	}

	public void setUses(List<MthRef> uses) {
		this.uses = uses;
	}

	public List<IMethodRef> getUnresolvedUsage() {
		return unresolvedUsage;
	}

	public void setUnresolvedUsage(List<IMethodRef> unresolvedUsage) {
		this.unresolvedUsage = unresolvedUsage;
	}

	public @Nullable MethodNode getResolvedMethod() {
		return resolvedMethod;
	}

	public void setResolvedMethod(MethodNode resolvedMethod) {
		this.resolvedMethod = resolvedMethod;
	}

	public @Nullable List<MethodNode> getResolvedUsage() {
		return resolvedUsage;
	}

	public void setResolvedUsage(List<MethodNode> resolvedUsage) {
		this.resolvedUsage = resolvedUsage;
	}

	public @Nullable List<MethodNode> getResolvedUses() {
		return resolvedUses;
	}

	public void setResolvedUses(List<MethodNode> resolvedUses) {
		this.resolvedUses = resolvedUses;
	}

	public @Nullable List<MethodInfo> getResolvedUnresolvedUsage() {
		return resolvedUnresolvedUsage;
	}

	public void setResolvedUnresolvedUsage(List<MethodInfo> resolvedUnresolvedUsage) {
		this.resolvedUnresolvedUsage = resolvedUnresolvedUsage;
	}

	public boolean callsSelf() {
		return callsSelf;
	}

	public void setCallsSelf(boolean callsSelf) {
		this.callsSelf = callsSelf;
	}
}
