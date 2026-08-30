package jadx.core.dex.visitors.regions;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.instructions.args.ArgType;

import static org.assertj.core.api.Assertions.assertThat;

class LoopRegionVisitorTest {

	@Test
	void matchesGenericAndRawTypesByObjectName() {
		ArgType genericEntry = ArgType.generic("java.util.Map$Entry", ArgType.STRING, ArgType.INT);
		ArgType rawEntry = ArgType.object("java.util.Map$Entry");

		assertThat(LoopRegionVisitor.isSameRawObjectType(genericEntry, rawEntry)).isTrue();
		assertThat(LoopRegionVisitor.isSameRawObjectType(genericEntry, ArgType.object("java.lang.ref.WeakReference"))).isFalse();
	}

	@Test
	void matchesWildcardBoundAndRawTypesByObjectName() {
		ArgType genericItem = ArgType.generic(
				"test.Item",
				ArgType.wildcard(ArgType.object("test.Style"), ArgType.WildcardBound.EXTENDS));
		ArgType wildcardItem = ArgType.wildcard(genericItem, ArgType.WildcardBound.EXTENDS);

		assertThat(LoopRegionVisitor.isSameRawExtendsWildcardType(
				wildcardItem, ArgType.object("test.Item"))).isTrue();
		assertThat(LoopRegionVisitor.isSameRawExtendsWildcardType(
				wildcardItem, ArgType.object("test.Other"))).isFalse();
		assertThat(LoopRegionVisitor.isSameRawExtendsWildcardType(
				ArgType.wildcard(genericItem, ArgType.WildcardBound.SUPER),
				ArgType.object("test.Item"))).isFalse();
		assertThat(LoopRegionVisitor.isSameRawExtendsWildcardType(
				ArgType.wildcard(ArgType.object("test.Item"), ArgType.WildcardBound.EXTENDS),
				ArgType.object("test.Item"))).isFalse();
	}

	@Test
	void matchesBoundedWildcardAndRawTypesByObjectName() {
		ArgType genericItem = ArgType.generic("test.Item", ArgType.STRING);
		ArgType boundedType = ArgType.genericType("T", genericItem);
		ArgType wildcardItem = ArgType.wildcard(boundedType, ArgType.WildcardBound.EXTENDS);

		assertThat(LoopRegionVisitor.isSameRawBoundedWildcardType(
				wildcardItem, ArgType.object("test.Item"))).isTrue();
		assertThat(LoopRegionVisitor.isSameRawBoundedWildcardType(
				wildcardItem, ArgType.object("test.Other"))).isFalse();
		assertThat(LoopRegionVisitor.isSameRawBoundedWildcardType(
				ArgType.wildcard(boundedType, ArgType.WildcardBound.SUPER),
				ArgType.object("test.Item"))).isFalse();
		assertThat(LoopRegionVisitor.isSameRawBoundedWildcardType(
				ArgType.wildcard(ArgType.genericType("T"), ArgType.WildcardBound.EXTENDS),
				ArgType.object("test.Item"))).isFalse();
		assertThat(LoopRegionVisitor.isSameRawBoundedWildcardType(
				ArgType.wildcard(
						ArgType.genericType("T", ArgType.object("test.Item")),
						ArgType.WildcardBound.EXTENDS),
				ArgType.object("test.Item"))).isFalse();
		assertThat(LoopRegionVisitor.isSameRawBoundedWildcardType(
				ArgType.wildcard(
						ArgType.genericType("T", List.of(genericItem, ArgType.object("test.Marker"))),
						ArgType.WildcardBound.EXTENDS),
				ArgType.object("test.Item"))).isFalse();
		assertThat(LoopRegionVisitor.isSameRawBoundedWildcardType(
				wildcardItem, genericItem)).isFalse();
	}

	@Test
	void detectsOnlyUnknownObjectWildcards() {
		assertThat(LoopRegionVisitor.isUnknownObjectWildcardType(ArgType.wildcard())).isTrue();
		assertThat(LoopRegionVisitor.isUnknownObjectWildcardType(
				ArgType.wildcard(ArgType.OBJECT, ArgType.WildcardBound.EXTENDS))).isTrue();

		assertThat(LoopRegionVisitor.isUnknownObjectWildcardType(
				ArgType.wildcard(ArgType.OBJECT, ArgType.WildcardBound.SUPER))).isFalse();
		assertThat(LoopRegionVisitor.isUnknownObjectWildcardType(
				ArgType.wildcard(ArgType.STRING, ArgType.WildcardBound.EXTENDS))).isFalse();
		assertThat(LoopRegionVisitor.isUnknownObjectWildcardType(ArgType.genericType("T"))).isFalse();
		assertThat(LoopRegionVisitor.isUnknownObjectWildcardType(ArgType.OBJECT)).isFalse();
	}

	@Test
	void checksNarrowingOnlyForConcreteNonGenericTypes() {
		ArgType parent = ArgType.object("test.Parent");
		ArgType child = ArgType.object("test.Child");

		assertThat(LoopRegionVisitor.canCheckKnownNarrowingType(parent, child)).isTrue();
		assertThat(LoopRegionVisitor.canCheckKnownNarrowingType(
				ArgType.generic(parent, ArgType.STRING), child)).isFalse();
		assertThat(LoopRegionVisitor.canCheckKnownNarrowingType(
				ArgType.wildcard(parent, ArgType.WildcardBound.EXTENDS), child)).isFalse();
		assertThat(LoopRegionVisitor.canCheckKnownNarrowingType(
				parent, ArgType.genericType("T", parent))).isFalse();
	}

	@Test
	void detectsCommonIterableInterfacesWithoutHierarchyLookup() {
		assertThat(LoopRegionVisitor.isKnownIterableInterface(
				ArgType.generic("java.lang.Iterable", ArgType.STRING))).isTrue();
		assertThat(LoopRegionVisitor.isKnownIterableInterface(
				ArgType.generic("java.util.List", ArgType.STRING))).isTrue();
		assertThat(LoopRegionVisitor.isKnownIterableInterface(
				ArgType.generic("java.util.Set", ArgType.STRING))).isTrue();
		assertThat(LoopRegionVisitor.isKnownIterableInterface(
				ArgType.object("java.util.Map"))).isFalse();
		assertThat(LoopRegionVisitor.isKnownIterableInterface(
				ArgType.object("test.Provider"))).isFalse();
	}

	@Test
	void resolvesLoopTypeFromMatchingConsumerSuperBound() {
		ArgType elementType = ArgType.genericType("E");
		assertThat(LoopRegionVisitor.resolveConsumerSuperLoopType(
				ArgType.wildcard(elementType, ArgType.WildcardBound.EXTENDS),
				elementType)).isEqualTo(elementType);

		ArgType iterableEntry = ArgType.generic("java.util.Map$Entry", ArgType.STRING, ArgType.INT);
		ArgType consumerEntry = ArgType.generic(
				"java.util.Map$Entry",
				ArgType.wildcard(ArgType.STRING, ArgType.WildcardBound.EXTENDS),
				ArgType.wildcard(ArgType.INT, ArgType.WildcardBound.EXTENDS));
		assertThat(LoopRegionVisitor.resolveConsumerSuperLoopType(iterableEntry, consumerEntry))
				.isEqualTo(iterableEntry);

		assertThat(LoopRegionVisitor.resolveConsumerSuperLoopType(
				ArgType.wildcard(elementType, ArgType.WildcardBound.SUPER),
				elementType)).isNull();
		assertThat(LoopRegionVisitor.resolveConsumerSuperLoopType(
				iterableEntry, ArgType.object("test.Other"))).isNull();
	}

	@Test
	void matchesOnlyExactSingleGenericUpperBound() {
		ArgType baseType = ArgType.object("test.Base");

		assertThat(LoopRegionVisitor.isExactSingleGenericUpperBound(
				baseType, ArgType.genericType("T", baseType))).isTrue();
		assertThat(LoopRegionVisitor.isExactSingleGenericUpperBound(
				baseType, ArgType.genericType("T"))).isFalse();
		assertThat(LoopRegionVisitor.isExactSingleGenericUpperBound(
				baseType,
				ArgType.genericType("T", List.of(baseType, ArgType.object("test.Marker"))))).isFalse();
		assertThat(LoopRegionVisitor.isExactSingleGenericUpperBound(
				baseType, ArgType.genericType("T", ArgType.object("test.Other")))).isFalse();
		assertThat(LoopRegionVisitor.isExactSingleGenericUpperBound(
				baseType, baseType)).isFalse();
	}
}
