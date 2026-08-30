package jadx.core.dex.instructions.args;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static jadx.core.dex.instructions.args.ArgType.WildcardBound.SUPER;
import static jadx.core.dex.instructions.args.ArgType.generic;
import static jadx.core.dex.instructions.args.ArgType.genericType;
import static jadx.core.dex.instructions.args.ArgType.wildcard;
import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArgTypeTest {

	@Test
	public void testEqualsOfGenericTypes() {
		ArgType first = ArgType.generic("java.lang.List", ArgType.STRING);
		ArgType second = ArgType.generic("Ljava/lang/List;", ArgType.STRING);

		assertThat(first).isEqualTo(second);
	}

	@Test
	void testContainsGenericType() {
		ArgType wildcard = wildcard(genericType("T"), SUPER);
		assertThat(wildcard.containsTypeVariable()).isTrue();

		ArgType type = generic("java.lang.List", wildcard);
		assertThat(type.containsTypeVariable()).isTrue();
	}

	@Test
	void testInnerGeneric() {
		ArgType[] genericTypes = new ArgType[] { ArgType.genericType("K"), ArgType.genericType("V") };
		ArgType base = ArgType.generic("java.util.Map", genericTypes);

		ArgType genericInner = ArgType.outerGeneric(base, ArgType.generic("Entry", genericTypes));
		assertThat(genericInner.toString()).isEqualTo("java.util.Map<K, V>$Entry<K, V>");
		assertThat(genericInner.containsTypeVariable()).isTrue();

		ArgType genericInner2 = ArgType.outerGeneric(base, ArgType.object("Entry"));
		assertThat(genericInner2.toString()).isEqualTo("java.util.Map<K, V>$Entry");
		assertThat(genericInner2.containsTypeVariable()).isTrue();
	}

	@Test
	void testGenericTypesAreLocked() {
		List<ArgType> source = new ArrayList<>(List.of(ArgType.STRING, ArgType.INT));
		ArgType type = generic("java.util.Map", source);
		int hash = type.hashCode();

		source.set(0, ArgType.OBJECT);

		assertThat(type.getGenericTypes()).containsExactly(ArgType.STRING, ArgType.INT);
		assertThat(type.hashCode()).isEqualTo(hash);
		assertThatThrownBy(() -> type.getGenericTypes().add(ArgType.OBJECT))
				.isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	void testGenericTypeBoundsAreLocked() {
		List<ArgType> source = new ArrayList<>(List.of(ArgType.STRING, ArgType.INT));
		ArgType type = genericType("T", source);
		int hash = type.hashCode();

		source.set(0, ArgType.OBJECT);

		assertThat(type.getExtendTypes()).containsExactly(ArgType.STRING, ArgType.INT);
		assertThat(type.hashCode()).isEqualTo(hash);
		assertThatThrownBy(() -> type.getExtendTypes().add(ArgType.OBJECT))
				.isInstanceOf(UnsupportedOperationException.class);

		List<ArgType> replacement = new ArrayList<>(List.of(ArgType.LONG));
		type.setExtendTypes(replacement);
		replacement.set(0, ArgType.DOUBLE);

		assertThat(type.getExtendTypes()).containsExactly(ArgType.LONG);
	}
}
