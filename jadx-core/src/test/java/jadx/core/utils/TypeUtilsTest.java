package jadx.core.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.api.JadxArgs;
import jadx.core.dex.attributes.nodes.ClassTypeVarsAttr;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.nodes.RootNode;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

class TypeUtilsTest {
	private static final Logger LOG = LoggerFactory.getLogger(TypeUtilsTest.class);

	private static RootNode root;

	@BeforeAll
	public static void init() {
		root = new RootNode(new JadxArgs());
		root.initClassPath();
	}

	@Test
	public void testReplaceGenericsWithWildcards() {
		// check classpath graph
		List<ArgType> classGenerics = root.getTypeUtils().getClassGenerics(ArgType.object("java.util.ArrayList"));
		assertThat(classGenerics).hasSize(1);
		ArgType genericInfo = classGenerics.get(0);
		assertThat(genericInfo.getObject()).isEqualTo("E");
		assertThat(genericInfo.getExtendTypes()).hasSize(0);

		// prepare input
		ArgType instanceType = ArgType.generic("java.util.ArrayList", ArgType.OBJECT);
		LOG.debug("instanceType: {}", instanceType);

		ArgType generic = ArgType.generic("java.util.List", ArgType.wildcard(ArgType.genericType("E"), ArgType.WildcardBound.SUPER));
		LOG.debug("generic: {}", generic);

		// replace
		ArgType result = root.getTypeUtils().replaceClassGenerics(instanceType, generic);
		LOG.debug("result: {}", result);

		ArgType expected = ArgType.generic("java.util.List", ArgType.wildcard(ArgType.OBJECT, ArgType.WildcardBound.SUPER));
		assertThat(result).isEqualTo(expected);
	}

	@Test
	public void testReuseUnboundedTypeVarInMapping() {
		List<ArgType> classGenerics = root.getTypeUtils().getClassGenerics(ArgType.object("java.util.ArrayList"));
		ArgType typeVar = classGenerics.get(0);
		ArgType instanceType = ArgType.generic("java.util.ArrayList", ArgType.STRING);

		Map<ArgType, ArgType> mapping = root.getTypeUtils().getTypeVariablesMapping(instanceType);

		assertThat(mapping).containsEntry(typeVar, ArgType.STRING);
		assertThat(mapping.keySet().iterator().next()).isSameAs(typeVar);
	}

	@Test
	public void testSmallTypeVariablesMappings() {
		assertTypeVariablesMapping("java.util.HashMap", ArgType.STRING, ArgType.OBJECT);
		assertTypeVariablesMapping("java.util.stream.Collector", ArgType.STRING, ArgType.OBJECT, ArgType.CLASS);
	}

	@Test
	public void testSmallTypeVariablesMappingWithNullFallsBack() {
		String clsName = "java.util.HashMap";
		List<ArgType> typeVars = root.getTypeUtils().getClassGenerics(ArgType.object(clsName));
		ArgType instanceType = ArgType.generic(clsName, Arrays.asList(ArgType.STRING, null));

		Map<ArgType, ArgType> mapping = root.getTypeUtils().getTypeVariablesMapping(instanceType);

		assertThat(mapping).containsEntry(typeVars.get(0), ArgType.STRING);
		assertThat(mapping).containsEntry(typeVars.get(1), null);
	}

	private static void assertTypeVariablesMapping(String clsName, ArgType... actualTypes) {
		List<ArgType> typeVars = root.getTypeUtils().getClassGenerics(ArgType.object(clsName));
		assertThat(typeVars).hasSameSizeAs(actualTypes);
		ArgType instanceType = ArgType.generic(clsName, actualTypes);

		Map<ArgType, ArgType> mapping = root.getTypeUtils().getTypeVariablesMapping(instanceType);

		assertThat(mapping).hasSize(actualTypes.length);
		for (int i = 0; i < actualTypes.length; i++) {
			assertThat(mapping).containsEntry(typeVars.get(i), actualTypes[i]);
		}
	}

	@Test
	public void testGetTypeVarsMapForNonObjectType() {
		assertThat(ClassTypeVarsAttr.EMPTY.getTypeVarsMapFor(ArgType.INT)).isEmpty();
		assertThat(ClassTypeVarsAttr.EMPTY.getTypeVarsMapFor(ArgType.UNKNOWN)).isEmpty();
	}
}
