package jadx.gui.cache.usage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import jadx.core.dex.nodes.ClassNode;
import jadx.core.dex.nodes.MethodNode;

final class ClsUsageData {
	private final String rawName;

	private List<String> clsDeps;
	private List<String> clsUsage;
	private List<MthRef> clsUseInMth;
	private @Nullable List<ClassNode> resolvedClsDeps;
	private @Nullable List<ClassNode> resolvedClsUsage;
	private @Nullable List<MethodNode> resolvedClsUseInMth;

	private final Map<String, FldUsageData> fldUsage = new HashMap<>();
	private final Map<String, MthUsageData> mthUsage = new HashMap<>();

	ClsUsageData(String rawName) {
		this.rawName = rawName;
	}

	public String getRawName() {
		return rawName;
	}

	public List<String> getClsDeps() {
		return clsDeps;
	}

	public void setClsDeps(List<String> clsDeps) {
		this.clsDeps = clsDeps;
	}

	public List<String> getClsUsage() {
		return clsUsage;
	}

	public void setClsUsage(List<String> clsUsage) {
		this.clsUsage = clsUsage;
	}

	public @Nullable List<ClassNode> getResolvedClsDeps() {
		return resolvedClsDeps;
	}

	public void setResolvedClsDeps(List<ClassNode> resolvedClsDeps) {
		this.resolvedClsDeps = resolvedClsDeps;
	}

	public @Nullable List<ClassNode> getResolvedClsUsage() {
		return resolvedClsUsage;
	}

	public void setResolvedClsUsage(List<ClassNode> resolvedClsUsage) {
		this.resolvedClsUsage = resolvedClsUsage;
	}

	public @Nullable List<MethodNode> getResolvedClsUseInMth() {
		return resolvedClsUseInMth;
	}

	public void setResolvedClsUseInMth(List<MethodNode> resolvedClsUseInMth) {
		this.resolvedClsUseInMth = resolvedClsUseInMth;
	}

	public List<MthRef> getClsUseInMth() {
		return clsUseInMth;
	}

	public void setClsUseInMth(List<MthRef> clsUseInMth) {
		this.clsUseInMth = clsUseInMth;
	}

	public Map<String, FldUsageData> getFldUsage() {
		return fldUsage;
	}

	public Map<String, MthUsageData> getMthUsage() {
		return mthUsage;
	}
}
