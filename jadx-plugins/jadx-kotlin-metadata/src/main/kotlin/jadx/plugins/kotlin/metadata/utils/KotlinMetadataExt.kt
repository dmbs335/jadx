@file:Suppress("UNCHECKED_CAST")

package jadx.plugins.kotlin.metadata.utils

import jadx.api.plugins.input.data.annotations.EncodedType
import jadx.api.plugins.input.data.annotations.EncodedValue
import jadx.api.plugins.input.data.annotations.IAnnotation
import jadx.core.dex.nodes.ClassNode
import jadx.plugins.kotlin.metadata.model.KotlinMetadataConsts
import kotlin.metadata.jvm.KotlinClassMetadata
import kotlin.metadata.jvm.Metadata

fun ClassNode.getMetadata(): Metadata? {
	val annotation: IAnnotation? = getAnnotation(KotlinMetadataConsts.KOTLIN_METADATA_ANNOTATION)
	// Some protectors retain the annotation type but strip every optional element. There is no
	// metadata payload to decode in that case, and ordinary DEX code remains fully available.
	// Treating this marker-only annotation as a class analysis loss creates one false ERROR per
	// Kotlin class and can make otherwise complete application scans fail their coverage gate.
	if (annotation == null || annotation.values.isEmpty()) return null

	return annotation.run {
		val k = getParamAsInt(KotlinMetadataConsts.KOTLIN_METADATA_K_PARAMETER)
		val mvArray = getParamAsIntArray(KotlinMetadataConsts.KOTLIN_METADATA_MV_PARAMETER)
		val d1Array = getParamAsStringArray(KotlinMetadataConsts.KOTLIN_METADATA_D1_PARAMETER)
		val d2Array = getParamAsStringArray(KotlinMetadataConsts.KOTLIN_METADATA_D2_PARAMETER)
		val xs = getParamAsString(KotlinMetadataConsts.KOTLIN_METADATA_XS_PARAMETER)
		val pn = getParamAsString(KotlinMetadataConsts.KOTLIN_METADATA_PN_PARAMETER)
		val xi = getParamAsInt(KotlinMetadataConsts.KOTLIN_METADATA_XI_PARAMETER)

		Metadata(
			kind = k,
			metadataVersion = mvArray,
			data1 = d1Array,
			data2 = d2Array,
			extraString = xs,
			packageName = pn,
			extraInt = xi,
		)
	}
}

private fun IAnnotation.getParamsAsList(paramName: String): List<EncodedValue>? {
	val encodedValue = values[paramName]
		?.takeIf { it.type == EncodedType.ENCODED_ARRAY && it.value is List<*> }
	return encodedValue?.value?.let { it as List<EncodedValue> }
}

private fun IAnnotation.getParamAsStringArray(paramName: String): Array<String>? {
	return getParamsAsList(paramName)
		?.map<EncodedValue, Any?>(EncodedValue::getValue)
		?.onEach { if (it != null && it !is String) return@onEach }
		?.map { "$it" }
		?.toTypedArray()
}

private fun IAnnotation.getParamAsIntArray(paramName: String): IntArray? = getParamsAsList(paramName)
	?.map<EncodedValue, Any?>(EncodedValue::getValue)
	?.map { it as Int }
	?.toIntArray()

private fun IAnnotation.getParamAsInt(paramName: String): Int? {
	val encodedValue = values[paramName]
		?.takeIf { it.type == EncodedType.ENCODED_INT && it.value is Int }
	return encodedValue?.value?.let { it as Int }
}

private fun IAnnotation.getParamAsString(paramName: String): String? {
	val encodedValue = values[paramName]
		?.takeIf { it.type == EncodedType.ENCODED_STRING && it.value is String }
	return encodedValue?.value?.let { it as String }
}

/**
 * Check the cheap annotation header before asking kotlinx-metadata to decode the payload.
 * [KmClassWrapper] can only consume class metadata (kind 1); file facades, synthetic
 * classes and multifile entries are guaranteed to be rejected by its type cast.
 */
fun ClassNode.hasKotlinClassMetadataKind(): Boolean {
	val annotation = getAnnotation(KotlinMetadataConsts.KOTLIN_METADATA_ANNOTATION) ?: return false
	if (annotation.values.isEmpty()) return false
	// `k` defaults to 1 in kotlin.Metadata, so a missing encoded value still denotes a class.
	return (annotation.getParamAsInt(KotlinMetadataConsts.KOTLIN_METADATA_K_PARAMETER) ?: 1) == 1
}

fun ClassNode.getKotlinClassMetadata(): KotlinClassMetadata? {
	if (contains(MalformedKotlinMetadataAttr.TYPE)) return null
	val metadata = getMetadata() ?: return null
	if (metadata.metadataVersion.isEmpty()) {
		root().errorsCounter.addAnalysisExclusion(
			MALFORMED_METADATA_CATEGORY,
			"$rawName: missing metadataVersion",
			null,
		)
		addAttr(MalformedKotlinMetadataAttr)
		return null
	}
	return try {
		KotlinClassMetadata.readLenient(metadata)
	} catch (e: Exception) {
		// Kotlin metadata is an optional naming/type-hint layer. A protector can corrupt
		// d1/d2 while leaving the executable DEX method bodies intact. Isolate that class,
		// retain a visible audited exclusion and continue bytecode analysis without
		// reporting every such annotation as lost executable coverage.
		root().errorsCounter.addAnalysisExclusion(MALFORMED_METADATA_CATEGORY, rawName, e)
		addAttr(MalformedKotlinMetadataAttr)
		null
	}
}

private const val MALFORMED_METADATA_CATEGORY = "kotlin-metadata-malformed"
