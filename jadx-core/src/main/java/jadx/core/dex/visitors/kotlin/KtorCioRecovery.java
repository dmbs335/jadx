package jadx.core.dex.visitors.kotlin;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import jadx.core.dex.info.FieldInfo;
import jadx.core.dex.instructions.InvokeNode;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.nodes.MethodNode;

/**
 * Central boundary for stable Ktor CIO ABI recognition used by CFG and region recovery.
 * Generated continuation identities are deliberately excluded; consumers still prove the exact
 * graph shape they intend to rewrite.
 */
public final class KtorCioRecovery {
	private static final Set<String> READ_FIELDS =
			Set.of("label", "$channel", "$nioChannel", "$selectable", "$selector");
	private static final String BYTE_CHANNEL = "io.ktor.utils.io.ByteChannel";
	private static final String BYTE_READ_CHANNEL = "io.ktor.utils.io.ByteReadChannel";
	private static final String BYTE_WRITE_CHANNEL = "io.ktor.utils.io.ByteWriteChannel";
	private static final String BYTE_READ_CHANNEL_OPERATIONS = "io.ktor.utils.io.ByteReadChannelOperationsKt";
	private static final String OBJECT_POOL = "io.ktor.utils.io.pool.ObjectPool";
	private static final String PROGRESS_LISTENER = "io.ktor.client.content.ProgressListener";
	private static final String HTTP_REQUEST_DATA = "io.ktor.client.request.HttpRequestData";
	private static final String TIMEOUT = "io.ktor.network.util.Timeout";
	private static final String SOURCE = "kotlinx.io.Source";
	private static final String BUFFER = "kotlinx.io.Buffer";
	private static final String BUFFER_JVM = "kotlinx.io.BuffersJvmKt";
	private static final String RAW_SINK = "kotlinx.io.RawSink";
	private static final String SOURCES = "kotlinx.io.SourcesKt";
	private static final String BYTE_READ_PACKET = "io.ktor.utils.io.core.ByteReadPacketKt";
	private static final String OUTPUT_STREAM = "java.io.OutputStream";
	private static final String BUFFERED_OUTPUT_STREAM = "java.io.BufferedOutputStream";
	private static final String FILE_CHANNEL = "java.nio.channels.FileChannel";

	private KtorCioRecovery() {
	}

	public static boolean isByteReadChannelOperationsClass(String name) {
		return name.startsWith("io.ktor.utils.io.ByteReadChannelOperations");
	}

	public static boolean isPooledObservableReadInvoke(InvokeNode invoke) {
		List<ArgType> args = invoke.getCallMth().getArgumentsTypes();
		return isByteReadChannelOperationsClass(invoke.getCallMth().getDeclClass().getFullName())
				&& ArgType.OBJECT.equals(invoke.getCallMth().getReturnType())
				&& args.size() == 4
				&& BYTE_READ_CHANNEL.equals(args.get(0).toString())
				&& "byte[]".equals(args.get(1).toString())
				&& ArgType.INT.equals(args.get(2))
				&& CoroutineMethodUtils.isKnownContinuationType(args.get(3));
	}

	public static boolean isPooledObservableWriteInvoke(InvokeNode invoke) {
		List<ArgType> args = invoke.getCallMth().getArgumentsTypes();
		return invoke.getCallMth().getDeclClass().getFullName()
				.startsWith("io.ktor.utils.io.ByteWriteChannelOperations")
				&& ArgType.OBJECT.equals(invoke.getCallMth().getReturnType())
				&& args.size() == 5
				&& BYTE_WRITE_CHANNEL.equals(args.get(0).toString())
				&& "byte[]".equals(args.get(1).toString())
				&& ArgType.INT.equals(args.get(2))
				&& ArgType.INT.equals(args.get(3))
				&& CoroutineMethodUtils.isKnownContinuationType(args.get(4));
	}

	public static boolean isPooledObservableProgressInvoke(InvokeNode invoke) {
		return PROGRESS_LISTENER.equals(invoke.getCallMth().getDeclClass().getFullName())
				&& ArgType.OBJECT.equals(invoke.getCallMth().getReturnType());
	}

	public static boolean isPooledObservableBorrowInvoke(InvokeNode invoke) {
		String owner = invoke.getCallMth().getDeclClass().getFullName();
		return ("io.ktor.utils.io.pool.DefaultPool".equals(owner) || OBJECT_POOL.equals(owner))
				&& invoke.getCallMth().getArgumentsTypes().isEmpty()
				&& invoke.getCallMth().getReturnType().isObject();
	}

	public static boolean isPooledObservableRecycleInvoke(InvokeNode invoke) {
		List<ArgType> args = invoke.getCallMth().getArgumentsTypes();
		return OBJECT_POOL.equals(invoke.getCallMth().getDeclClass().getFullName())
				&& args.size() == 1
				&& args.get(0).isObject()
				&& ArgType.VOID.equals(invoke.getCallMth().getReturnType());
	}

	public static boolean isByteWriteByteSuspendInvoke(InvokeNode invoke) {
		if (invoke == null
				|| !invoke.getCallMth().getDeclClass().getFullName()
						.startsWith("io.ktor.utils.io.ByteWriteChannelOperations")
				|| !ArgType.OBJECT.equals(invoke.getCallMth().getReturnType())) {
			return false;
		}
		List<ArgType> args = invoke.getCallMth().getArgumentsTypes();
		return args.size() == 3
				&& BYTE_WRITE_CHANNEL.equals(args.get(0).toString())
				&& ArgType.BYTE.equals(args.get(1))
				&& CoroutineMethodUtils.isKnownContinuationType(args.get(2));
	}

	public static boolean isReadStateMachine(MethodNode mth) {
		if (!CoroutineMethodUtils.isStateMachine(mth)) {
			return false;
		}
		int matched = 0;
		for (var field : mth.getParentClass().getFields()) {
			switch (field.getName()) {
				case "label":
					matched |= 1;
					break;
				case "$channel":
					matched |= 1 << 1;
					break;
				case "$nioChannel":
					matched |= 1 << 2;
					break;
				case "$selectable":
					matched |= 1 << 3;
					break;
				case "$selector":
					matched |= 1 << 4;
					break;
				default:
					break;
			}
		}
		return matched == (1 << READ_FIELDS.size()) - 1;
	}

	public static boolean isDirectReadStateMachine(MethodNode mth) {
		if (!CoroutineMethodUtils.isStateMachine(mth)) {
			return false;
		}
		String labelType = null;
		String channelType = null;
		String timeoutType = null;
		for (var field : mth.getParentClass().getFields()) {
			switch (field.getName()) {
				case "label":
					if (labelType == null) {
						labelType = field.getType().toString();
					}
					break;
				case "$channel":
					if (channelType == null) {
						channelType = field.getType().toString();
					}
					break;
				case "$timeout":
					if (timeoutType == null) {
						timeoutType = field.getType().toString();
					}
					break;
				default:
					break;
			}
		}
		return "int".equals(labelType)
				&& BYTE_CHANNEL.equals(channelType)
				&& TIMEOUT.equals(timeoutType);
	}

	public static boolean isReadPacketMethod(MethodNode mth) {
		List<ArgType> argTypes = mth.getMethodInfo().getArgumentsTypes();
		return argTypes.size() == 3
				&& isObjectType(argTypes.get(0), BYTE_READ_CHANNEL)
				&& argTypes.get(1).equals(ArgType.INT)
				&& CoroutineMethodUtils.isKnownContinuationType(argTypes.get(2))
				&& isByteReadChannelOperations(mth)
				&& containsInvoke(mth, KtorCioRecovery::isSourceRemainingInvoke)
				&& containsInvoke(mth, KtorCioRecovery::isSourceReadToBufferInvoke)
				&& containsInvoke(mth, KtorCioRecovery::isSourceTransferToBufferInvoke);
	}

	public static boolean isByteChannelCopyMethod(MethodNode mth) {
		List<ArgType> args = mth.getMethodInfo().getArgumentsTypes();
		boolean boundedCopy = args.size() == 4
				&& isObjectType(args.get(0), BYTE_READ_CHANNEL)
				&& isObjectType(args.get(1), BYTE_WRITE_CHANNEL)
				&& ArgType.LONG.equals(args.get(2))
				&& CoroutineMethodUtils.isKnownContinuationType(args.get(3));
		boolean unboundedCopy = args.size() == 3
				&& isObjectType(args.get(0), BYTE_READ_CHANNEL)
				&& isObjectType(args.get(1), BYTE_WRITE_CHANNEL)
				&& CoroutineMethodUtils.isKnownContinuationType(args.get(2));
		return isByteReadChannelOperations(mth)
				&& (boundedCopy || unboundedCopy)
				&& containsInvoke(mth, KtorCioRecovery::isAwaitContentInvoke)
				&& containsInvoke(mth, KtorCioRecovery::isByteWriteChannelSuspendInvoke)
				&& (containsInvoke(mth, KtorCioRecovery::isSourceReadToBufferInvoke)
						|| containsInvoke(mth, KtorCioRecovery::isSourceTransferToBufferInvoke));
	}

	/**
	 * Recognize Ktor's JVM {@code ByteReadChannel.copyTo(OutputStream)} lowering by its stable
	 * binary protocol. Release builds can rename both the top-level method and every Ktor accessor,
	 * so no source-level method name is part of this proof.
	 */
	public static boolean isByteChannelOutputStreamCopyMethod(MethodNode mth) {
		List<ArgType> args = mth.getMethodInfo().getArgumentsTypes();
		if (args.size() != 4
				|| !(isObjectType(args.get(0), BYTE_CHANNEL)
						|| isObjectType(args.get(0), BYTE_READ_CHANNEL))
				|| !(isObjectType(args.get(1), OUTPUT_STREAM)
						|| isObjectType(args.get(1), BUFFERED_OUTPUT_STREAM))
				|| !ArgType.LONG.equals(args.get(2))
				|| !CoroutineMethodUtils.isKnownContinuationType(args.get(3))) {
			return false;
		}
		return containsInvoke(mth, KtorCioRecovery::isAwaitContentInvoke)
				&& containsInvoke(mth, KtorCioRecovery::isClosedForReadInvoke)
				&& containsInvoke(mth, KtorCioRecovery::isReadBufferInvoke)
				&& (containsInvoke(mth, KtorCioRecovery::isBufferReadToOutputStreamInvoke)
						|| containsInvoke(mth, KtorCioRecovery::isOutputStreamWriteInvoke)
								&& containsInvoke(mth, KtorCioRecovery::isBufferSkipInvoke));
	}

	/**
	 * Recognize Ktor's JVM {@code ByteReadChannel.copyTo(FileChannel, limit)} lowering without
	 * depending on the facade method or generated continuation names.
	 */
	public static boolean isByteChannelFileChannelCopyMethod(MethodNode mth) {
		List<ArgType> args = mth.getMethodInfo().getArgumentsTypes();
		return mth.getParentClass().getRawName().startsWith("io.ktor.utils.io.ByteReadChannelOperations")
				&& args.size() == 4
				&& isObjectType(args.get(0), BYTE_READ_CHANNEL)
				&& isObjectType(args.get(1), FILE_CHANNEL)
				&& ArgType.LONG.equals(args.get(2))
				&& CoroutineMethodUtils.isKnownContinuationType(args.get(3))
				&& ArgType.OBJECT.equals(mth.getReturnType())
				&& containsInvoke(mth, KtorCioRecovery::isAwaitContentInvoke)
				&& containsInvoke(mth, KtorCioRecovery::isClosedForReadInvoke)
				&& containsInvoke(mth, KtorCioRecovery::isReadBufferInvoke);
	}

	public static boolean isTimeoutStopInvoke(InvokeNode invoke) {
		return invoke.getCallMth().getName().equals("stop")
				&& invoke.getCallMth().getDeclClass().getFullName().equals(TIMEOUT);
	}

	public static boolean isByteChannelClosedForWriteInvoke(InvokeNode invoke) {
		return invoke.getCallMth().getName().equals("isClosedForWrite")
				&& invoke.getCallMth().getDeclClass().getFullName().equals(BYTE_CHANNEL);
	}

	public static boolean isByteWriteChannelInvoke(InvokeNode invoke) {
		return invoke.getCallMth().getDeclClass().getFullName().equals(BYTE_WRITE_CHANNEL);
	}

	public static boolean isByteWriteChannelSuspendInvoke(InvokeNode invoke) {
		List<ArgType> args = invoke.getCallMth().getArgumentsTypes();
		return isByteWriteChannelInvoke(invoke)
				&& args.size() == 1
				&& CoroutineMethodUtils.isKnownContinuationType(args.get(0))
				&& ArgType.OBJECT.equals(invoke.getCallMth().getReturnType());
	}

	public static boolean isByteReadChannelOperations(MethodNode mth) {
		return mth.getParentClass().getRawName().equals(BYTE_READ_CHANNEL_OPERATIONS);
	}

	/**
	 * Recognize Ktor's nullable line-read suspend ABI. The public facade and JVM implementation
	 * class names vary across Ktor releases, while the receiver, limit, continuation, and result
	 * types remain stable.
	 */
	public static boolean isByteReadChannelLineInvoke(InvokeNode invoke) {
		List<ArgType> args = invoke.getCallMth().getArgumentsTypes();
		return invoke.getCallMth().getDeclClass().getFullName()
				.startsWith("io.ktor.utils.io.ByteReadChannelOperations")
				&& args.size() == 3
				&& isObjectType(args.get(0), BYTE_READ_CHANNEL)
				&& ArgType.INT.equals(args.get(1))
				&& CoroutineMethodUtils.isKnownContinuationType(args.get(2))
				&& ArgType.OBJECT.equals(invoke.getCallMth().getReturnType());
	}

	public static boolean isAwaitContentInvoke(InvokeNode invoke) {
		List<ArgType> args = invoke.getCallMth().getArgumentsTypes();
		if (!invoke.getCallMth().getDeclClass().getFullName().equals(BYTE_READ_CHANNEL)
				|| !ArgType.OBJECT.equals(invoke.getCallMth().getReturnType())) {
			return false;
		}
		if (args.size() == 2) {
			return ArgType.INT.equals(args.get(0))
					&& CoroutineMethodUtils.isKnownContinuationType(args.get(1));
		}
		return args.size() == 5
				&& isObjectType(args.get(0), BYTE_READ_CHANNEL)
				&& ArgType.INT.equals(args.get(1))
				&& CoroutineMethodUtils.isKnownContinuationType(args.get(2))
				&& ArgType.INT.equals(args.get(3))
				&& ArgType.OBJECT.equals(args.get(4));
	}

	public static boolean isClosedForReadInvoke(InvokeNode invoke) {
		return invoke.getCallMth().getDeclClass().getFullName().equals(BYTE_READ_CHANNEL)
				&& invoke.getCallMth().getArgumentsTypes().isEmpty()
				&& ArgType.BOOLEAN.equals(invoke.getCallMth().getReturnType());
	}

	public static boolean isReadBufferInvoke(InvokeNode invoke) {
		return invoke.getCallMth().getDeclClass().getFullName().equals(BYTE_READ_CHANNEL)
				&& invoke.getCallMth().getArgumentsTypes().isEmpty()
				&& isObjectType(invoke.getCallMth().getReturnType(), SOURCE);
	}

	public static boolean isSourceReadToArrayInvoke(InvokeNode invoke) {
		List<ArgType> args = invoke.getCallMth().getArgumentsTypes();
		return invoke.getCallMth().getDeclClass().getFullName().equals(SOURCES)
				&& args.size() == 4
				&& isObjectType(args.get(0), SOURCE)
				&& args.get(1).isArray()
				&& ArgType.BYTE.equals(args.get(1).getArrayElement())
				&& ArgType.INT.equals(args.get(2))
				&& ArgType.INT.equals(args.get(3))
				&& ArgType.VOID.equals(invoke.getCallMth().getReturnType());
	}

	public static boolean isSourceRemainingInvoke(InvokeNode invoke) {
		List<ArgType> args = invoke.getCallMth().getArgumentsTypes();
		return invoke.getCallMth().getDeclClass().getFullName().equals(BYTE_READ_PACKET)
				&& args.size() == 1
				&& isObjectType(args.get(0), SOURCE)
				&& ArgType.LONG.equals(invoke.getCallMth().getReturnType());
	}

	public static boolean isSourceDiscardInvoke(InvokeNode invoke) {
		List<ArgType> args = invoke.getCallMth().getArgumentsTypes();
		return invoke.getCallMth().getDeclClass().getFullName().equals(BYTE_READ_PACKET)
				&& args.size() == 2
				&& isObjectType(args.get(0), SOURCE)
				&& ArgType.LONG.equals(args.get(1))
				&& ArgType.LONG.equals(invoke.getCallMth().getReturnType());
	}

	public static boolean isSourceReadToBufferInvoke(InvokeNode invoke) {
		List<ArgType> args = invoke.getCallMth().getArgumentsTypes();
		return invoke.getCallMth().getDeclClass().getFullName().equals(SOURCE)
				&& args.size() == 2
				&& isObjectType(args.get(0), RAW_SINK)
				&& ArgType.LONG.equals(args.get(1))
				&& ArgType.VOID.equals(invoke.getCallMth().getReturnType());
	}

	public static boolean isSourceTransferToBufferInvoke(InvokeNode invoke) {
		List<ArgType> args = invoke.getCallMth().getArgumentsTypes();
		return invoke.getCallMth().getDeclClass().getFullName().equals(SOURCE)
				&& args.size() == 1
				&& isObjectType(args.get(0), RAW_SINK)
				&& ArgType.LONG.equals(invoke.getCallMth().getReturnType());
	}

	public static boolean isBufferSizeInvoke(InvokeNode invoke) {
		return invoke.getCallMth().getDeclClass().getFullName().equals(BUFFER)
				&& invoke.getCallMth().getArgumentsTypes().isEmpty()
				&& ArgType.LONG.equals(invoke.getCallMth().getReturnType());
	}

	public static boolean isBufferSizeField(FieldInfo field) {
		return field.getDeclClass().getFullName().equals(BUFFER)
				&& ArgType.LONG.equals(field.getType());
	}

	public static boolean isOutputStreamWriteInvoke(InvokeNode invoke) {
		List<ArgType> args = invoke.getCallMth().getArgumentsTypes();
		return invoke.getCallMth().getDeclClass().getFullName().equals(OUTPUT_STREAM)
				&& args.size() == 3
				&& args.get(0).isArray()
				&& ArgType.BYTE.equals(args.get(0).getArrayElement())
				&& ArgType.INT.equals(args.get(1))
				&& ArgType.INT.equals(args.get(2))
				&& ArgType.VOID.equals(invoke.getCallMth().getReturnType());
	}

	public static boolean isBufferSkipInvoke(InvokeNode invoke) {
		List<ArgType> args = invoke.getCallMth().getArgumentsTypes();
		return invoke.getCallMth().getDeclClass().getFullName().equals(BUFFER)
				&& args.size() == 1
				&& ArgType.LONG.equals(args.get(0))
				&& ArgType.VOID.equals(invoke.getCallMth().getReturnType());
	}

	public static boolean isBufferReadToOutputStreamInvoke(InvokeNode invoke) {
		List<ArgType> args = invoke.getCallMth().getArgumentsTypes();
		return invoke.getCallMth().getDeclClass().getFullName().equals(BUFFER_JVM)
				&& args.size() == 5
				&& isObjectType(args.get(0), BUFFER)
				&& isObjectType(args.get(1), OUTPUT_STREAM)
				&& ArgType.LONG.equals(args.get(2))
				&& ArgType.INT.equals(args.get(3))
				&& ArgType.OBJECT.equals(args.get(4))
				&& ArgType.VOID.equals(invoke.getCallMth().getReturnType());
	}

	public static boolean matchesPooledFanOutFields(Map<String, String> fieldTypes) {
		return BYTE_READ_CHANNEL.equals(fieldTypes.get("$this_split"))
				&& BYTE_CHANNEL.equals(fieldTypes.get("$first"))
				&& BYTE_CHANNEL.equals(fieldTypes.get("$second"));
	}

	public static boolean matchesPooledObservableFields(Map<String, String> fieldTypes) {
		return BYTE_READ_CHANNEL.equals(fieldTypes.get("$this_observable"))
				&& PROGRESS_LISTENER.equals(fieldTypes.get("$listener"))
				&& "java.lang.Long".equals(fieldTypes.get("$contentLength"));
	}

	public static boolean matchesOkHttpSourceFields(Map<String, String> fieldTypes) {
		return "okio.BufferedSource".equals(fieldTypes.get("$this_toChannel"))
				&& "kotlin.coroutines.CoroutineContext".equals(fieldTypes.get("$context"))
				&& HTTP_REQUEST_DATA.equals(fieldTypes.get("$requestData"));
	}

	public static boolean matchesReadTransformArgs(List<ArgType> argTypes) {
		return argTypes.size() == 5
				&& isObjectType(argTypes.get(0), BYTE_READ_CHANNEL)
				&& isObjectType(argTypes.get(1), BYTE_WRITE_CHANNEL)
				&& ArgType.BOOLEAN.equals(argTypes.get(2))
				&& isObjectType(argTypes.get(3), OBJECT_POOL)
				&& CoroutineMethodUtils.isContinuationType(argTypes.get(4));
	}

	public static boolean matchesReadShape(boolean stateMachine, Set<String> fieldNames) {
		return stateMachine && fieldNames.containsAll(READ_FIELDS);
	}

	public static boolean matchesDirectReadShape(boolean stateMachine, Map<String, String> fieldTypes) {
		return stateMachine
				&& "int".equals(fieldTypes.get("label"))
				&& BYTE_CHANNEL.equals(fieldTypes.get("$channel"))
				&& TIMEOUT.equals(fieldTypes.get("$timeout"));
	}

	private static boolean isObjectType(ArgType type, String objectName) {
		return type.isObject() && objectName.equals(type.getObject());
	}

	private static boolean containsInvoke(MethodNode mth, Predicate<InvokeNode> predicate) {
		for (BlockNode block : mth.getBasicBlocks()) {
			for (InsnNode insn : block.getInstructions()) {
				Boolean found = insn.visitInsns(inner -> inner instanceof InvokeNode
						&& predicate.test((InvokeNode) inner) ? Boolean.TRUE : null);
				if (found != null) {
					return true;
				}
			}
		}
		return false;
	}
}
