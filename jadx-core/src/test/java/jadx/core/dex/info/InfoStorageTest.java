package jadx.core.dex.info;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.api.JadxArgs;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.nodes.RootNode;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InfoStorageTest {

	@Test
	void testTypeParsingIsScopedAndReused() {
		InfoStorage first = new InfoStorage();
		ArgType firstType = first.getType("Lsample/Value;");

		assertThat(first.getType("Lsample/Value;")).isSameAs(firstType);
		assertThat(firstType.getObject()).isEqualTo("sample.Value");

		InfoStorage second = new InfoStorage();
		assertThat(second.getType("Lsample/Value;")).isNotSameAs(firstType);
	}

	@Test
	void testMethodArgumentsAreLocked() {
		RootNode root = new RootNode(new JadxArgs());
		ClassInfo declClass = ClassInfo.fromType(root, ArgType.object("sample.Test"));
		List<ArgType> source = new ArrayList<>(List.of(ArgType.STRING, ArgType.INT));
		MethodInfo method = MethodInfo.fromDetails(root, declClass, "call", source, ArgType.VOID);
		String shortId = method.getShortId();
		int hash = method.hashCode();

		source.set(0, ArgType.OBJECT);

		assertThat(method.getArgumentsTypes()).containsExactly(ArgType.STRING, ArgType.INT);
		assertThat(method.getShortId()).isEqualTo(shortId);
		assertThat(method.hashCode()).isEqualTo(hash);
		assertThatThrownBy(() -> method.getArgumentsTypes().add(ArgType.LONG))
				.isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	void testRawMethodIdKeepsInputClassNameAfterInnerClassUpdates() {
		RootNode root = new RootNode(new JadxArgs());
		ClassInfo declClass = ClassInfo.fromType(root, ArgType.object("sample.Outer$Inner"));
		MethodInfo method = MethodInfo.fromDetails(root, declClass, "call", List.of(), ArgType.VOID);

		declClass.notInner(root);

		assertThat(method.getRawFullId()).isEqualTo("sample.Outer$Inner.call()V");
	}

	@Test
	void testMethodsByCompositeInputId() {
		RootNode root = new RootNode(new JadxArgs());
		ClassInfo declClass = ClassInfo.fromType(root, ArgType.object("sample.Test"));
		MethodInfo first = MethodInfo.fromDetails(root, declClass, "first", List.of(), ArgType.VOID);
		MethodInfo second = MethodInfo.fromDetails(root, declClass, "second", List.of(), ArgType.VOID);
		InfoStorage storage = root.getInfoStorage();

		int firstId = 0x0001_0020;
		int secondId = 0xFFFF_FFFE;
		storage.putByUniqId(firstId, first);
		storage.putByUniqId(secondId, second);

		assertThat(storage.getByUniqId(firstId)).isSameAs(first);
		assertThat(storage.getByUniqId(secondId)).isSameAs(second);
		assertThat(storage.getByUniqId(0x0001_001F)).isNull();
		assertThat(storage.getByUniqId(0x7FFF_FFFE)).isNull();
	}
}
