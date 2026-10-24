package jadx.core.dex.instructions;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.api.JadxArgs;
import jadx.api.plugins.input.data.annotations.EncodedType;
import jadx.api.plugins.input.data.annotations.EncodedValue;
import jadx.core.dex.info.ClassInfo;
import jadx.core.dex.info.MethodInfo;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.nodes.RootNode;

import static org.assertj.core.api.Assertions.assertThat;

class InvokeCustomRawNodeTest {
	@Test
	void distinguishesCallSitesWithDifferentBootstrapValues() {
		RootNode root = new RootNode(new JadxArgs());
		ClassInfo cls = ClassInfo.fromName(root, "test.CallSite");
		MethodInfo method = MethodInfo.fromDetails(root, cls, "call", List.of(), ArgType.VOID);
		InvokeNode resolve = new InvokeNode(method, InvokeType.STATIC, 0);
		InvokeCustomRawNode first = new InvokeCustomRawNode(resolve, method, InvokeType.CUSTOM_RAW, 0);
		InvokeCustomRawNode second = new InvokeCustomRawNode(resolve, method, InvokeType.CUSTOM_RAW, 0);
		first.setCallSiteValues(List.of(new EncodedValue(EncodedType.ENCODED_STRING, "first")));
		second.setCallSiteValues(List.of(new EncodedValue(EncodedType.ENCODED_STRING, "second")));

		assertThat(first.isSame(second)).isFalse();
	}

	@Test
	void distinguishesResolveArgumentsWithSameBootstrapValues() {
		RootNode root = new RootNode(new JadxArgs());
		ClassInfo cls = ClassInfo.fromName(root, "test.CallSite");
		MethodInfo method = MethodInfo.fromDetails(root, cls, "call", List.of(), ArgType.VOID);
		MethodInfo resolveMethod = MethodInfo.fromDetails(root, cls, "resolve", List.of(ArgType.INT), ArgType.VOID);
		InvokeNode firstResolve = new InvokeNode(resolveMethod, InvokeType.STATIC, 1);
		firstResolve.addArg(InsnArg.lit(1, ArgType.INT));
		InvokeNode secondResolve = new InvokeNode(resolveMethod, InvokeType.STATIC, 1);
		secondResolve.addArg(InsnArg.lit(2, ArgType.INT));
		InvokeCustomRawNode first = new InvokeCustomRawNode(firstResolve, method, InvokeType.CUSTOM_RAW, 0);
		InvokeCustomRawNode second = new InvokeCustomRawNode(secondResolve, method, InvokeType.CUSTOM_RAW, 0);
		first.setCallSiteValues(List.of(new EncodedValue(EncodedType.ENCODED_STRING, "same")));
		second.setCallSiteValues(List.of(new EncodedValue(EncodedType.ENCODED_STRING, "same")));

		assertThat(first.isSame(second)).isFalse();
	}
}
