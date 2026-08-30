package jadx.core.dex.attributes.nodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jadx.api.plugins.input.data.attributes.PinnedAttribute;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.attributes.AttrNode;

public class RenameReasonAttr extends PinnedAttribute {

	public static RenameReasonAttr forNode(AttrNode node) {
		RenameReasonAttr renameReasonAttr = node.get(AType.RENAME_REASON);
		if (renameReasonAttr != null) {
			return renameReasonAttr;
		}
		RenameReasonAttr newAttr = new RenameReasonAttr();
		node.addAttr(newAttr);
		return newAttr;
	}

	private String firstReason;
	private List<String> additionalReasons = Collections.emptyList();

	public RenameReasonAttr() {
	}

	public RenameReasonAttr(String description) {
		append(description);
	}

	public RenameReasonAttr(AttrNode node) {
		RenameReasonAttr renameReasonAttr = node.get(AType.RENAME_REASON);
		if (renameReasonAttr != null) {
			this.firstReason = renameReasonAttr.firstReason;
			if (!renameReasonAttr.additionalReasons.isEmpty()) {
				this.additionalReasons = new ArrayList<>(renameReasonAttr.additionalReasons);
			}
		}
	}

	public RenameReasonAttr(AttrNode node, boolean notValid, boolean notPrintable) {
		this(node);
		if (notValid) {
			notValid();
		}
		if (notPrintable) {
			notPrintable();
		}
	}

	public RenameReasonAttr notValid() {
		return append("not valid java name");
	}

	public RenameReasonAttr notPrintable() {
		return append("contains not printable characters");
	}

	public RenameReasonAttr append(String reason) {
		if (reason.isEmpty()) {
			return this;
		}
		if (firstReason == null) {
			firstReason = reason;
			return this;
		}
		if (firstReason.equals(reason) || additionalReasons.contains(reason)) {
			return this;
		}
		if (additionalReasons.isEmpty()) {
			additionalReasons = new ArrayList<>(1);
		}
		additionalReasons.add(reason);
		return this;
	}

	public String getDescription() {
		if (firstReason == null) {
			return "";
		}
		if (additionalReasons.isEmpty()) {
			return firstReason;
		}
		return firstReason + " and " + String.join(" and ", additionalReasons);
	}

	@Override
	public AType<RenameReasonAttr> getAttrType() {
		return AType.RENAME_REASON;
	}

	@Override
	public String toString() {
		return "RENAME_REASON:" + getDescription();
	}
}
