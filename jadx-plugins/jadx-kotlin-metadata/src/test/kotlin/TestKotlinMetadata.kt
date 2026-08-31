package jadx.plugins.kotlin.metadata.tests

import jadx.plugins.kotlin.metadata.KotlinMetadataOptions.Companion.CLASS_ALIAS_OPT
import jadx.plugins.kotlin.metadata.KotlinMetadataOptions.Companion.COMPANION_OPT
import jadx.plugins.kotlin.metadata.KotlinMetadataOptions.Companion.DATA_CLASS_OPT
import jadx.plugins.kotlin.metadata.KotlinMetadataOptions.Companion.FIELDS_OPT
import jadx.plugins.kotlin.metadata.KotlinMetadataOptions.Companion.GETTERS_OPT
import jadx.plugins.kotlin.metadata.KotlinMetadataOptions.Companion.METHOD_ARGS_OPT
import jadx.plugins.kotlin.metadata.KotlinMetadataOptions.Companion.TO_STRING_OPT
import jadx.tests.api.SmaliTest
import jadx.tests.api.utils.assertj.JadxAssertions.assertThat
import jadx.tests.api.utils.assertj.JadxCodeAssertions
import org.junit.jupiter.api.Test
import java.io.File

class TestKotlinMetadata : SmaliTest() {
	// @formatter:off
	/*
		package deobf

		data class DataClassSample(
			val name: String,
			private val id: Int,
		) {
			var inner: Short = 3

			companion object {
				fun getTag(): String {
					return "TAG"
				}
			}
		}
	 */
	// @formatter:on

	@Test
	fun testMethodArgs() {
		setupArgs { this[METHOD_ARGS_OPT] = true }
		assertThatClass()
			.containsOne("public boolean equals(Object other) {")
	}

	@Test
	fun testIgnoreMethodArgs() {
		setupArgs()
		assertThatClass()
			.containsOne("public boolean equals(Object obj) {")
	}

	@Test
	fun testFields() {
		setupArgs { this[FIELDS_OPT] = true }
		assertThatClass()
			.containsOne("private final String name;")
			.containsOne("private final int id;")
			.containsOne("private short inner;")
			.countString(3, "reason: from kotlin metadata")
	}

	@Test
	fun testIgnoreFields() {
		setupArgs()
		assertThatClass()
			.containsOne("private final String a;")
			.containsOne("private final int b;")
			.containsOne("private short c;")
			.countString(0, "reason: from kotlin metadata")
	}

	@Test
	fun testCompanion() {
		setupArgs { this[COMPANION_OPT] = true }
		assertThatClass()
			.containsOne("public static final Companion INSTANCE = new Companion(null);")
			.containsOne("public static final class Companion {")
			.countString(2, "reason: from kotlin metadata")
	}

	@Test
	fun testIgnoreCompanion() {
		setupArgs()
		assertThatClass()
			.containsOne("public static final b d = new b(null);")
			.containsOne("public static final class b {")
			.countString(0, "reason: from kotlin metadata")
	}

	@Test
	fun testDataClass() {
		setupArgs { this[DATA_CLASS_OPT] = true }
		assertThatClass()
			.containsOne("/* data */")
	}

	@Test
	fun testIgnoreDataClass() {
		setupArgs()
		assertThatClass()
			.countString(0, "/* data */")
	}

	@Test
	fun testToString() {
		setupArgs { this[TO_STRING_OPT] = true }
		assertThatClass()
			.containsOne("public final class DataClassSample {")
			.containsOne("private final String name;")
			.containsOne("private final int id;")
			.countString(3, "reason: from toString")
	}

	@Test
	fun testToStringComputedPropertyIsNotMappedToBackingField() {
		setupArgs { this[TO_STRING_OPT] = true }
		assertThatClass()
			.containsOne("currentPage=")
			.countString(2, "getCurrentPage()")
			.doesNotContain("currentPage;")
			.countString(3, "reason: from toString")
	}

	@Test
	fun testIgnoreToString() {
		setupArgs()
		assertThatClass()
			.containsOne("public final class a {")
			.containsOne("private final String a;")
			.containsOne("private final int b;")
			.countString(0, "reason: from toString")
	}

	@Test
	fun testGetters() {
		setupArgs { this[GETTERS_OPT] = true }
		assertThatClass()
			.containsOne("public final String getA() {")
			.countString(1, "reason: from getter")
	}

	@Test
	fun testGettersAlias() {
		setupArgs {
			this[FIELDS_OPT] = true
			this[GETTERS_OPT] = true
		}
		assertThatClass()
			.containsOne("public final String getName() {")
			.countString(1, "reason: from getter")
	}

	@Test
	fun testGetterAliasAvailableBeforeOwnerDecompile() {
		disableCompilation()
		setupArgs {
			this[FIELDS_OPT] = true
			this[GETTERS_OPT] = true
		}
		assertThat(getClassNodeFromSmaliFiles("deobf", "TestKotlinMetadata", "Caller"))
			.code()
			.containsOne("return aVar.getName();")
	}

	@Test
	fun testMalformedMetadataIsIsolatedPerClass() {
		setupArgs { this[FIELDS_OPT] = true }
		val cls = getClassNodeFromFiles(
			listOf(
				File("src/test/smali/deobf/TestKotlinMetadataIsolation/MalformedMetadata.smali"),
				File("src/test/smali/deobf/TestKotlinMetadata/a.smali"),
				File("src/test/smali/deobf/TestKotlinMetadata/a\$b.smali"),
			),
			"deobf.a",
		)

		assertThat(cls)
			.code()
			.containsOne("private final String name;")
		org.assertj.core.api.Assertions.assertThat(jadxDecompiler.errorsCount).isZero()
		org.assertj.core.api.Assertions.assertThat(jadxDecompiler.globalErrors).isEmpty()
		org.assertj.core.api.Assertions.assertThat(jadxDecompiler.analysisLossCounts).isEmpty()
		org.assertj.core.api.Assertions.assertThat(jadxDecompiler.analysisExclusionCounts)
			.containsEntry("kotlin-metadata-malformed", 1)
		org.assertj.core.api.Assertions.assertThat(
			jadxDecompiler.analysisExclusionSamples["kotlin-metadata-malformed"],
		)
			.singleElement()
			.asString()
			.contains("deobf.MalformedMetadata")
	}

	@Test
	fun testMarkerOnlyMetadataDoesNotReportCodeAnalysisLoss() {
		val cls = getClassNodeFromFiles(
			listOf(File("src/test/smali/deobf/TestKotlinMetadataIsolation/EmptyMetadata.smali")),
			"deobf.EmptyMetadata",
		)

		assertThat(cls)
			.code()
			.containsOne("public final class EmptyMetadata")
		org.assertj.core.api.Assertions.assertThat(jadxDecompiler.errorsCount).isZero()
		org.assertj.core.api.Assertions.assertThat(jadxDecompiler.analysisLossCounts).isEmpty()
	}

	@Test
	fun testNonClassMetadataDoesNotEnterClassDecoder() {
		setupArgs { this[FIELDS_OPT] = true }
		val cls = getClassNodeFromFiles(
			listOf(File("src/test/smali/deobf/TestKotlinMetadataIsolation/NonClassMetadata.smali")),
			"deobf.NonClassMetadata",
		)

		assertThat(cls)
			.code()
			.containsOne("public final class NonClassMetadata")
		org.assertj.core.api.Assertions.assertThat(jadxDecompiler.errorsCount).isZero()
		org.assertj.core.api.Assertions.assertThat(jadxDecompiler.analysisLossCounts).isEmpty()
		org.assertj.core.api.Assertions.assertThat(jadxDecompiler.analysisExclusionCounts).isEmpty()
	}

	@Test
	fun testIgnoreGetters() {
		setupArgs()
		assertThatClass()
			.countString(0, "reason: from getter")
	}

	private fun setupArgs(builder: MutableMap<String, Boolean>.() -> Unit = {}) {
		val allOff = mutableMapOf(
			CLASS_ALIAS_OPT to false,
			METHOD_ARGS_OPT to false,
			FIELDS_OPT to false,
			COMPANION_OPT to false,
			DATA_CLASS_OPT to false,
			TO_STRING_OPT to false,
			GETTERS_OPT to false,
		)
		args.pluginOptions = allOff.apply(builder).mapValues {
			if (it.value) "yes" else "no"
		}
	}

	private fun assertThatClass(): JadxCodeAssertions = assertThat(getClassNodeFromSmaliFiles("deobf", "TestKotlinMetadata", "a"))
		.code()
}
