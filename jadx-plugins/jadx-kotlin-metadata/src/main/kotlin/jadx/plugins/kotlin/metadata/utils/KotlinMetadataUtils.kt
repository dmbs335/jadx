package jadx.plugins.kotlin.metadata.utils

import jadx.core.deobf.NameMapper
import jadx.core.dex.attributes.nodes.RenameReasonAttr
import jadx.core.dex.nodes.ClassNode
import jadx.core.dex.nodes.MethodNode
import jadx.core.utils.Utils
import jadx.plugins.kotlin.metadata.model.ClassAliasRename
import jadx.plugins.kotlin.metadata.model.CompanionRename
import jadx.plugins.kotlin.metadata.model.FieldRename
import jadx.plugins.kotlin.metadata.model.MethodArgRename
import jadx.plugins.kotlin.metadata.model.MethodRename
import kotlin.metadata.KmClass
import kotlin.metadata.KmFunction

object KotlinMetadataUtils {

	@JvmStatic
	fun getAlias(cls: ClassNode): ClassAliasRename? {
		val annotation = cls.getMetadata() ?: return null
		return getAlias(cls, annotation)
	}

	fun getAlias(cls: ClassNode, annotation: Metadata): ClassAliasRename? = getClassAlias(cls, annotation)

	/**
	 * Try to get class info from Kotlin Metadata annotation
	 */
	private fun getClassAlias(cls: ClassNode, annotation: Metadata): ClassAliasRename? {
		val firstValue = annotation.data2.getOrNull(0) ?: return null

		try {
			val clsName = firstValue.trim()
				.takeUnless(String::isEmpty)
				?.let(Utils::cleanObjectName)
				?: return null

			val alias = splitAndCheckClsName(cls, clsName)
			if (alias != null) {
				RenameReasonAttr.forNode(cls).append("from Kotlin metadata")
				return alias
			}
		} catch (e: Exception) {
			LOG.error("Failed to parse kotlin metadata", e)
		}
		return null
	}

	// Don't use ClassInfo facility to not pollute class into cache
	private fun splitAndCheckClsName(originCls: ClassNode, fullClsName: String): ClassAliasRename? {
		if (!NameMapper.isValidFullIdentifier(fullClsName)) {
			return null
		}
		val pkg: String
		val name: String
		val dot = fullClsName.lastIndexOf('.')
		if (dot == -1) {
			pkg = ""
			name = fullClsName
		} else {
			pkg = fullClsName.substring(0, dot)
			name = fullClsName.substring(dot + 1)
		}
		val originClsInfo = originCls.classInfo
		val originName = originClsInfo.shortName
		if (originName == name || name.contains("$") ||
			!NameMapper.isValidIdentifier(name) ||
			countPkgParts(originClsInfo.getPackage()) != countPkgParts(pkg) || pkg.startsWith("java.")
		) {
			return null
		}
		val newClsNode = originCls.root().resolveClass(fullClsName)
		return if (newClsNode != null) {
			// class with alias name already exist
			null
		} else {
			ClassAliasRename(pkg, name)
		}
	}

	private fun countPkgParts(pkg: String): Int {
		if (pkg.isEmpty()) {
			return 0
		}
		var count = 1
		var pos = 0
		while (true) {
			pos = pkg.indexOf('.', pos)
			if (pos == -1) {
				return count
			}
			pos++
			count++
		}
	}

	fun mapMethodArgs(cls: ClassNode, kmCls: KmClass): Map<MethodNode, List<MethodArgRename>> {
		return buildMap {
			kmCls.functions.forEach { kmFunction ->
				val node: MethodNode = cls.searchMethodByShortId(kmFunction.shortId) ?: return@forEach

				val argCount = node.argTypes.size
				val paramCount = kmFunction.valueParameters.size
				if (argCount == paramCount) {
					// requires arg registers to be loaded, is this necessary ?
					val aliasList = node.argRegs.zip(kmFunction.valueParameters).map { (rArg, kmValueParameter) ->
						MethodArgRename(rArg = rArg, alias = kmValueParameter.name)
					}
					put(node, aliasList)
				}
			}
		}
	}

	@Suppress("UNCHECKED_CAST")
	fun packMethodArgs(cls: ClassNode, kmCls: KmClass): PackedMethodArgs {
		// Preserve mapMethodArgs' last-value-wins behavior for duplicate metadata signatures.
		// Some libraries contain bridge/synthetic entries resolving to the same MethodNode.
		val matches = LinkedHashMap<MethodNode, KmFunction>()
		kmCls.functions.forEach { function ->
			val node = cls.searchMethodByShortId(function.shortId) ?: return@forEach
			if (node.argTypes.size == function.valueParameters.size) {
				matches[node] = function
			}
		}
		val methods = arrayOfNulls<MethodNode>(matches.size)
		val names = arrayOfNulls<String>(matches.values.sumOf { it.valueParameters.size })
		val offsets = IntArray(matches.size + 1)
		var entryIndex = 0
		var nameIndex = 0
		matches.forEach { (method, function) ->
			methods[entryIndex] = method
			offsets[entryIndex++] = nameIndex
			function.valueParameters.forEach { parameter ->
				names[nameIndex++] = parameter.name
			}
		}
		offsets[entryIndex] = nameIndex
		return PackedMethodArgs(methods as Array<MethodNode>, names as Array<String>, offsets)
	}

	fun mapFields(cls: ClassNode, kmCls: KmClass): List<FieldRename> {
		return kmCls.properties.mapNotNull { kmProperty ->
			val node = cls.searchFieldByShortId(kmProperty.shortId) ?: return@mapNotNull null
			FieldRename(field = node, alias = kmProperty.name)
		}
	}

	fun mapGetters(cls: ClassNode, kmCls: KmClass): List<MethodRename> {
		return kmCls.properties.mapNotNull { kmProperty ->
			val field = cls.searchFieldByShortId(kmProperty.shortId) ?: return@mapNotNull null
			val getter = cls.searchMethodByShortId(kmProperty.getterShortId) ?: return@mapNotNull null
			MethodRename(
				mth = getter,
				alias = KotlinUtils.getGetterAlias(field.alias),
			)
		}
	}

	fun mapCompanion(cls: ClassNode, kmCls: KmClass): CompanionRename? {
		val compName = kmCls.companionObject ?: return null
		val compField = cls.fields.firstOrNull {
			it.name == compName && it.accessFlags.run { isStatic && isFinal && isPublic }
		} ?: return null

		if (compField.type.isObject) {
			val compType = compField.type.`object`
			val compCls = cls.innerClasses.firstOrNull {
				it.classInfo.makeRawFullName() == compType
			} ?: return null

			val isOnlyInit = compField.useIn.size == 1 && compField.useIn[0].methodInfo.isClassInit
			val isEmpty = compCls.run { methods.all { it.isConstructor } && fields.isEmpty() }

			return CompanionRename(
				field = compField,
				cls = compCls,
				hide = isOnlyInit && isEmpty,
			)
		}

		return null
	}
}
