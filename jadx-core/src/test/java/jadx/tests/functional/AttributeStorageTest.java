package jadx.tests.functional;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jadx.api.plugins.input.data.attributes.IJadxAttribute;
import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.attributes.AttrList;
import jadx.core.dex.attributes.AttributeStorage;
import jadx.core.dex.attributes.nodes.JadxError;

import static jadx.core.dex.attributes.AFlag.SYNTHETIC;
import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class AttributeStorageTest {
	private AttributeStorage storage;

	@BeforeEach
	public void setup() {
		storage = new AttributeStorage();
	}

	@Test
	public void testAdd() {
		storage.add(SYNTHETIC);
		assertThat(storage.contains(SYNTHETIC)).isTrue();
	}

	@Test
	public void testRemove() {
		storage.add(SYNTHETIC);
		storage.remove(SYNTHETIC);
		assertThat(storage.contains(SYNTHETIC)).isFalse();
	}

	@Test
	public void testAllFlags() {
		for (AFlag flag : AFlag.values()) {
			storage.add(flag);
		}
		for (AFlag flag : AFlag.values()) {
			assertThat(storage.contains(flag)).isTrue();
		}

		storage.clearFlags();
		assertThat(storage.isEmpty()).isTrue();
	}

	@Test
	public void testCopyFlags() {
		AttributeStorage other = new AttributeStorage();
		other.add(SYNTHETIC);

		storage.addAll(other);

		assertThat(storage.contains(SYNTHETIC)).isTrue();
	}

	public static final AType<TestAttr> TEST = new AType<>();
	public static final AType<SecondTestAttr> SECOND_TEST = new AType<>();
	public static final AType<KeepLoadedTestAttr> KEEP_LOADED_TEST = new AType<>();
	public static final AType<SecondKeepLoadedTestAttr> SECOND_KEEP_LOADED_TEST = new AType<>();
	public static final AType<AttrList<String>> TEST_LIST = new AType<>();

	public static class TestAttr implements IJadxAttribute {
		@Override
		public AType<TestAttr> getAttrType() {
			return TEST;
		}
	}

	public static class SecondTestAttr implements IJadxAttribute {
		@Override
		public AType<SecondTestAttr> getAttrType() {
			return SECOND_TEST;
		}
	}

	public static class KeepLoadedTestAttr implements IJadxAttribute {
		@Override
		public AType<KeepLoadedTestAttr> getAttrType() {
			return KEEP_LOADED_TEST;
		}

		@Override
		public boolean keepLoaded() {
			return true;
		}
	}

	public static class SecondKeepLoadedTestAttr implements IJadxAttribute {
		@Override
		public AType<SecondKeepLoadedTestAttr> getAttrType() {
			return SECOND_KEEP_LOADED_TEST;
		}

		@Override
		public boolean keepLoaded() {
			return true;
		}
	}

	@Test
	public void testAddAttribute() {
		TestAttr attr = new TestAttr();
		storage.add(attr);

		assertThat(storage.contains(TEST)).isTrue();
		assertThat(storage.get(TEST)).isEqualTo(attr);
	}

	@Test
	public void testRemoveAttribute() {
		TestAttr attr = new TestAttr();
		storage.add(attr);
		storage.remove(attr);

		assertThat(storage.contains(TEST)).isFalse();
		assertThat(storage.get(TEST)).isNull();
	}

	@Test
	public void testRemoveOtherAttribute() {
		TestAttr attr = new TestAttr();
		storage.add(attr);
		storage.remove(new TestAttr());

		assertThat(storage.contains(TEST)).isTrue();
		assertThat(storage.get(TEST)).isEqualTo(attr);
	}

	@Test
	public void testReplaceSingleAttribute() {
		TestAttr first = new TestAttr();
		TestAttr replacement = new TestAttr();
		storage.add(first);
		storage.add(replacement);

		assertThat(storage.get(TEST)).isEqualTo(replacement);
	}

	@Test
	public void testPromoteAndDemoteAttributeMap() {
		TestAttr first = new TestAttr();
		SecondTestAttr second = new SecondTestAttr();
		storage.add(first);
		storage.add(second);

		assertThat(storage.get(TEST)).isEqualTo(first);
		assertThat(storage.get(SECOND_TEST)).isEqualTo(second);

		storage.remove(TEST);
		assertThat(storage.get(TEST)).isNull();
		assertThat(storage.get(SECOND_TEST)).isEqualTo(second);

		storage.remove(second);
		assertThat(storage.isEmpty()).isTrue();
	}

	@Test
	public void testCopySingleAndMultipleAttributes() {
		AttributeStorage single = new AttributeStorage();
		TestAttr first = new TestAttr();
		single.add(first);
		storage.addAll(single);

		AttributeStorage multiple = new AttributeStorage();
		TestAttr replacement = new TestAttr();
		SecondTestAttr second = new SecondTestAttr();
		multiple.add(replacement);
		multiple.add(second);
		storage.addAll(multiple);

		assertThat(storage.get(TEST)).isEqualTo(replacement);
		assertThat(storage.get(SECOND_TEST)).isEqualTo(second);
	}

	@Test
	public void testGetAllReusesReadOnlyView() {
		storage.add(TEST_LIST, "first");

		List<String> firstView = storage.getAll(TEST_LIST);
		List<String> secondView = storage.getAll(TEST_LIST);
		assertThat(secondView).isSameAs(firstView);

		storage.add(TEST_LIST, "second");
		assertThat(firstView).containsExactly("first", "second");
		assertThatExceptionOfType(UnsupportedOperationException.class)
				.isThrownBy(() -> firstView.add("forbidden"));
	}

	@Test
	public void testUnloadSingleAndMultipleAttributes() {
		KeepLoadedTestAttr keep = new KeepLoadedTestAttr();
		SecondKeepLoadedTestAttr secondKeep = new SecondKeepLoadedTestAttr();
		storage.add(new TestAttr());
		storage.add(keep);
		storage.add(secondKeep);

		storage.unloadAttributes();
		assertThat(storage.get(TEST)).isNull();
		assertThat(storage.get(KEEP_LOADED_TEST)).isSameAs(keep);
		assertThat(storage.get(SECOND_KEEP_LOADED_TEST)).isSameAs(secondKeep);

		storage.remove(keep);
		storage.remove(secondKeep);
		storage.add(new TestAttr());
		storage.unloadAttributes();
		assertThat(storage.isEmpty()).isTrue();
	}

	@Test
	public void testJadxErrorFlagMirrorLifecycle() {
		storage.add(AType.JADX_ERROR, new JadxError("test", null));
		assertThat(storage.contains(AFlag.JADX_ERROR)).isTrue();
		assertThat(storage.contains(AType.JADX_ERROR)).isTrue();

		storage.remove(AType.JADX_ERROR);
		assertThat(storage.contains(AFlag.JADX_ERROR)).isFalse();
		assertThat(storage.contains(AType.JADX_ERROR)).isFalse();

		storage.add(AType.JADX_ERROR, new JadxError("test", null));
		storage.unloadAttributes();
		assertThat(storage.contains(AFlag.JADX_ERROR)).isFalse();
		assertThat(storage.contains(AType.JADX_ERROR)).isFalse();
	}
}
