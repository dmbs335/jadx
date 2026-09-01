package jadx.core.dex.info;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import jadx.api.JadxArgs;
import jadx.core.dex.nodes.ClassNode;

import static org.assertj.core.api.Assertions.assertThat;

class ConstStorageTest {
	@Test
	void tracksOwnersOnlyForGlobalValues() throws Exception {
		ConstStorage storage = new ConstStorage(new JadxArgs());

		Object globalValues = getField(storage, "globalValues");
		Object classValues = getClassValues(storage);

		assertThat(getField(globalValues, "valuesByClass")).isNotNull();
		assertThat(getField(classValues, "valuesByClass")).isNull();
	}

	@Test
	@SuppressWarnings("unchecked")
	void resetClearsGeneratedValuesButKeepsResourceNames() throws Exception {
		ConstStorage storage = new ConstStorage(new JadxArgs());
		Object globalValues = getField(storage, "globalValues");
		((Map<Object, Object>) getField(globalValues, "values")).put(1, new Object());
		((Set<Object>) getField(globalValues, "duplicates")).add(2);
		((Map<Object, Object>) getField(globalValues, "valuesByClass")).put(new Object(), Set.of(1));
		getClassValues(storage);
		Map<Integer, String> resourceNames = Map.of(1, "id/value");
		storage.setResourcesNames(resourceNames);

		storage.reset();

		assertThat((Map<?, ?>) getField(globalValues, "values")).isEmpty();
		assertThat((Set<?>) getField(globalValues, "duplicates")).isEmpty();
		assertThat((Map<?, ?>) getField(globalValues, "valuesByClass")).isEmpty();
		assertThat((Map<?, ?>) getField(storage, "classes")).isEmpty();
		assertThat(storage.getResourcesNames()).isSameAs(resourceNames);
	}

	private static Object getClassValues(ConstStorage storage) throws Exception {
		Method method = ConstStorage.class.getDeclaredMethod("getClsValues", ClassNode.class);
		method.setAccessible(true);
		return method.invoke(storage, new Object[] { null });
	}

	private static Object getField(Object instance, String name) throws Exception {
		Field field = instance.getClass().getDeclaredField(name);
		field.setAccessible(true);
		return field.get(instance);
	}
}
