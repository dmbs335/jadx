package jadx.tests.integration.java8;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import jadx.tests.api.IntegrationTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestLambdaInArray extends IntegrationTest {

	public static class TestCls {
		@FunctionalInterface
		public interface Mapper<I, O> {
			O map(I input);
		}

		@FunctionalInterface
		public interface InheritedMapper<I, O> extends Mapper<I, O> {
		}

		@FunctionalInterface
		public interface SwappedMapper<A, B> extends Mapper<B, A> {
		}

		@FunctionalInterface
		public interface ChainedMapper<I, O> extends SwappedMapper<O, I> {
		}

		private String lastConsumed;

		public List<Function<String, Integer>> test() {
			return Arrays.asList(this::call1, this::call2);
		}

		public Object erasedResult() {
			return Arrays.asList((Function<String, Integer>) this::call1);
		}

		public Object erasedSupplier() {
			return Arrays.asList((Supplier<Integer>) this::constant);
		}

		public Object erasedConsumer() {
			return Arrays.asList((Consumer<String>) this::consume);
		}

		public Object erasedCustomMapper() {
			return Arrays.asList((Mapper<String, Integer>) this::call1);
		}

		public Object erasedInheritedMapper() {
			return Arrays.asList((InheritedMapper<String, Integer>) this::call1);
		}

		public Object erasedSwappedMapper() {
			return Arrays.asList((SwappedMapper<String, Integer>) this::numberString);
		}

		public Object erasedChainedMapper() {
			return Arrays.asList((ChainedMapper<String, Integer>) this::call1);
		}

		public Object erasedBoxingMethodReference() {
			return Arrays.asList((Function<Integer, Integer>) Integer::intValue);
		}

		public Object erasedUnboundComparator() {
			return Arrays.asList((Comparator<Integer>) Integer::compareTo);
		}

		private String numberString(Integer value) {
			return "n" + value;
		}

		private Integer constant() {
			return 1;
		}

		private void consume(String value) {
			lastConsumed = value;
		}

		private Integer call1(String s) {
			return s.length();
		}

		private Integer call2(String s) {
			return -s.length();
		}

		public void check() throws Exception {
			assertThat(test()).hasSize(2);
			assertThat(test().get(0).apply("abc")).isEqualTo(3);
			assertThat(test().get(1).apply("abc")).isEqualTo(-3);
			assertThat((List<?>) erasedResult()).hasSize(1);
			@SuppressWarnings("unchecked")
			Function<String, Integer> function = (Function<String, Integer>) ((List<?>) erasedResult()).get(0);
			assertThat(function.apply("abcd")).isEqualTo(4);
			assertThat((List<?>) erasedSupplier()).hasSize(1);
			assertThat(((Supplier<?>) ((List<?>) erasedSupplier()).get(0)).get()).isEqualTo(1);
			assertThat((List<?>) erasedConsumer()).hasSize(1);
			@SuppressWarnings("unchecked")
			Consumer<String> consumer = (Consumer<String>) ((List<?>) erasedConsumer()).get(0);
			consumer.accept("used");
			assertThat(lastConsumed).isEqualTo("used");
			@SuppressWarnings("unchecked")
			Mapper<String, Integer> mapper = (Mapper<String, Integer>) ((List<?>) erasedCustomMapper()).get(0);
			assertThat(mapper.map("abcde")).isEqualTo(5);
			@SuppressWarnings("unchecked")
			InheritedMapper<String, Integer> inheritedMapper =
					(InheritedMapper<String, Integer>) ((List<?>) erasedInheritedMapper()).get(0);
			assertThat(inheritedMapper.map("abcdef")).isEqualTo(6);
			@SuppressWarnings("unchecked")
			SwappedMapper<String, Integer> swappedMapper =
					(SwappedMapper<String, Integer>) ((List<?>) erasedSwappedMapper()).get(0);
			assertThat(swappedMapper.map(7)).isEqualTo("n7");
			@SuppressWarnings("unchecked")
			ChainedMapper<String, Integer> chainedMapper =
					(ChainedMapper<String, Integer>) ((List<?>) erasedChainedMapper()).get(0);
			assertThat(chainedMapper.map("abcdefg")).isEqualTo(7);
			@SuppressWarnings("unchecked")
			Function<Integer, Integer> boxingRef =
					(Function<Integer, Integer>) ((List<?>) erasedBoxingMethodReference()).get(0);
			assertThat(boxingRef.apply(9)).isEqualTo(9);
			@SuppressWarnings("unchecked")
			Comparator<Integer> comparator =
					(Comparator<Integer>) ((List<?>) erasedUnboundComparator()).get(0);
			assertThat(comparator.compare(2, 9)).isLessThan(0);
		}
	}

	@Test
	public void test() {
		assertThat(getClassNode(TestCls.class))
				.code()
				.contains("return Arrays.asList(")
				.contains("(Function<String, Integer>) this::call1")
				.contains("(Function<String, Integer>) this::call2")
				.contains("(Supplier<Integer>) this::constant")
				.contains("(Consumer<String>) this::consume")
				.contains("(Mapper<String, Integer>) this::call1")
				.contains("(InheritedMapper<String, Integer>) this::call1")
				.contains("(SwappedMapper<String, Integer>) this::numberString")
				.contains("(ChainedMapper<String, Integer>) this::call1")
				.contains("(Function<Integer, Integer>) (v0) ->")
				.contains("(Comparator<Integer>)");
	}
}
