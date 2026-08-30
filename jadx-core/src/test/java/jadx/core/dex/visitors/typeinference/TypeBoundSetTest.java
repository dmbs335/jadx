package jadx.core.dex.visitors.typeinference;

import java.util.ConcurrentModificationException;
import java.util.Iterator;

import org.junit.jupiter.api.Test;

import jadx.core.dex.instructions.args.ArgType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TypeBoundSetTest {

	@Test
	void preservesInsertionOrderAndSetSemantics() {
		TypeBoundSet set = new TypeBoundSet();
		ITypeBound first = new TypeBoundConst(BoundEnum.ASSIGN, ArgType.INT);
		ITypeBound second = new TypeBoundConst(BoundEnum.USE, ArgType.STRING);

		assertThat(set.add(first)).isTrue();
		assertThat(set.add(second)).isTrue();
		assertThat(set.add(new TypeBoundConst(BoundEnum.ASSIGN, ArgType.INT))).isFalse();
		assertThat(set).containsExactly(first, second);
		assertThat(set.get(0)).isSameAs(first);
		assertThat(set.get(1)).isSameAs(second);
	}

	@Test
	void iteratorRemoveKeepsOrder() {
		TypeBoundSet set = new TypeBoundSet();
		ITypeBound first = new TypeBoundConst(BoundEnum.ASSIGN, ArgType.INT);
		ITypeBound second = new TypeBoundConst(BoundEnum.USE, ArgType.STRING);
		ITypeBound third = new TypeBoundConst(BoundEnum.USE, ArgType.BOOLEAN);
		set.add(first);
		set.add(second);
		set.add(third);

		Iterator<ITypeBound> iterator = set.iterator();
		assertThat(iterator.next()).isSameAs(first);
		iterator.remove();

		assertThat(set).containsExactly(second, third);
	}

	@Test
	void iteratorIsFailFast() {
		TypeBoundSet set = new TypeBoundSet();
		set.add(new TypeBoundConst(BoundEnum.ASSIGN, ArgType.INT));
		Iterator<ITypeBound> iterator = set.iterator();
		set.add(new TypeBoundConst(BoundEnum.USE, ArgType.STRING));

		assertThatThrownBy(iterator::next).isInstanceOf(ConcurrentModificationException.class);
	}
}
