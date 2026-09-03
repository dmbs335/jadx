package jadx.core.dex.visitors.regions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.attributes.nodes.CodeFeaturesAttr;
import jadx.core.dex.attributes.nodes.RegionRefAttr;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.nodes.IBlock;
import jadx.core.dex.nodes.IBranchRegion;
import jadx.core.dex.nodes.IContainer;
import jadx.core.dex.nodes.IRegion;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.regions.SwitchRegion;
import jadx.core.dex.regions.TryCatchRegion;
import jadx.core.dex.visitors.AbstractVisitor;
import jadx.core.dex.visitors.JadxVisitor;
import jadx.core.dex.visitors.regions.maker.SwitchRegionMaker;
import jadx.core.utils.BlockInsnPair;
import jadx.core.utils.BlockParentContainer;
import jadx.core.utils.BlockUtils;
import jadx.core.utils.ListUtils;
import jadx.core.utils.RegionUtils;
import jadx.core.utils.exceptions.JadxException;

import static jadx.core.dex.attributes.nodes.CodeFeaturesAttr.CodeFeature.SWITCH;

@JadxVisitor(
		name = "SwitchBreakVisitor",
		desc = "Optimize 'break' instruction: common code extract, remove unreachable",
		runAfter = LoopRegionVisitor.class // can add 'continue' at case end
)
public class SwitchBreakVisitor extends AbstractVisitor {

	@Override
	public void visit(MethodNode mth) throws JadxException {
		if (CodeFeaturesAttr.contains(mth, SWITCH)) {
			runSwitchTraverse(mth, ExtractCommonBreak::new);
			runSwitchTraverse(mth, RemoveUnreachableBreak::new);
			IfRegionVisitor.processIfRequested(mth);
		}
	}

	private static void runSwitchTraverse(MethodNode mth, Supplier<BaseSwitchRegionVisitor> builder) {
		DepthRegionTraversal.traverse(mth, new IterativeSwitchRegionVisitor(builder));
	}

	/**
	 * Add common 'break' if 'break' or exit insn ('return', 'throw', 'continue') found in all branches.
	 * Remove exist common break if all branches contain exit insn.
	 */
	private static final class ExtractCommonBreak extends BaseSwitchRegionVisitor {
		@Override
		public void processRegion(MethodNode mth, IRegion region) {
			if (region instanceof IBranchRegion && !(region instanceof SwitchRegion)) {
				// if break in all branches extract to parent region
				processBranchRegion(mth, region);
			}
		}

		private void processBranchRegion(MethodNode mth, IRegion region) {
			IRegion parentRegion = region.getParent();
			if (parentRegion.contains(AFlag.FALL_THROUGH)) {
				// fallthrough case, can't extract break
				return;
			}
			boolean dontAddCommonBreak = false;
			IBlock lastParentBlock = RegionUtils.getLastBlock(parentRegion);
			if (BlockUtils.containsExitInsn(lastParentBlock)) {
				if (isBreakBlock(lastParentBlock)) {
					// parent block already contains 'break'
					dontAddCommonBreak = true;
				} else {
					// can't add 'break' after 'return', 'throw' or 'continue'
					return;
				}
			}
			List<IContainer> branches = ((IBranchRegion) region).getBranches();
			boolean hasNormalFallThroughBranch = branches.stream()
					.filter(branch -> branch != null && !RegionUtils.hasExitBlock(branch))
					.map(RegionUtils::getLastInsnWithBlock)
					.anyMatch(last -> last != null && !isBreakBlock(last.getBlock()));
			long nonExitBreakBranches = branches.stream()
					.filter(branch -> branch != null && !RegionUtils.hasExitBlock(branch))
					.map(RegionUtils::getLastInsnWithBlock)
					.filter(last -> last != null && isBreakBlock(last.getBlock()))
					.count();
			boolean soleExceptionalBreak = !hasNormalFallThroughBranch && nonExitBreakBranches == 1;
			boolean handlerConditional = !(region instanceof TryCatchRegion)
					&& containsExceptionHandlerPath(mth, region);
			boolean removeCommonBreak = true; // all branches contain exit insns, common break is unreachable
			List<BlockParentContainer> forBreakRemove = new ArrayList<>();
			for (IContainer branch : branches) {
				if (branch == null) {
					removeCommonBreak = false;
					continue;
				}
				BlockInsnPair last = RegionUtils.getLastInsnWithBlock(branch);
				if (last == null) {
					return;
				}
				InsnNode lastInsn = last.getInsn();
				boolean branchExits = RegionUtils.hasExitBlock(branch);
				boolean catchBranch = region instanceof TryCatchRegion
						&& branch != ((TryCatchRegion) region).getTryRegion();
				boolean unsafeExceptionalMove = soleExceptionalBreak && (catchBranch || handlerConditional);
				if ((branchExits || !unsafeExceptionalMove)
						&& lastInsn.getType() == InsnType.BREAK
						&& isBreakBlock(last.getBlock())) {
					IBlock block = last.getBlock();
					IContainer parent = RegionUtils.getBlockContainer(branch, block);
					forBreakRemove.add(new BlockParentContainer(parent, block));
					removeCommonBreak = false;
				} else if (!branchExits) {
					removeCommonBreak = false;
				}
			}
			if (!forBreakRemove.isEmpty()) {
				// common 'break' confirmed
				for (BlockParentContainer breakData : forBreakRemove) {
					removeBreak(breakData.getBlock(), breakData.getParent());
				}
				if (!dontAddCommonBreak) {
					addBreakRegion.add(parentRegion);
					// new 'break' might become 'common' for upper branch region, request to run checks again
					requestReRun();
				}
				// removed 'break' may allow to use 'else-if' chain
				mth.add(AFlag.REQUEST_IF_REGION_OPTIMIZE);
			}
			if (removeCommonBreak && lastParentBlock != null) {
				removeBreak(lastParentBlock, parentRegion);
			}
		}

		private static boolean containsExceptionHandlerPath(MethodNode mth, IContainer container) {
			boolean[] found = { false };
			RegionUtils.visitBlockNodes(mth, container, block -> {
				if (BlockUtils.isExceptionHandlerPath(block)) {
					found[0] = true;
				}
			});
			return found[0];
		}
	}

	private static final class RemoveUnreachableBreak extends BaseSwitchRegionVisitor {
		@Override
		public void processRegion(MethodNode mth, IRegion region) {
			List<IContainer> subBlocks = region.getSubBlocks();
			IContainer lastContainer = ListUtils.last(subBlocks);
			if (lastContainer instanceof IBlock) {
				IBlock block = (IBlock) lastContainer;
				if (isBreakBlock(block) && isPrevInsnIsExit(block, subBlocks)) {
					removeBreak(block, region);
				}
			}
		}

		private boolean isPrevInsnIsExit(IBlock breakBlock, List<IContainer> subBlocks) {
			InsnNode prevInsn = null;
			if (breakBlock.getInstructions().size() > 1) {
				// check prev insn in same block
				List<InsnNode> insns = breakBlock.getInstructions();
				prevInsn = insns.get(insns.size() - 2);
			} else if (subBlocks.size() > 1) {
				IContainer prev = subBlocks.get(subBlocks.size() - 2);
				if (prev instanceof IBranchRegion && RegionUtils.hasExitBlock(prev)) {
					return true;
				}
				prevInsn = getLastGeneratedInsn(prev);
			}
			return prevInsn != null && prevInsn.isExitEdgeInsn();
		}

		private @Nullable InsnNode getLastGeneratedInsn(IContainer container) {
			if (container instanceof IBlock) {
				List<InsnNode> insns = ((IBlock) container).getInstructions();
				for (int i = insns.size() - 1; i >= 0; i--) {
					InsnNode insn = insns.get(i);
					if (!insn.contains(AFlag.DONT_GENERATE)) {
						return insn;
					}
				}
				return null;
			}
			if (container instanceof IBranchRegion) {
				return null;
			}
			if (container instanceof IRegion) {
				List<IContainer> blocks = ((IRegion) container).getSubBlocks();
				for (int i = blocks.size() - 1; i >= 0; i--) {
					InsnNode insn = getLastGeneratedInsn(blocks.get(i));
					if (insn != null) {
						return insn;
					}
				}
			}
			return null;
		}
	}

	/**
	 * For every 'switch' region run new instance of provided 'switch' visitor.
	 * If rerun requested, run traverse for that visitor again.
	 */
	private static final class IterativeSwitchRegionVisitor extends AbstractRegionVisitor {
		private final Supplier<BaseSwitchRegionVisitor> builder;

		public IterativeSwitchRegionVisitor(Supplier<BaseSwitchRegionVisitor> builder) {
			this.builder = builder;
		}

		@Override
		public void leaveRegion(MethodNode mth, IRegion region) {
			if (region instanceof SwitchRegion) {
				SwitchRegion switchRegion = (SwitchRegion) region;
				BaseSwitchRegionVisitor switchVisitor = builder.get();
				switchVisitor.setCurrentSwitch(switchRegion);
				boolean runAgain;
				int k = 0;
				do {
					runAgain = false;
					DepthRegionTraversal.traverse(mth, switchRegion, switchVisitor);
					if (switchVisitor.isReRunRequested()) {
						switchVisitor.reset();
						runAgain = true;
					}
					if (k++ > 20) {
						// 20 nested 'if' are not expected
						mth.addWarnComment("Unexpected iteration count in SwitchBreakVisitor. Please report as an issue");
						break;
					}
				} while (runAgain);
			}
		}
	}

	private abstract static class BaseSwitchRegionVisitor extends AbstractRegionVisitor {
		protected final Set<IRegion> addBreakRegion = new HashSet<>();
		protected final Set<IContainer> cleanupSet = new HashSet<>();
		protected SwitchRegion currentSwitch;
		private boolean reRunRequested = false;

		public abstract void processRegion(MethodNode mth, IRegion region);

		@Override
		public boolean enterRegion(MethodNode mth, IRegion region) {
			processRegion(mth, region);
			return true;
		}

		@Override
		public void leaveRegion(MethodNode mth, IRegion region) {
			if (addBreakRegion.contains(region)) {
				addBreakRegion.remove(region);
				region.getSubBlocks().add(SwitchRegionMaker.buildBreakContainer(currentSwitch));
			}
			if (cleanupSet.contains(region)) {
				cleanupSet.remove(region);
				region.getSubBlocks().removeIf(r -> r.contains(AFlag.REMOVE));
			}
		}

		/**
		 * Method called before visitor rerun
		 */
		public void reset() {
			reRunRequested = false;
			addBreakRegion.clear();
			cleanupSet.clear();
		}

		public void requestReRun() {
			reRunRequested = true;
		}

		public boolean isReRunRequested() {
			return reRunRequested;
		}

		public void setCurrentSwitch(SwitchRegion currentSwitch) {
			this.currentSwitch = currentSwitch;
		}

		protected boolean isBreakBlock(@Nullable IBlock block) {
			if (block != null) {
				InsnNode lastInsn = ListUtils.last(block.getInstructions());
				if (lastInsn != null && lastInsn.getType() == InsnType.BREAK) {
					RegionRefAttr regionRefAttr = lastInsn.get(AType.REGION_REF);
					return regionRefAttr != null && regionRefAttr.getRegion() == currentSwitch;
				}
			}
			return false;
		}

		protected void removeBreak(IBlock breakBlock, IContainer parentContainer) {
			List<InsnNode> instructions = breakBlock.getInstructions();
			InsnNode last = ListUtils.last(instructions);
			if (last != null && last.getType() == InsnType.BREAK) {
				ListUtils.removeLast(instructions);
				if (instructions.isEmpty()) {
					breakBlock.add(AFlag.REMOVE);
					cleanupSet.add(parentContainer);
				}
			}
		}
	}

	@Override
	public String getName() {
		return "SwitchBreakVisitor";
	}
}
