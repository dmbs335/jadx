package jadx.plugins.kotlin.metadata.pass

import jadx.api.plugins.input.data.AccessFlags
import jadx.api.plugins.pass.JadxPassInfo
import jadx.api.plugins.pass.impl.OrderedJadxPassInfo
import jadx.api.plugins.pass.types.JadxDecompilePass
import jadx.core.deobf.NameMapper
import jadx.core.dex.attributes.AFlag
import jadx.core.dex.attributes.nodes.RenameReasonAttr
import jadx.core.dex.nodes.ClassNode
import jadx.core.dex.nodes.MethodNode
import jadx.core.dex.nodes.RootNode
import jadx.plugins.kotlin.metadata.KotlinMetadataOptions
import jadx.plugins.kotlin.metadata.model.CompanionRename
import jadx.plugins.kotlin.metadata.utils.KmClassWrapper
import jadx.plugins.kotlin.metadata.utils.KmClassWrapper.Companion.getWrapper
import jadx.plugins.kotlin.metadata.utils.KotlinMetadataPreparedAttr
import jadx.plugins.kotlin.metadata.utils.PackedMethodArgs

class KotlinMetadataDecompilePass(
	private val options: KotlinMetadataOptions,
) : JadxDecompilePass {

	override fun getInfo(): JadxPassInfo = OrderedJadxPassInfo(
		"KotlinMetadataDecompile",
		"Use kotlin.Metadata annotation perform various renames",
	)
		.before("CodeRenameVisitor")

	override fun init(root: RootNode) {
	}

	override fun visit(cls: ClassNode): Boolean {
		cls.innerClasses.forEach(::visit)

		val prepared = cls.get(KotlinMetadataPreparedAttr.TYPE)
		val wrapper = if (prepared == null) cls.getWrapper() else null
		if (prepared == null && wrapper == null) return false
		// Keep the compact prepare result across class unload/reload cycles. MethodNode and
		// FieldNode identities are stable, while method argument registers are read from the
		// current load when the packed names are applied.
		if (options.isMethodArgs) {
			if (prepared != null) {
				prepared.methodArgs?.let { renameMethodArgs(cls, it) }
			} else {
				renameMethodArgs(wrapper!!)
			}
		}
		if (options.isCompanion) {
			if (prepared != null) renameCompanion(prepared.companion) else renameCompanion(wrapper!!.getCompanion())
		}
		if (options.isDataClass) {
			if (prepared != null) {
				prepared.isDataClass?.let { fixDataClass(cls, it) }
			} else {
				fixDataClass(cls, wrapper!!.isDataClass())
			}
		}
		if (options.isToString) renameToString(cls)

		return false
	}

	private fun renameMethodArgs(wrapper: KmClassWrapper) {
		wrapper.getMethodArgs().forEach { (_, list) ->
			list.forEach { (rArg, alias) ->
				RenameReasonAttr.forNode(rArg).append(METADATA_REASON)
				rArg.name = alias
			}
		}
	}

	private fun renameMethodArgs(cls: ClassNode, methodArgs: PackedMethodArgs) {
		val methods = methodArgs.methods
		val names = methodArgs.names
		val offsets = methodArgs.offsets
		for (entry in methods.indices) {
			val start = offsets[entry]
			val end = offsets[entry + 1]
			val node = methods[entry]
			if (node.parentClass == cls && node.argTypes.size == end - start) {
				node.argRegs.forEachIndexed { index, rArg ->
					RenameReasonAttr.forNode(rArg).append(METADATA_REASON)
					rArg.name = names[start + index]
				}
			}
		}
	}

	private fun renameCompanion(companion: CompanionRename?) {
		companion?.run {
			if (AFlag.DONT_RENAME !in field) {
				RenameReasonAttr.forNode(field).append(METADATA_REASON)
				field.rename(COMPANION_FIELD)
			}
			if (AFlag.DONT_RENAME !in cls) {
				RenameReasonAttr.forNode(cls).append(METADATA_REASON)
				cls.rename(COMPANION_CLASS)
			}
			// Usage information is incomplete during the prepare pass, so a prepared
			// CompanionRename must not carry an early hide decision. Re-evaluate here,
			// at the same point where mapCompanion historically made this decision.
			val shouldHide = field.useIn.size == 1
				&& field.useIn[0].methodInfo.isClassInit
				&& cls.methods.all { it.isConstructor }
				&& cls.fields.isEmpty()
			if (shouldHide) {
				field.add(AFlag.DONT_GENERATE)
				cls.add(AFlag.DONT_GENERATE)
				cls.add(AFlag.DONT_INLINE)
			}
		}
	}

	private fun fixDataClass(cls: ClassNode, isData: Boolean) {
		if (isData != cls.accessFlags.isData) {
			cls.accessFlags = cls.accessFlags.run {
				if (isData) add(AccessFlags.DATA) else remove(AccessFlags.DATA)
			}
		}
	}

	override fun visit(mth: MethodNode?) {
		/* no op */
	}

	private fun renameToString(cls: ClassNode) {
		val toString = jadx.plugins.kotlin.metadata.utils.KotlinUtils.parseToString(cls)
		toString?.run {
			clsAlias?.let { alias ->
				if (NameMapper.isValidIdentifier(alias) && AFlag.DONT_RENAME !in cls) {
					RenameReasonAttr.forNode(cls).append(TO_STRING_REASON)
					cls.rename(alias)
				}
			}

			fields.forEach { (field, alias) ->
				if (NameMapper.isValidIdentifier(alias) && AFlag.DONT_RENAME !in field) {
					RenameReasonAttr.forNode(field).append(TO_STRING_REASON)
					field.rename(alias)
				}
			}
		}
	}

	companion object {
		private const val METADATA_REASON = "from kotlin metadata"
		private const val COMPANION_FIELD = "INSTANCE"
		private const val COMPANION_CLASS = "Companion"
		private const val TO_STRING_REASON = "from toString"
	}
}
