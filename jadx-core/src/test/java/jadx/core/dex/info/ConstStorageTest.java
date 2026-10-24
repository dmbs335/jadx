package jadx.core.dex.info;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import jadx.api.JadxArgs;
import jadx.core.dex.nodes.ClassNode;
import jadx.core.dex.nodes.IFieldInfoRef;

import static org.assertj.core.api.Assertions.assertThat;

class ConstStorageTest {
	@Test
	void distinguishesNaNConstantFieldsByRawBits() throws Exception {
		ConstStorage storage = new ConstStorage(new JadxArgs());
		IFieldInfoRef floatField1 = () -> null;
		IFieldInfoRef floatField2 = () -> null;
		IFieldInfoRef doubleField1 = () -> null;
		IFieldInfoRef doubleField2 = () -> null;
		float floatNaN1 = Float.intBitsToFloat(0x7fc00001);
		float floatNaN2 = Float.intBitsToFloat(0x7fc00002);
		double doubleNaN1 = Double.longBitsToDouble(0x7ff8000000000001L);
		double doubleNaN2 = Double.longBitsToDouble(0x7ff8000000000002L);

		storage.addGlobalConstField(floatField1, floatNaN1);
		storage.addGlobalConstField(floatField2, floatNaN2);
		storage.addGlobalConstField(doubleField1, doubleNaN1);
		storage.addGlobalConstField(doubleField2, doubleNaN2);

		Object globalValues = getField(storage, "globalValues");
		assertThat(storage.getGlobalConstFields()).hasSize(4);
		assertThat(getValue(globalValues, floatNaN1)).isSameAs(floatField1);
		assertThat(getValue(globalValues, floatNaN2)).isSameAs(floatField2);
		assertThat(getValue(globalValues, doubleNaN1)).isSameAs(doubleField1);
		assertThat(getValue(globalValues, doubleNaN2)).isSameAs(doubleField2);

		storage.addGlobalConstField(() -> null, floatNaN1);
		assertThat(getValue(globalValues, floatNaN1)).isNull();
		assertThat(getValue(globalValues, floatNaN2)).isSameAs(floatField2);
	}

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

	private static Object getValue(Object storage, Object value) throws Exception {
		Method method = storage.getClass().getDeclaredMethod("get", Object.class);
		method.setAccessible(true);
		return method.invoke(storage, value);
	}
}
