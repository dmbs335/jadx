package jadx.core.dex.visitors.regions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.IBlock;
import jadx.core.dex.nodes.IRegion;
import jadx.core.dex.nodes.InsnContainer;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.regions.Region;

import static org.assertj.core.api.Assertions.assertThat;

class DepthRegionTraversalTest {

	@Test
	void useRegionTreeSizeForDuplicatedControlFlow() {
		assertThat(DepthRegionTraversal.calcIterativeLimit(193, 700)).isEqualTo(1400);
	}

	@Test
	void keepBlockBasedFloorForRegularMethods() {
		assertThat(DepthRegionTraversal.calcIterativeLimit(100, 20)).isEqualTo(500);
	}

	@Test
	void preserveVisitOrderAndNestedTraversal() {
		Region root = new Region(null);
		InsnContainer first = new InsnContainer(Collections.emptyList());
		Region nested = new Region(root);
		InsnContainer inner = new InsnContainer(Collections.emptyList());
		InsnContainer last = new InsnContainer(Collections.emptyList());
		root.add(first);
		nested.add(inner);
		root.add(nested);
		root.add(last);

		Map<Object, String> names = new IdentityHashMap<>();
		names.put(root, "root");
		names.put(first, "first");
		names.put(nested, "nested");
		names.put(inner, "inner");
		names.put(last, "last");
		List<String> events = new ArrayList<>();
		DepthRegionTraversal.traverse(null, root, new IRegionVisitor() {
			@Override
			public void processBlock(MethodNode mth, IBlock block) {
				events.add("block:" + names.get(block));
			}

			@Override
			public boolean enterRegion(MethodNode mth, IRegion region) {
				events.add("enter:" + names.get(region));
				if (region == nested) {
					DepthRegionTraversal.traverse(null, inner, new RecordingVisitor(events, names, "reentrant:"));
				}
				return true;
			}

			@Override
			public void leaveRegion(MethodNode mth, IRegion region) {
				events.add("leave:" + names.get(region));
			}
		});

		assertThat(events).containsExactly(
				"enter:root",
				"block:first",
				"enter:nested",
				"reentrant:block:inner",
				"block:inner",
				"leave:nested",
				"block:last",
				"leave:root");
	}

	private static final class RecordingVisitor implements IRegionVisitor {
		private final List<String> events;
		private final Map<Object, String> names;
		private final String prefix;

		private RecordingVisitor(List<String> events, Map<Object, String> names, String prefix) {
			this.events = events;
			this.names = names;
			this.prefix = prefix;
		}

		@Override
		public void processBlock(MethodNode mth, IBlock block) {
			events.add(prefix + "block:" + names.get(block));
		}

		@Override
		public boolean enterRegion(MethodNode mth, IRegion region) {
			events.add(prefix + "enter:" + names.get(region));
			return true;
		}

		@Override
		public void leaveRegion(MethodNode mth, IRegion region) {
			events.add(prefix + "leave:" + names.get(region));
		}
	}
}
