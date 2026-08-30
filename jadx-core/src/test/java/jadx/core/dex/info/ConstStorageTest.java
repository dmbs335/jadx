package jadx.core.dex.info;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

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
