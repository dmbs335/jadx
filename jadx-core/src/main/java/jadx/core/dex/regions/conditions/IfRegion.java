package jadx.core.dex.regions.conditions;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.RandomAccess;

import jadx.api.ICodeWriter;
import jadx.core.codegen.RegionGen;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.IBranchRegion;
import jadx.core.dex.nodes.IContainer;
import jadx.core.dex.nodes.IRegion;
import jadx.core.utils.exceptions.CodegenException;

public final class IfRegion extends ConditionRegion implements IBranchRegion {
	private IContainer thenRegion;
	private IContainer elseRegion;
	private final List<IContainer> subBlocks = Collections.unmodifiableList(new SubBlocksList());

	public IfRegion(IRegion parent) {
		super(parent);
	}

	public IContainer getThenRegion() {
		return thenRegion;
	}

	public void setThenRegion(IContainer thenRegion) {
		this.thenRegion = thenRegion;
	}

	public IContainer getElseRegion() {
		return elseRegion;
	}

	public void setElseRegion(IContainer elseRegion) {
		this.elseRegion = elseRegion;
	}

	public void invert() {
		invertCondition();
		// swap regions
		IContainer tmp = thenRegion;
		thenRegion = elseRegion;
		elseRegion = tmp;
	}

	public int getSourceLine() {
		return getConditionSourceLine();
	}

	@Override
	public List<IContainer> getSubBlocks() {
		return subBlocks;
	}

	private final class SubBlocksList extends AbstractList<IContainer> implements RandomAccess {
		@Override
		public IContainer get(int index) {
			List<BlockNode> conditionBlocks = getConditionBlocks();
			int conditionsCount = conditionBlocks.size();
			if (index < 0) {
				throw new IndexOutOfBoundsException("Index: " + index);
			}
			if (index < conditionsCount) {
				return conditionBlocks.get(index);
			}
			index -= conditionsCount;
			if (thenRegion != null) {
				if (index == 0) {
					return thenRegion;
				}
				index--;
			}
			if (elseRegion != null && index == 0) {
				return elseRegion;
			}
			throw new IndexOutOfBoundsException("Index: " + index);
		}

		@Override
		public int size() {
			return getConditionBlocks().size()
					+ (thenRegion == null ? 0 : 1)
					+ (elseRegion == null ? 0 : 1);
		}
	}

	@Override
	public List<IContainer> getBranches() {
		List<IContainer> branches = new ArrayList<>(2);
		branches.add(thenRegion);
		branches.add(elseRegion);
		return Collections.unmodifiableList(branches);
	}

	@Override
	public boolean replaceSubBlock(IContainer oldBlock, IContainer newBlock) {
		if (oldBlock == thenRegion) {
			thenRegion = newBlock;
			updateParent(thenRegion, this);
			return true;
		}
		if (oldBlock == elseRegion) {
			elseRegion = newBlock;
			updateParent(elseRegion, this);
			return true;
		}
		return false;
	}

	@Override
	public void generate(RegionGen regionGen, ICodeWriter code) throws CodegenException {
		regionGen.makeIf(this, code, true);
	}

	@Override
	public String baseString() {
		StringBuilder sb = new StringBuilder();
		if (thenRegion != null) {
			sb.append(thenRegion.baseString());
		}
		if (elseRegion != null) {
			sb.append(elseRegion.baseString());
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		return "IF " + getConditionBlocks() + " THEN: " + thenRegion + " ELSE: " + elseRegion;
	}
}
