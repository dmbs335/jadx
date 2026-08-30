package jadx.plugins.kotlin.metadata.utils

import jadx.api.plugins.input.data.attributes.IJadxAttrType
import jadx.api.plugins.input.data.attributes.IJadxAttribute

object MalformedKotlinMetadataAttr : IJadxAttribute {
	@JvmField
	val TYPE: IJadxAttrType<MalformedKotlinMetadataAttr> =
		IJadxAttrType.create("MALFORMED_KOTLIN_METADATA")

	override fun getAttrType(): IJadxAttrType<MalformedKotlinMetadataAttr> = TYPE

	override fun keepLoaded(): Boolean = true
}
