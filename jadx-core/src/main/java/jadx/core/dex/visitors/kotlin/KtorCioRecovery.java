package jadx.core.dex.visitors.kotlin;

import java.util.List;
import java.util.Map;
import java.util.Set;

import jadx.core.dex.instructions.InvokeNode;
import jadx.core.dex.instructions.args.ArgType;
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

	private KtorCioRecovery() {
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
		return mth.getName().equals("readPacket")
				&& argTypes.size() == 3
				&& argTypes.get(1).equals(ArgType.INT)
				&& isByteReadChannelOperations(mth);
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

	public static boolean isByteReadChannelOperations(MethodNode mth) {
		return mth.getParentClass().getRawName().equals(BYTE_READ_CHANNEL_OPERATIONS);
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
}
