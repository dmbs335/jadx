package jadx.plugins.kotlin.metadata.utils

import jadx.api.plugins.input.data.attributes.IJadxAttrType
import jadx.api.plugins.input.data.attributes.IJadxAttribute
import jadx.core.dex.nodes.MethodNode
import jadx.plugins.kotlin.metadata.model.CompanionRename

/** Carries the compact metadata needed after the prepare pass releases the decoded protobuf graph. */
class KotlinMetadataPreparedAttr(
	val methodArgs: PackedMethodArgs?,
	val companion: CompanionRename?,
	val isDataClass: Boolean?,
) : IJadxAttribute {
	override fun getAttrType(): IJadxAttrType<KotlinMetadataPreparedAttr> = TYPE

	override fun keepLoaded(): Boolean = true

	companion object {
		@JvmField
		val TYPE: IJadxAttrType<KotlinMetadataPreparedAttr> =
			IJadxAttrType.create("KOTLIN_METADATA_PREPARED")
	}
}

class PackedMethodArgs(
	val methods: Array<MethodNode>,
	val names: Array<String>,
	val offsets: IntArray,
)
