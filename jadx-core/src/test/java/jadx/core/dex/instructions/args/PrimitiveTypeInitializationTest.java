package jadx.core.dex.instructions.args;

import java.net.URL;
import java.net.URLClassLoader;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PrimitiveTypeInitializationTest {
	private static final String ARG_TYPE = ArgType.class.getName();
	private static final String PRIMITIVE_TYPE = PrimitiveType.class.getName();

	@Test
	void primitiveFirst() throws Exception {
		checkInitialization(PRIMITIVE_TYPE);
	}

	@Test
	void argTypeFirst() throws Exception {
		checkInitialization(ARG_TYPE);
	}

	private static void checkInitialization(String firstClass) throws Exception {
		URL location = ArgType.class.getProtectionDomain().getCodeSource().getLocation();
		try (URLClassLoader loader = new URLClassLoader(new URL[] { location }, ArgType.class.getClassLoader()) {
			@Override
			protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
				if (!isIsolatedType(name)) {
					return super.loadClass(name, resolve);
				}
				Class<?> cls = findLoadedClass(name);
				if (cls == null) {
					cls = findClass(name);
				}
				if (resolve) {
					resolveClass(cls);
				}
				return cls;
			}
		}) {
			Class.forName(firstClass, true, loader);
			Class<?> primitiveClass = Class.forName(PRIMITIVE_TYPE, true, loader);
			for (Object primitive : primitiveClass.getEnumConstants()) {
				Object boxed = primitiveClass.getMethod("getBoxType").invoke(primitive);
				assertThat(boxed).isNotNull();
				assertThat(primitiveClass.getMethod("getBoxType").invoke(primitive)).isSameAs(boxed);
			}
			Class<?> argTypeClass = Class.forName(ARG_TYPE, true, loader);
			Object object = primitiveClass.getField("OBJECT").get(null);
			Object array = primitiveClass.getField("ARRAY").get(null);
			Object integer = primitiveClass.getField("INT").get(null);
			assertThat(primitiveClass.getMethod("getBoxType").invoke(object))
					.isSameAs(argTypeClass.getField("OBJECT").get(null));
			assertThat(primitiveClass.getMethod("getBoxType").invoke(array))
					.isSameAs(argTypeClass.getField("OBJECT_ARRAY").get(null));
			assertThat(primitiveClass.getMethod("getBoxType").invoke(integer).toString()).isEqualTo("java.lang.Integer");
		}
	}

	private static boolean isIsolatedType(String name) {
		return name.equals(ARG_TYPE) || name.startsWith(ARG_TYPE + '$')
				|| name.equals(PRIMITIVE_TYPE) || name.startsWith(PRIMITIVE_TYPE + '$');
	}
}
