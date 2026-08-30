package jadx.plugins.kotlin.metadata.pass

import jadx.api.plugins.pass.JadxPassInfo
import jadx.api.plugins.pass.impl.OrderedJadxPassInfo
import jadx.api.plugins.pass.types.JadxPreparePass
import jadx.core.dex.attributes.AFlag
import jadx.core.dex.attributes.nodes.RenameReasonAttr
import jadx.core.dex.nodes.ClassNode
import jadx.core.dex.nodes.RootNode
import jadx.core.utils.Utils
import jadx.core.utils.exceptions.JadxRuntimeException
import jadx.plugins.kotlin.metadata.KotlinMetadataOptions
import jadx.plugins.kotlin.metadata.utils.KmClassWrapper
import jadx.plugins.kotlin.metadata.utils.KmClassWrapper.Companion.getWrapper
import jadx.plugins.kotlin.metadata.utils.KotlinMetadataPreparedAttr
import jadx.plugins.kotlin.metadata.utils.KotlinMetadataUtils
import jadx.plugins.kotlin.metadata.utils.getMetadata
import jadx.plugins.kotlin.metadata.utils.hasKotlinClassMetadataKind
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.metadata.jvm.Metadata

class KotlinMetadataPreparePass(
	private val options: KotlinMetadataOptions,
) : JadxPreparePass {

	override fun getInfo(): JadxPassInfo = OrderedJadxPassInfo(
		"KotlinMetadataPrepare",
		"Use kotlin.Metadata annotation to rename class & package",
	)
		.before("RenameVisitor")

	override fun init(root: RootNode) {
		val threads = minOf(root.args.threadsCount, MAX_DECODE_THREADS)
		if (threads == 1) {
			processSequential(root)
			return
		}
		processParallel(root, threads)
	}

	private fun processSequential(root: RootNode) {
		for (cls in root.classes) {
			try {
				processClass(cls)
			} catch (e: Exception) {
				root.errorsCounter.addAnalysisLoss("kotlin-metadata", cls.rawName, e)
			}
		}
	}

	private fun processParallel(root: RootNode, threads: Int) {
		val executor = Executors.newFixedThreadPool(threads, Utils.simpleThreadFactory("kotlin-metadata"))
		val batch = ArrayList<MetadataWork>(threads * BATCHES_PER_THREAD)
		try {
			for (cls in root.classes) {
				try {
					val metadata = cls.getMetadata() ?: continue
					processAlias(cls, metadata)
					if (cls.hasKotlinClassMetadataKind()) {
						batch.add(MetadataWork(cls, metadata))
						if (batch.size == threads * BATCHES_PER_THREAD) {
							processBatch(root, executor, batch)
						}
					}
				} catch (e: Exception) {
					root.errorsCounter.addAnalysisLoss("kotlin-metadata", cls.rawName, e)
				}
			}
			processBatch(root, executor, batch)
		} finally {
			executor.shutdownNow()
		}
	}

	private fun processBatch(root: RootNode, executor: ExecutorService, batch: MutableList<MetadataWork>) {
		if (batch.isEmpty()) return
		val futures = ArrayList<Future<KmClassWrapper?>>(batch.size)
		for (work in batch) {
			futures.add(executor.submit<KmClassWrapper?> { work.cls.getWrapper(work.metadata) })
		}
		val wrappers = arrayOfNulls<KmClassWrapper>(batch.size)
		for (i in futures.indices) {
			try {
				wrappers[i] = futures[i].get()
			} catch (e: InterruptedException) {
				futures.forEach { it.cancel(true) }
				Thread.currentThread().interrupt()
				throw JadxRuntimeException("Kotlin metadata decoding interrupted", e)
			} catch (e: ExecutionException) {
				val cls = batch[i].cls
				root.errorsCounter.addAnalysisLoss("kotlin-metadata", cls.rawName, e.cause ?: e)
			}
		}
		for (i in wrappers.indices) {
			val wrapper = wrappers[i] ?: continue
			try {
				applyWrapper(wrapper)
			} catch (e: Exception) {
				root.errorsCounter.addAnalysisLoss("kotlin-metadata", wrapper.cls.rawName, e)
			}
		}
		batch.clear()
	}

	private fun processClass(cls: ClassNode) {
		val metadata = cls.getMetadata() ?: return
		processAlias(cls, metadata)
		val wrapper = cls.getWrapper(metadata) ?: return
		applyWrapper(wrapper)
	}

	private fun processAlias(cls: ClassNode, metadata: Metadata) {
		if (options.isClassAlias && !cls.contains(AFlag.DONT_RENAME)) {
			val kotlinCls = KotlinMetadataUtils.getAlias(cls, metadata)
			if (kotlinCls != null) {
				cls.rename(kotlinCls.name)
				cls.packageNode.rename(kotlinCls.pkg)
			}
		}
	}

	private fun applyWrapper(wrapper: KmClassWrapper) {
		val cls = wrapper.cls
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
		private const val MAX_DECODE_THREADS = 4
		private const val BATCHES_PER_THREAD = 4
		private const val METADATA_REASON = "from kotlin metadata"
		private const val GETTER_REASON = "from getter"
	}

	private data class MetadataWork(
		val cls: ClassNode,
		val metadata: Metadata,
	)
}
