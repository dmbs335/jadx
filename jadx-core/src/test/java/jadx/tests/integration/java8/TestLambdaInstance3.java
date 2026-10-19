package jadx.tests.integration.java8;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.RaungTest;
import jadx.tests.api.compiler.TestCompiler;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

@SuppressWarnings("DataFlowIssue")
public class TestLambdaInstance3 extends RaungTest {

	public interface TestCls<R> extends Supplier<R> {
		default TestCls<R> test() {
			return (TestCls<R> & Memoized) Lazy.of(this)::get;
		}
	}

	public static final class Lazy<T> implements Supplier<T> {
		private final Supplier<? extends T> supplier;

		private Lazy(Supplier<? extends T> supplier) {
			this.supplier = supplier;
		}

		public static <T> Lazy<T> of(Supplier<? extends T> supplier) {
			return new Lazy<>(supplier);
		}

		@Override
		public T get() {
			return supplier.get();
		}
	}

	public interface Memoized {
	}

	public interface Tagged {
	}

	public interface ObjectFactory {
		Object get();
	}

	public interface StringFactory {
		String get();
	}

	public static class CovariantBridgeLambda {
		public ObjectFactory create() {
			return (ObjectFactory & StringFactory) () -> "value";
		}

		public void check() {
			ObjectFactory factory = create();
			if (!(factory instanceof StringFactory)) {
				throw new AssertionError("missing covariant lambda interface");
			}
			if (!"value".equals(factory.get()) || !"value".equals(((StringFactory) factory).get())) {
				throw new AssertionError("wrong covariant lambda dispatch");
			}
		}
	}

	public static class CovariantBridgeLambdaDirectCast {
		private boolean objectOnly;

		public ObjectFactory create() {
			if (objectOnly) {
				return () -> "value";
			}
			return (ObjectFactory & StringFactory) () -> "value";
		}

		public void check() {
			ObjectFactory factory = create();
			if (!"value".equals(factory.get()) || !"value".equals(((StringFactory) factory).get())) {
				throw new AssertionError("wrong direct covariant lambda dispatch");
			}
			objectOnly = true;
			factory = create();
			boolean rejected = false;
			try {
				((StringFactory) factory).get();
			} catch (ClassCastException expected) {
				rejected = true;
			}
			if (!rejected) {
				throw new AssertionError("missing StringFactory cast failure");
			}
		}
	}

	public static class CovariantBridgeLambdaFieldCast {
		private ObjectFactory factory = (ObjectFactory & StringFactory) () -> "value";

		public void check() {
			if (!"value".equals(((StringFactory) factory).get())) {
				throw new AssertionError("wrong field covariant dispatch");
			}
			factory = () -> "value";
			boolean rejected = false;
			try {
				((StringFactory) factory).get();
			} catch (ClassCastException expected) {
				rejected = true;
			}
			if (!rejected) {
				throw new AssertionError("missing field StringFactory cast failure");
			}
		}
	}

	public static class CovariantBridgeLambdaFieldLocalCast {
		private ObjectFactory factory = (ObjectFactory & StringFactory) () -> "value";

		public void check() {
			ObjectFactory local = factory;
			if (!"value".equals(local.get()) || !"value".equals(((StringFactory) local).get())) {
				throw new AssertionError("wrong local field covariant dispatch");
			}
			factory = () -> "value";
			local = factory;
			if (!"value".equals(local.get())) {
				throw new AssertionError("wrong local field value");
			}
			boolean rejected = false;
			try {
				((StringFactory) local).get();
			} catch (ClassCastException expected) {
				rejected = true;
			}
			if (!rejected) {
				throw new AssertionError("missing local field StringFactory cast failure");
			}
		}
	}

	public static class CovariantBridgeLambdaArrayCast {
		private final ObjectFactory[] factories = { (ObjectFactory & StringFactory) () -> "value" };

		public void check() {
			if (!"value".equals(((StringFactory) factories[0]).get())) {
				throw new AssertionError("wrong array covariant dispatch");
			}
			factories[0] = () -> "value";
			boolean rejected = false;
			try {
				((StringFactory) factories[0]).get();
			} catch (ClassCastException expected) {
				rejected = true;
			}
			if (!rejected) {
				throw new AssertionError("missing array StringFactory cast failure");
			}
		}
	}

	public static class CovariantBridgeLambdaArrayLocalCast {
		private final ObjectFactory[] factories = { (ObjectFactory & StringFactory) () -> "value" };

		public void check() {
			ObjectFactory local = factories[0];
			if (!"value".equals(local.get()) || !"value".equals(((StringFactory) local).get())) {
				throw new AssertionError("wrong local array covariant dispatch");
			}
			factories[0] = () -> "value";
			local = factories[0];
			if (!"value".equals(local.get())) {
				throw new AssertionError("wrong local array value");
			}
			boolean rejected = false;
			try {
				((StringFactory) local).get();
			} catch (ClassCastException expected) {
				rejected = true;
			}
			if (!rejected) {
				throw new AssertionError("missing local array StringFactory cast failure");
			}
		}
	}

	public static class CovariantBridgeLambdaArrayAliasCast {
		private final ObjectFactory[] factories = { (ObjectFactory & StringFactory) () -> "value" };

		public void check() {
			ObjectFactory[] alias = factories;
			if (alias.length != 1) {
				throw new AssertionError("wrong array alias length");
			}
			ObjectFactory local = alias[0];
			if (!"value".equals(local.get()) || !"value".equals(((StringFactory) local).get())) {
				throw new AssertionError("wrong aliased array covariant dispatch");
			}
			alias[0] = () -> "value";
			local = alias[0];
			if (!"value".equals(local.get())) {
				throw new AssertionError("wrong aliased array value");
			}
			boolean rejected = false;
			try {
				((StringFactory) local).get();
			} catch (ClassCastException expected) {
				rejected = true;
			}
			if (!rejected) {
				throw new AssertionError("missing aliased array StringFactory cast failure");
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked", "checkstyle:IllegalType" })
	public static class CovariantBridgeLambdaRawListCast {
		private final java.util.ArrayList values = new java.util.ArrayList();

		public CovariantBridgeLambdaRawListCast() {
			values.add((ObjectFactory & StringFactory) () -> "value");
		}

		public void check() {
			Object value = values.get(0);
			if (!"value".equals(((ObjectFactory) value).get())
					|| !"value".equals(((StringFactory) value).get())) {
				throw new AssertionError("wrong raw-list covariant dispatch");
			}
			values.set(0, (ObjectFactory) () -> "value");
			value = values.get(0);
			if (!"value".equals(((ObjectFactory) value).get())) {
				throw new AssertionError("wrong raw-list value");
			}
			boolean rejected = false;
			try {
				((StringFactory) value).get();
			} catch (ClassCastException expected) {
				rejected = true;
			}
			if (!rejected) {
				throw new AssertionError("missing raw-list StringFactory cast failure");
			}
		}
	}

	public static class LambdaObjectReturnCast {
		public Object create() {
			return (ObjectFactory) () -> "value";
		}

		public void check() {
			Object value = create();
			if (!"value".equals(((ObjectFactory) value).get())) {
				throw new AssertionError("wrong Object-return lambda");
			}
		}
	}

	public static class LambdaObjectFieldAndArrayCast {
		private Object value;
		private final Object[] values = new Object[1];

		public void check() {
			value = (ObjectFactory) () -> "field";
			values[0] = (ObjectFactory) () -> "array";
			if (!"field".equals(((ObjectFactory) value).get())
					|| !"array".equals(((ObjectFactory) values[0]).get())) {
				throw new AssertionError("wrong Object field or array lambda");
			}
		}
	}

	public static class LambdaObjectLocalCast {
		public void check() {
			Object value = (ObjectFactory) () -> "local";
			if (!"local".equals(((ObjectFactory) value).get())) {
				throw new AssertionError("wrong Object local lambda");
			}
		}
	}

	public static class LambdaObjectConditionalCast {
		public Object create(boolean enabled) {
			return enabled ? (ObjectFactory) () -> "conditional" : null;
		}

		public void check() {
			if (!"conditional".equals(((ObjectFactory) create(true)).get()) || create(false) != null) {
				throw new AssertionError("wrong Object conditional lambda");
			}
		}
	}

	public static class LambdaObjectArrayInitializerCast {
		public Object[] create() {
			return new Object[] { (ObjectFactory) () -> "array" };
		}

		public void check() {
			if (!"array".equals(((ObjectFactory) create()[0]).get())) {
				throw new AssertionError("wrong Object array initializer lambda");
			}
		}
	}

	public static class LambdaObjectVarArgCast {
		private Object first(Object... values) {
			return values[0];
		}

		public void check() {
			if (!"vararg".equals(((ObjectFactory) first((ObjectFactory) () -> "vararg")).get())) {
				throw new AssertionError("wrong Object vararg lambda");
			}
		}
	}

	public static class LambdaGenericArgumentCast {
		private <T> T identity(T value) {
			return value;
		}

		public void check() {
			Object value = identity((ObjectFactory) () -> "generic");
			if (!"generic".equals(((ObjectFactory) value).get())) {
				throw new AssertionError("wrong generic argument lambda");
			}
		}
	}

	public static class LambdaGenericCallSiteArgument {
		private <T> T identity(T value) {
			return value;
		}

		public void check() {
			Object value = this.<ObjectFactory>identity(() -> "generic-call-site");
			if (!"generic-call-site".equals(((ObjectFactory) value).get())) {
				throw new AssertionError("wrong generic call-site lambda");
			}
		}
	}

	public static class LambdaGenericVarArgCast {
		@SafeVarargs
		private final <T> T first(T... values) {
			return values[0];
		}

		public void check() {
			Object value = first((ObjectFactory) () -> "generic-vararg");
			if (!"generic-vararg".equals(((ObjectFactory) value).get())) {
				throw new AssertionError("wrong generic vararg lambda");
			}
		}
	}

	public static class LambdaExternalGenericVarArgCast {
		public void check() {
			Object values = Arrays.asList((ObjectFactory) () -> "external-generic-vararg");
			if (!"external-generic-vararg".equals(((ObjectFactory) ((List<?>) values).get(0)).get())) {
				throw new AssertionError("wrong external generic vararg lambda");
			}
		}
	}

	public static class LambdaGenericReturnCast {
		@SuppressWarnings("unchecked")
		public <T> T create() {
			return (T) (ObjectFactory) () -> "generic-return";
		}

		public void check() {
			if (!"generic-return".equals(this.<ObjectFactory>create().get())) {
				throw new AssertionError("wrong generic-return lambda");
			}
		}
	}

	public static class LambdaGenericFieldCast<T> {
		private T value;

		@SuppressWarnings("unchecked")
		public void set() {
			value = (T) (ObjectFactory) () -> "generic-field";
		}

		public void check() {
			set();
			if (!"generic-field".equals(((ObjectFactory) value).get())) {
				throw new AssertionError("wrong generic-field lambda");
			}
		}
	}

	public static class LambdaGenericFieldInitializer<T> {
		@SuppressWarnings("unchecked")
		private final T value = (T) (ObjectFactory) () -> "generic-initializer";

		public void check() {
			if (!"generic-initializer".equals(((ObjectFactory) value).get())) {
				throw new AssertionError("wrong generic field initializer");
			}
		}
	}

	public static class LambdaGenericArrayCast<T> {
		@SuppressWarnings("unchecked")
		private final T[] values = (T[]) new Object[1];

		@SuppressWarnings("unchecked")
		public void set() {
			values[0] = (T) (ObjectFactory) () -> "generic-array";
		}

		public void check() {
			set();
			if (!"generic-array".equals(((ObjectFactory) values[0]).get())) {
				throw new AssertionError("wrong generic-array lambda");
			}
		}
	}

	public static class LambdaGenericArrayInitializer<T> {
		@SuppressWarnings("unchecked")
		private final T[] values = (T[]) new Object[] { (ObjectFactory) () -> "generic-array-initializer" };

		public void check() {
			if (!"generic-array-initializer".equals(((ObjectFactory) values[0]).get())) {
				throw new AssertionError("wrong generic array initializer");
			}
		}
	}

	public static class LambdaMarkerCastAfterCreation {
		public Serializable create() {
			return (Serializable) (ObjectFactory) () -> "marker-cast";
		}

		public void check() {
			try {
				create();
				throw new AssertionError("missing marker cast failure");
			} catch (ClassCastException expected) {
				// The lambda was created as ObjectFactory, not Serializable.
			}
		}
	}

	public static class LambdaObjectConstructorCast {
		private final Object value;

		public LambdaObjectConstructorCast() {
			this(null);
		}

		public LambdaObjectConstructorCast(Object value) {
			this.value = value;
		}

		public void check() {
			LambdaObjectConstructorCast holder = new LambdaObjectConstructorCast((ObjectFactory) () -> "constructor");
			if (!"constructor".equals(((ObjectFactory) holder.value).get())) {
				throw new AssertionError("wrong constructor argument lambda");
			}
		}
	}

	public static class LambdaObjectFieldInitializerCast {
		private final Object value = (ObjectFactory) () -> "field-init";

		public void check() {
			if (!"field-init".equals(((ObjectFactory) value).get())) {
				throw new AssertionError("wrong Object field initializer lambda");
			}
		}
	}

	public static class LambdaInstanceOfCast {
		public void check() {
			if (!(((ObjectFactory) () -> "instance") instanceof ObjectFactory)) {
				throw new AssertionError("wrong lambda instanceof result");
			}
		}
	}

	public static class LambdaMonitorCast {
		private int count;

		public void check() {
			synchronized ((ObjectFactory) () -> "monitor") {
				count++;
			}
			if (count != 1) {
				throw new AssertionError("wrong lambda monitor body");
			}
		}
	}

	public static class LambdaStringConcatCast {
		public void check() {
			String value = "prefix:" + ((ObjectFactory) () -> "concat");
			if (!value.startsWith("prefix:")) {
				throw new AssertionError("wrong lambda string concatenation");
			}
		}
	}

	public static class LambdaNullComparisonCast {
		public void check() {
			if (((ObjectFactory) () -> "comparison") == null) {
				throw new AssertionError("wrong lambda null comparison");
			}
			if (null == ((ObjectFactory) () -> "comparison-reversed")) {
				throw new AssertionError("wrong reversed lambda null comparison");
			}
		}
	}

	public static class LambdaAssertMessageCast {
		private boolean condition = true;

		public void check() {
			assert condition : (ObjectFactory) () -> "assertion";
		}
	}

	public static class LambdaObjectConditionalBranchesCast {
		public Object create(boolean first) {
			return first ? (ObjectFactory) () -> "first" : (StringFactory) () -> "second";
		}

		public void check() {
			if (!"first".equals(((ObjectFactory) create(true)).get())
					|| !"second".equals(((StringFactory) create(false)).get())) {
				throw new AssertionError("wrong conditional lambda branches");
			}
		}
	}

	public static class LambdaDirectReceiverCast {
		public void check() {
			String description = ((ObjectFactory) () -> "receiver").toString();
			if (description == null) {
				throw new AssertionError("missing lambda receiver");
			}
		}
	}

	public static class CovariantBridgeLambdaArrayArgumentCast {
		private void verify(ObjectFactory[] factories) {
			if (!"value".equals(((StringFactory) factories[0]).get())) {
				throw new AssertionError("wrong argument-array covariant dispatch");
			}
			factories[0] = () -> "value";
			boolean rejected = false;
			try {
				((StringFactory) factories[0]).get();
			} catch (ClassCastException expected) {
				rejected = true;
			}
			if (!rejected) {
				throw new AssertionError("missing argument-array StringFactory cast failure");
			}
		}

		public void check() {
			verify(new ObjectFactory[] { (ObjectFactory & StringFactory) () -> "value" });
		}
	}

	public static class CovariantBridgeLambdaArrayReturnCast {
		private final ObjectFactory[] factories = { (ObjectFactory & StringFactory) () -> "value" };

		public ObjectFactory[] getFactories() {
			return factories;
		}

		public void check() {
			if (!"value".equals(((StringFactory) getFactories()[0]).get())) {
				throw new AssertionError("wrong returned-array covariant dispatch");
			}
			factories[0] = () -> "value";
			boolean rejected = false;
			try {
				((StringFactory) getFactories()[0]).get();
			} catch (ClassCastException expected) {
				rejected = true;
			}
			if (!rejected) {
				throw new AssertionError("missing returned-array StringFactory cast failure");
			}
		}
	}

	public static class SerializableLambda {
		public Runnable create() {
			return (Runnable & Serializable) () -> {
			};
		}

		public void check() throws Exception {
			Object lambda = create();
			if (!(lambda instanceof Serializable)) {
				throw new AssertionError("lambda is not serializable");
			}
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
				out.writeObject(lambda);
			}
			try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
				((Runnable) in.readObject()).run();
			}
		}
	}

	public static class SerializableMarkerLambda {
		public Runnable create() {
			return (Runnable & Serializable & Tagged) () -> {
			};
		}

		public void check() throws Exception {
			Object lambda = create();
			if (!(lambda instanceof Serializable) || !(lambda instanceof Tagged)) {
				throw new AssertionError("missing lambda marker interface");
			}
			((Runnable) lambda).run();
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
				out.writeObject(lambda);
			}
			try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
				Object restored = in.readObject();
				if (!(restored instanceof Serializable) || !(restored instanceof Tagged)) {
					throw new AssertionError("deserialized lambda lost marker interface");
				}
			}
		}
	}

	public static class CapturedMarkerLambda {
		private int calls;

		public Runnable create() {
			return (Runnable & Tagged) () -> calls++;
		}

		public void check() {
			Object lambda = create();
			if (!(lambda instanceof Tagged)) {
				throw new AssertionError("missing captured lambda marker");
			}
			((Runnable) lambda).run();
			if (calls != 1) {
				throw new AssertionError("captured receiver was not updated");
			}
		}
	}

	public static class CapturedSerializableMarkerLambda implements Serializable {
		private static final long serialVersionUID = 1L;
		private int calls;

		public Runnable create() {
			return (Runnable & Serializable & Tagged) () -> calls++;
		}

		public void check() throws Exception {
			Object lambda = create();
			if (!(lambda instanceof Serializable) || !(lambda instanceof Tagged)) {
				throw new AssertionError("missing captured serializable lambda marker");
			}
			((Runnable) lambda).run();
			if (calls != 1) {
				throw new AssertionError("captured serializable receiver was not updated");
			}
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
				out.writeObject(lambda);
			}
			try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
				Object restored = in.readObject();
				if (!(restored instanceof Serializable) || !(restored instanceof Tagged)) {
					throw new AssertionError("deserialized captured lambda lost marker interface");
				}
				((Runnable) restored).run();
			}
		}
	}

	@Test
	public void test() throws Exception {
		// some java versions failed to compile usage of interface with '$' in name
		addClsRename("jadx.tests.integration.java8.TestLambdaInstance3$TestCls", "java8.TestCls");
		ClassNode cls = getClassNode(TestCls.class);
		assertThat(cls)
				.code()
				.doesNotContain("this::get")
				.containsOne("TestLambdaInstance3.Memoized) lazyOf::get;");
		TestCls<String> original = () -> "value";
		org.assertj.core.api.Assertions.assertThat(original.test()).isInstanceOf(Memoized.class);
		try (TestCompiler compiler = new TestCompiler(getCompilerOptions())) {
			compiler.compileNodes(List.of(cls));
			Class<?> generated = compiler.getClass("java8.TestCls");
			Object instance = Proxy.newProxyInstance(generated.getClassLoader(), new Class<?>[] { generated },
					(proxy, method, args) -> method.getName().equals("get")
							? "value"
							: InvocationHandler.invokeDefault(proxy, method, args));
			Object value = generated.getMethod("test").invoke(instance);
			org.assertj.core.api.Assertions.assertThat(value).isInstanceOf(Memoized.class);
			org.assertj.core.api.Assertions.assertThat(generated.isInstance(value)).isTrue();
			org.assertj.core.api.Assertions.assertThat(((Supplier<?>) value).get()).isEqualTo("value");
		}
	}

	@Test
	public void testRaung() {
		disableCompilation();
		assertThat(getClassNodeFromRaung())
				.code()
				.doesNotContain("this::get")
				.containsOne(" lazyOf::get");
	}

	@Test
	public void testSerializableMarkerLambda() {
		assertThat(getClassNode(SerializableMarkerLambda.class))
				.code()
				.contains("Serializable")
				.contains("Tagged");
	}

	@Test
	public void testSerializableLambda() {
		assertThat(getClassNode(SerializableLambda.class))
				.code()
				.contains("Serializable");
	}

	@Test
	public void testCovariantBridgeLambda() {
		assertThat(getClassNode(CovariantBridgeLambda.class))
				.code()
				.contains("StringFactory");
	}

	@Test
	public void testCovariantBridgeLambdaDirectCast() {
		assertThat(getClassNode(CovariantBridgeLambdaDirectCast.class))
				.code()
				.contains("StringFactory");
	}

	@Test
	public void testCovariantBridgeLambdaFieldCast() {
		assertThat(getClassNode(CovariantBridgeLambdaFieldCast.class))
				.code()
				.contains("StringFactory");
	}

	@Test
	public void testCovariantBridgeLambdaFieldLocalCast() {
		assertThat(getClassNode(CovariantBridgeLambdaFieldLocalCast.class))
				.code()
				.contains("StringFactory");
	}

	@Test
	public void testCovariantBridgeLambdaArrayCast() {
		assertThat(getClassNode(CovariantBridgeLambdaArrayCast.class))
				.code()
				.contains("StringFactory");
	}

	@Test
	public void testCovariantBridgeLambdaArrayLocalCast() {
		assertThat(getClassNode(CovariantBridgeLambdaArrayLocalCast.class))
				.code()
				.contains("StringFactory");
	}

	@Test
	public void testCovariantBridgeLambdaArrayAliasCast() {
		assertThat(getClassNode(CovariantBridgeLambdaArrayAliasCast.class))
				.code()
				.contains("StringFactory");
	}

	@Test
	public void testCovariantBridgeLambdaRawListCast() {
		assertThat(getClassNode(CovariantBridgeLambdaRawListCast.class))
				.code()
				.contains("StringFactory");
	}

	@Test
	public void testLambdaObjectReturnCast() {
		assertThat(getClassNode(LambdaObjectReturnCast.class))
				.code()
				.contains("ObjectFactory");
	}

	@Test
	public void testLambdaObjectFieldAndArrayCast() {
		assertThat(getClassNode(LambdaObjectFieldAndArrayCast.class))
				.code()
				.contains("ObjectFactory");
	}

	@Test
	public void testLambdaObjectLocalCast() {
		assertThat(getClassNode(LambdaObjectLocalCast.class))
				.code()
				.contains("ObjectFactory");
	}

	@Test
	public void testLambdaObjectConditionalCast() {
		assertThat(getClassNode(LambdaObjectConditionalCast.class))
				.code()
				.contains("ObjectFactory");
	}

	@Test
	public void testLambdaObjectArrayInitializerCast() {
		assertThat(getClassNode(LambdaObjectArrayInitializerCast.class))
				.code()
				.contains("ObjectFactory");
	}

	@Test
	public void testLambdaObjectVarArgCast() {
		assertThat(getClassNode(LambdaObjectVarArgCast.class))
				.code()
				.contains("ObjectFactory");
	}

	@Test
	public void testLambdaGenericArgumentCast() {
		assertThat(getClassNode(LambdaGenericArgumentCast.class))
				.code()
				.contains("ObjectFactory");
	}

	@Test
	public void testLambdaGenericCallSiteArgument() {
		assertThat(getClassNode(LambdaGenericCallSiteArgument.class))
				.code()
				.contains("ObjectFactory");
	}

	@Test
	public void testLambdaGenericVarArgCast() {
		assertThat(getClassNode(LambdaGenericVarArgCast.class))
				.code()
				.contains("ObjectFactory");
	}

	@Test
	public void testLambdaExternalGenericVarArgCast() {
		assertThat(getClassNode(LambdaExternalGenericVarArgCast.class))
				.code()
				.contains("Arrays.asList");
	}

	@Test
	public void testLambdaObjectConstructorCast() {
		assertThat(getClassNode(LambdaObjectConstructorCast.class))
				.code()
				.contains("ObjectFactory");
	}

	@Test
	public void testLambdaObjectFieldInitializerCast() {
		assertThat(getClassNode(LambdaObjectFieldInitializerCast.class))
				.code()
				.contains("ObjectFactory");
	}

	@Test
	public void testLambdaInstanceOfCast() {
		assertThat(getClassNode(LambdaInstanceOfCast.class))
				.code()
				.contains("ObjectFactory");
	}

	@Test
	public void testLambdaMonitorCast() {
		assertThat(getClassNode(LambdaMonitorCast.class))
				.code()
				.contains("ObjectFactory");
	}

	@Test
	public void testLambdaStringConcatCast() {
		assertThat(getClassNode(LambdaStringConcatCast.class))
				.code()
				.contains("ObjectFactory");
	}

	@Test
	public void testLambdaNullComparisonCast() {
		assertThat(getClassNode(LambdaNullComparisonCast.class))
				.code()
				.contains("ObjectFactory");
	}

	@Test
	public void testLambdaAssertMessageCast() {
		assertThat(getClassNode(LambdaAssertMessageCast.class))
				.code()
				.contains("ObjectFactory");
	}

	@Test
	public void testLambdaObjectConditionalBranchesCast() {
		assertThat(getClassNode(LambdaObjectConditionalBranchesCast.class))
				.code()
				.contains("ObjectFactory");
	}

	@Test
	public void testLambdaDirectReceiverCast() {
		assertThat(getClassNode(LambdaDirectReceiverCast.class))
				.code()
				.contains("ObjectFactory");
	}

	@Test
	public void testLambdaGenericReturnCast() {
		assertThat(getClassNode(LambdaGenericReturnCast.class))
				.code()
				.contains("ObjectFactory");
	}

	@Test
	public void testLambdaGenericFieldCast() {
		assertThat(getClassNode(LambdaGenericFieldCast.class))
				.code()
				.contains("ObjectFactory");
	}

	@Test
	public void testLambdaGenericFieldInitializer() {
		assertThat(getClassNode(LambdaGenericFieldInitializer.class))
				.code()
				.contains("ObjectFactory");
	}

	@Test
	public void testLambdaGenericArrayCast() {
		assertThat(getClassNode(LambdaGenericArrayCast.class))
				.code()
				.contains("ObjectFactory");
	}

	@Test
	public void testLambdaGenericArrayInitializer() {
		assertThat(getClassNode(LambdaGenericArrayInitializer.class))
				.code()
				.contains("ObjectFactory");
	}

	@Test
	public void testLambdaMarkerCastAfterCreation() {
		assertThat(getClassNode(LambdaMarkerCastAfterCreation.class))
				.code()
				.contains("ObjectFactory");
	}

	@Test
	public void testCovariantBridgeLambdaArrayArgumentCast() {
		assertThat(getClassNode(CovariantBridgeLambdaArrayArgumentCast.class))
				.code()
				.contains("StringFactory");
	}

	@Test
	public void testCovariantBridgeLambdaArrayReturnCast() {
		assertThat(getClassNode(CovariantBridgeLambdaArrayReturnCast.class))
				.code()
				.contains("StringFactory");
	}

	@Test
	public void testCapturedMarkerLambda() {
		assertThat(getClassNode(CapturedMarkerLambda.class))
				.code()
				.contains("Tagged");
	}

	@Test
	public void testCapturedSerializableMarkerLambda() {
		assertThat(getClassNode(CapturedSerializableMarkerLambda.class))
				.code()
				.contains("Serializable")
				.contains("Tagged");
	}
}
