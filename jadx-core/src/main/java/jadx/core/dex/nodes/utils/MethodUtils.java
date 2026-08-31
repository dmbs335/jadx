package jadx.core.dex.nodes.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import jadx.core.clsp.ClspClass;
import jadx.core.clsp.ClspMethod;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.attributes.nodes.MethodBridgeAttr;
import jadx.core.dex.attributes.nodes.MethodOverrideAttr;
import jadx.core.dex.attributes.nodes.SkipMethodArgsAttr;
import jadx.core.dex.info.ClassInfo;
import jadx.core.dex.info.MethodInfo;
import jadx.core.dex.instructions.BaseInvokeNode;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.nodes.ClassNode;
import jadx.core.dex.nodes.IMethodDetails;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.nodes.RootNode;
import jadx.core.utils.Utils;

public class MethodUtils {
	private final RootNode root;

	public MethodUtils(RootNode rootNode) {
		this.root = rootNode;
	}

	@Nullable
	public IMethodDetails getMethodDetails(BaseInvokeNode invokeNode) {
		IMethodDetails methodDetails = invokeNode.get(AType.METHOD_DETAILS);
		if (methodDetails != null) {
			return methodDetails;
		}
		return getMethodDetails(invokeNode.getCallMth());
	}

	@Nullable
	public IMethodDetails getMethodDetails(MethodInfo callMth) {
		MethodNode mthNode = root.resolveMethod(callMth);
		if (mthNode != null) {
			return mthNode;
		}
		return root.getClsp().getMethodDetails(callMth);
	}

	@Nullable
	public MethodNode resolveMethod(BaseInvokeNode invokeNode) {
		IMethodDetails methodDetails = getMethodDetails(invokeNode);
		if (methodDetails instanceof MethodNode) {
			return (MethodNode) methodDetails;
		}
		return null;
	}

	public boolean isSkipArg(BaseInvokeNode invokeNode, InsnArg arg) {
		MethodNode mth = resolveMethod(invokeNode);
		if (mth == null) {
			return false;
		}
		SkipMethodArgsAttr skipArgsAttr = mth.get(AType.SKIP_MTH_ARGS);
		if (skipArgsAttr == null) {
			return false;
		}
		int argIndex = invokeNode.getArgIndex(arg);
		return skipArgsAttr.isSkip(argIndex);
	}

	/**
	 * Search methods with same name and args count in class hierarchy starting from {@code startCls}
	 * Beware {@code startCls} can be different from {@code mthInfo.getDeclClass()}
	 */
	public boolean isMethodArgsOverloaded(ArgType startCls, MethodInfo mthInfo) {
		return processMethodArgsOverloaded(startCls, mthInfo, null, null);
	}

	public List<IMethodDetails> collectOverloadedMethods(ArgType startCls, MethodInfo mthInfo) {
		List<IMethodDetails> list = new ArrayList<>();
		processMethodArgsOverloaded(startCls, mthInfo, list, null);
		return list;
	}

	@Nullable
	public ArgType getMethodGenericReturnType(BaseInvokeNode invokeNode) {
		IMethodDetails methodDetails = getMethodDetails(invokeNode);
		if (methodDetails != null) {
			ArgType returnType = methodDetails.getReturnType();
			if (returnType != null && returnType.containsGeneric()) {
				return returnType;
			}
		}
		return null;
	}

	private boolean processMethodArgsOverloaded(ArgType startCls, MethodInfo mthInfo,
			@Nullable List<IMethodDetails> collectedMths, @Nullable Set<ArgType> visited) {
		if (startCls == null || !startCls.isObject()) {
			return false;
		}
		if (visited != null && !visited.add(startCls)) {
			return false;
		}
		boolean isMthConstructor = mthInfo.isConstructor() || mthInfo.isClassInit();
		ClassNode classNode = root.resolveClass(startCls);
		if (classNode != null) {
			List<MethodNode> methods = classNode.getMethods();
			int methodsCount = methods.size();
			for (int methodIndex = 0; methodIndex < methodsCount; methodIndex++) {
				MethodNode mth = methods.get(methodIndex);
				if (mthInfo.isOverloadedBy(mth.getMethodInfo())) {
					if (collectedMths == null) {
						return true;
					}
					collectedMths.add(mth);
				}
			}
			if (!isMthConstructor) {
				List<ArgType> interfaces = classNode.getInterfaces();
				Set<ArgType> hierarchyVisited = ensureVisitedForBranch(visited, startCls,
						(classNode.getSuperClass() == null ? 0 : 1) + interfaces.size());
				if (processMethodArgsOverloaded(classNode.getSuperClass(), mthInfo, collectedMths, hierarchyVisited)) {
					if (collectedMths == null) {
						return true;
					}
				}
				int interfacesCount = interfaces.size();
				for (int interfaceIndex = 0; interfaceIndex < interfacesCount; interfaceIndex++) {
					if (processMethodArgsOverloaded(
							interfaces.get(interfaceIndex), mthInfo, collectedMths, hierarchyVisited)) {
						if (collectedMths == null) {
							return true;
						}
					}
				}
			}
		} else {
			ClspClass clsDetails = root.getClsp().getClsDetails(startCls);
			if (clsDetails == null) {
				// class info not available
				return false;
			}
			ClspMethod[] methods = clsDetails.getMethodsArray();
			for (int methodIndex = 0; methodIndex < methods.length; methodIndex++) {
				ClspMethod clspMth = methods[methodIndex];
				if (mthInfo.isOverloadedBy(clspMth.getMethodInfo())) {
					if (collectedMths == null) {
						return true;
					}
					collectedMths.add(clspMth);
				}
			}
			if (!isMthConstructor) {
				ArgType[] parents = clsDetails.getParents();
				Set<ArgType> hierarchyVisited = ensureVisitedForBranch(visited, startCls, parents.length);
				for (ArgType parent : parents) {
					if (processMethodArgsOverloaded(parent, mthInfo, collectedMths, hierarchyVisited)) {
						if (collectedMths == null) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	private static @Nullable Set<ArgType> ensureVisitedForBranch(
			@Nullable Set<ArgType> visited, ArgType currentType, int parentCount) {
		if (visited != null || parentCount < 2) {
			return visited;
		}
		Set<ArgType> newVisited = new HashSet<>(8);
		newVisited.add(currentType);
		return newVisited;
	}

	@Nullable
	public IMethodDetails getOverrideBaseMth(MethodNode mth) {
		MethodOverrideAttr overrideAttr = mth.get(AType.METHOD_OVERRIDE);
		if (overrideAttr == null) {
			return null;
		}
		return Utils.getOne(overrideAttr.getBaseMethods());
	}

	public ClassInfo getMethodOriginDeclClass(MethodNode mth) {
		IMethodDetails baseMth = getOverrideBaseMth(mth);
		if (baseMth != null) {
			return baseMth.getMethodInfo().getDeclClass();
		}
		MethodBridgeAttr bridgeAttr = mth.get(AType.BRIDGED_BY);
		if (bridgeAttr != null) {
			return getMethodOriginDeclClass(bridgeAttr.getBridgeMth());
		}
		return mth.getMethodInfo().getDeclClass();
	}
}
