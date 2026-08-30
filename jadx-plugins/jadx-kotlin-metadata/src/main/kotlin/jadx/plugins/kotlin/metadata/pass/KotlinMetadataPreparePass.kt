package jadx.plugins.kotlin.metadata.pass

import jadx.api.plugins.pass.JadxPassInfo
import jadx.api.plugins.pass.impl.OrderedJadxPassInfo
import jadx.api.plugins.pass.types.JadxPreparePass
import jadx.core.dex.attributes.AFlag
import jadx.core.dex.attributes.nodes.RenameReasonAttr
import jadx.core.dex.nodes.ClassNode
import jadx.core.dex.nodes.RootNode
import jadx.plugins.kotlin.metadata.KotlinMetadataOptions
import jadx.plugins.kotlin.metadata.utils.KmClassWrapper.Companion.getWrapper
import jadx.plugins.kotlin.metadata.utils.KotlinMetadataPreparedAttr
import jadx.plugins.kotlin.metadata.utils.KotlinMetadataUtils

class KotlinMetadataPreparePass(
	private val options: KotlinMetadataOptions,
) : JadxPreparePass {

	override fun getInfo(): JadxPassInfo = OrderedJadxPassInfo(
		"KotlinMetadataPrepare",
		"Use kotlin.Metadata annotation to rename class & package",
	)
		.before("RenameVisitor")

	override fun init(root: RootNode) {
		for (cls in root.classes) {
			try {
				processClass(cls)
			} catch (e: Exception) {
				root.errorsCounter.addAnalysisLoss("kotlin-metadata", cls.rawName, e)
			}
		}
	}

	private fun processClass(cls: ClassNode) {
		if (options.isClassAlias && !cls.contains(AFlag.DONT_RENAME)) {
			val kotlinCls = KotlinMetadataUtils.getAlias(cls)
			if (kotlinCls != null) {
				cls.rename(kotlinCls.name)
				cls.packageNode.rename(kotlinCls.pkg)
			}
		}
		val wrapper = cls.getWrapper() ?: return
		if (options.isFields) {
			wrapper.getFields().forEach { (field, alias) ->
				if (!field.contains(AFlag.DONT_RENAME)) {
					RenameReasonAttr.forNode(field).append(METADATA_REASON)
					field.rename(alias)
				}
			}
		}
		if (options.isGetters) {
			wrapper.getGetters().forEach { (method, alias) ->
				if (!method.contains(AFlag.DONT_RENAME)) {
					RenameReasonAttr.forNode(method).append(GETTER_REASON)
					method.rename(alias)
				}
			}
		}
		if (options.isDecompilePassNeeded()) {
			val methodArgs = if (options.isMethodArgs) wrapper.getPackedMethodArgs() else null
			val companion = if (options.isCompanion) wrapper.getCompanion() else null
			val isDataClass = if (options.isDataClass) wrapper.isDataClass() else null
			cls.addAttr(KotlinMetadataPreparedAttr(methodArgs, companion, isDataClass))
		}
	}

	companion object {
		private const val METADATA_REASON = "from kotlin metadata"
		private const val GETTER_REASON = "from getter"
	}
}
