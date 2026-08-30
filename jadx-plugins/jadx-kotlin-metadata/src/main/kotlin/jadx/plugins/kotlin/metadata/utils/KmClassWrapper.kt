package jadx.plugins.kotlin.metadata.utils

import jadx.core.dex.nodes.ClassNode
import kotlin.metadata.KmClass
import kotlin.metadata.isData
import kotlin.metadata.jvm.KotlinClassMetadata

// don't expose kotlinx.metadata.* types ?
class KmClassWrapper private constructor(
	val cls: ClassNode,
	private val kmCls: KmClass,
) {

	fun getMethodArgs() = KotlinMetadataUtils.mapMethodArgs(cls, kmCls)

	fun getPackedMethodArgs() = KotlinMetadataUtils.packMethodArgs(cls, kmCls)

	fun getFields() = KotlinMetadataUtils.mapFields(cls, kmCls)

	fun getCompanion() = KotlinMetadataUtils.mapCompanion(cls, kmCls)

	fun isDataClass() = kmCls.isData

	// does not require metadata, may be useful for plain java ?
	fun parseToString() = KotlinUtils.parseToString(cls)

	fun getGetters() = KotlinMetadataUtils.mapGetters(cls, kmCls)

	companion object {

		fun ClassNode.getWrapper(): KmClassWrapper? {
			if (!hasKotlinClassMetadataKind()) return null
			val metadata = getKotlinClassMetadata()
			val kmCls = (metadata as? KotlinClassMetadata.Class)?.kmClass ?: return null
			return KmClassWrapper(this, kmCls)
		}
	}
}
