package jadx.gui.cache.usage;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.input.UnsynchronizedBufferedInputStream;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.api.JadxDecompiler;
import jadx.api.plugins.input.data.IMethodRef;
import jadx.api.usage.IUsageInfoData;
import jadx.core.dex.info.MethodInfo;
import jadx.core.dex.nodes.ClassNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.nodes.RootNode;
import jadx.core.utils.Utils;
import jadx.core.utils.exceptions.JadxRuntimeException;
import jadx.core.utils.files.FileUtils;
import jadx.gui.cache.code.disk.adapters.DataAdapterHelper;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

public class UsageFileAdapter extends DataAdapterHelper {
	private static final Logger LOG = LoggerFactory.getLogger(UsageFileAdapter.class);

	private static final int USAGE_DATA_VERSION = 5;
	private static final byte[] JADX_USAGE_HEADER = "jadx.usage".getBytes(StandardCharsets.US_ASCII);

	public static synchronized @Nullable RawUsageData load(RootNode root, Path usageFile, List<File> inputs) {
		if (!Files.isRegularFile(usageFile)) {
			return null;
		}
		long start = System.currentTimeMillis();
		try (InputStream fileInput = Files.newInputStream(usageFile);
				DataInputStream in = new DataInputStream(
						new UnsynchronizedBufferedInputStream.Builder().setInputStream(fileInput).get())) {
			in.skipBytes(JADX_USAGE_HEADER.length);
			int dataVersion = in.readInt();
			if (dataVersion != USAGE_DATA_VERSION) {
				LOG.debug("Found old usage data format");
				FileUtils.deleteFileIfExists(usageFile);
				return null;
			}
			String inputsHash = buildInputsHash(root, inputs);
			String fileInputsHash = in.readUTF();
			if (!inputsHash.equals(fileInputsHash)) {
				LOG.debug("Found usage data with different inputs hash");
				FileUtils.deleteFileIfExists(usageFile);
				return null;
			}
			RawUsageData data = readData(root, in);
			if (LOG.isDebugEnabled()) {
				LOG.debug("Loaded usage data from disk cache, classes count: {}, time: {}ms, file: {}",
						data.getClsMap().size(), System.currentTimeMillis() - start, usageFile);
			}
			return data;
		} catch (Exception e) {
			try {
				FileUtils.deleteFileIfExists(usageFile);
			} catch (IOException ex) {
				// ignore
			}
			LOG.error("Failed to load usage data file", e);
			return null;
		}
	}

	public static synchronized void save(RootNode root, IUsageInfoData data, Path usageFile, List<File> inputs) {
		long start = System.currentTimeMillis();
		FileUtils.makeDirsForFile(usageFile);
		String inputsHash = buildInputsHash(root, inputs);
		RawUsageData usageData = new RawUsageData();
		data.visitUsageData(new CollectUsageData(usageData));
		Path temporary = null;
		try {
			temporary = Files.createTempFile(usageFile.getParent(), usageFile.getFileName().toString(), ".tmp");
			try (OutputStream fileOutput = Files.newOutputStream(temporary, WRITE, CREATE, TRUNCATE_EXISTING);
					DataOutputStream out = new DataOutputStream(new BufferedOutputStream(fileOutput))) {
				out.write(JADX_USAGE_HEADER);
				out.writeInt(USAGE_DATA_VERSION);
				out.writeUTF(inputsHash);
				writeData(root, out, usageData);
			}
			moveReplace(temporary, usageFile);
			temporary = null;
		} catch (Exception e) {
			LOG.error("Failed to save usage data file", e);
		} finally {
			try {
				if (temporary != null) {
					Files.deleteIfExists(temporary);
				}
			} catch (IOException e) {
				LOG.debug("Failed to delete temporary usage data file: {}", temporary, e);
			}
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("Usage data saved, time: {}ms, file: {}", System.currentTimeMillis() - start, usageFile);
		}
	}

	private static void moveReplace(Path source, Path target) throws IOException {
		try {
			Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
		} catch (AtomicMoveNotSupportedException e) {
			Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
		}
	}

	private static RawUsageData readData(RootNode root, DataInputStream in) throws IOException {
		int clsCount = readUVInt(in);
		int clsWithoutDataCount = readUVInt(in);
		RawUsageData data = new RawUsageData(clsCount);

		// Class information
		String[] clsNames = new String[clsCount + clsWithoutDataCount];
		ClsUsageData[] classes = new ClsUsageData[clsCount];
		int c = 0;
		for (int i = 0; i < clsCount; i++) {
			String clsRawName = in.readUTF();
			classes[i] = data.getClassData(clsRawName);
			clsNames[c++] = clsRawName;
		}
		for (int i = 0; i < clsWithoutDataCount; i++) {
			clsNames[c++] = in.readUTF();
		}
		ClassNode[] resolvedClasses = new ClassNode[clsNames.length];
		for (int i = 0; i < clsNames.length; i++) {
			resolvedClasses[i] = root.resolveRawClass(clsNames[i]);
		}
		int uClsCount = readUVInt(in);
		String[] uClsNames = new String[uClsCount];
		for (int i = 0; i < uClsCount; i++) {
			uClsNames[i] = in.readUTF();
		}

		// Method information
		int mthCount = readUVInt(in);
		MthRef[] methods = new MthRef[mthCount];
		for (int i = 0; i < mthCount; i++) {
			int clsId = readUVInt(in);
			int methodIndex = readUVInt(in);
			ClsUsageData cls = classes[clsId];
			ClassNode clsNode = resolvedClasses[clsId];
			MethodNode method = clsNode.getMethods().get(methodIndex);
			String mthShortId = method.getMethodInfo().getShortId();
			MthRef mthRef = new MthRef(method);
			cls.getMthUsage().put(mthShortId, new MthUsageData(mthRef));
			methods[i] = mthRef;
		}

		// Unresolved method information
		int uMthCount = readUVInt(in);
		IMethodRef[] unresolvedMethods = new IMethodRef[uMthCount];
		for (int i = 0; i < uMthCount; i++) {
			int clsId = readUVInt(in);
			String name = in.readUTF();
			String returnType = in.readUTF();
			int argCount = readUVInt(in);
			List<String> args = new ArrayList<>(argCount);
			for (int j = 0; j < argCount; j++) {
				args.add(in.readUTF());
			}
			unresolvedMethods[i] = new CachedMethodRef(uClsNames[clsId], name, returnType, args);
		}
		MethodInfo[] resolvedUnresolvedMethods = new MethodInfo[uMthCount];

		// Usage data
		for (int i = 0; i < clsCount; i++) {
			ClsUsageData cls = data.getClassData(clsNames[i]);
			cls.setResolvedClsDeps(readClsNodeList(in, resolvedClasses));
			cls.setResolvedClsUsage(readClsNodeList(in, resolvedClasses));
			cls.setResolvedClsUseInMth(readMethodNodeList(in, root, methods));

			int mCount = readUVInt(in);
			for (int m = 0; m < mCount; m++) {
				int methodIndex = readUVInt(in);
				MthRef mthRef = methods[methodIndex];
				MthUsageData mthUsageData = cls.getMthUsage().get(mthRef.getShortId());
				mthUsageData.setResolvedMethod(mthRef.resolve(root));
				mthUsageData.setResolvedUsage(readMethodNodeList(in, root, methods));
				mthUsageData.setResolvedUses(readMethodNodeList(in, root, methods));
				mthUsageData.setResolvedUnresolvedUsage(
						readMethodInfoList(in, root, unresolvedMethods, resolvedUnresolvedMethods));
				mthUsageData.setCallsSelf(in.readBoolean());
			}
			int fCount = readUVInt(in);
			for (int f = 0; f < fCount; f++) {
				String fldShortId = in.readUTF();
				cls.getFldUsage().computeIfAbsent(fldShortId,
						fldId -> new FldUsageData(new FldRef(cls.getRawName(), fldId)))
						.setResolvedUsage(readMethodNodeList(in, root, methods));
			}
		}
		return data;
	}

	private static void writeData(RootNode root, DataOutputStream out, RawUsageData usageData) throws IOException {
		Map<String, Integer> clsMap = new HashMap<>();
		Map<MthRef, Integer> mthMap = new HashMap<>();
		Map<IMethodRef, Integer> uMthMap = new HashMap<>();
		Map<String, ClsUsageData> clsDataMap = usageData.getClsMap();

		Map<String, Integer> uClsMap = new HashMap<>();
		List<IMethodRef> unresolvedMethods = clsDataMap.values().stream()
				.flatMap(classUsageData -> classUsageData.getMthUsage().values().stream())
				.flatMap(methodUsageData -> {
					List<IMethodRef> unresolvedUsageList = methodUsageData.getUnresolvedUsage();
					return unresolvedUsageList == null ? null : unresolvedUsageList.stream();
				})
				.filter(Objects::nonNull)
				.collect(Collectors.toList());

		List<String> classes = new ArrayList<>(clsDataMap.keySet());
		Collections.sort(classes);
		List<String> classesWithoutData = usageData.getClassesWithoutData();

		// pool for classes from unresolved methods
		Set<String> uClsNames = new HashSet<>();
		for (IMethodRef uMthRef : unresolvedMethods) {
			uClsNames.add(uMthRef.getParentClassType());
		}
		List<String> uClsList = new ArrayList<>(uClsNames);
		Collections.sort(uClsList);

		// Class information
		writeUVInt(out, classes.size());
		writeUVInt(out, classesWithoutData.size());
		int i = 0;
		for (String cls : classes) {
			out.writeUTF(cls);
			clsMap.put(cls, i++);
		}
		for (String cls : classesWithoutData) {
			out.writeUTF(cls);
			clsMap.put(cls, i++);
		}

		writeUVInt(out, uClsList.size());
		int u = 0;
		for (String cls : uClsList) {
			out.writeUTF(cls);
			uClsMap.put(cls, u++);
		}

		// Method information
		List<MthRef> methods = clsDataMap.values().stream()
				.flatMap(c -> c.getMthUsage().values().stream())
				.map(MthUsageData::getMthRef)
				.collect(Collectors.toList());
		writeUVInt(out, methods.size());
		int j = 0;
		String currentCls = null;
		Map<MethodNode, Integer> methodIndexes = Collections.emptyMap();
		for (MthRef mth : methods) {
			if (!mth.getCls().equals(currentCls)) {
				currentCls = mth.getCls();
				ClassNode clsNode = root.resolveRawClass(currentCls);
				if (clsNode == null) {
					throw new JadxRuntimeException("Unknown method class in usage: " + currentCls);
				}
				List<MethodNode> classMethods = clsNode.getMethods();
				methodIndexes = new IdentityHashMap<>(classMethods.size());
				for (int methodIndex = 0; methodIndex < classMethods.size(); methodIndex++) {
					methodIndexes.put(classMethods.get(methodIndex), methodIndex);
				}
			}
			writeUVInt(out, clsMap.get(mth.getCls()));
			Integer methodIndex = methodIndexes.get(mth.resolve(root));
			if (methodIndex == null) {
				throw new JadxRuntimeException("Unknown method in usage: " + mth.getCls() + '.' + mth.getShortId());
			}
			writeUVInt(out, methodIndex);
			mthMap.put(mth, j++);
		}

		// Unresolved method information
		writeUVInt(out, unresolvedMethods.size());
		int k = 0;
		for (IMethodRef uMthRef : unresolvedMethods) {
			writeUVInt(out, uClsMap.get(uMthRef.getParentClassType()));
			out.writeUTF(uMthRef.getName());
			out.writeUTF(uMthRef.getReturnType());
			List<String> args = uMthRef.getArgTypes();
			writeUVInt(out, args.size());
			for (String arg : args) {
				out.writeUTF(arg);
			}
			uMthMap.put(uMthRef, k++);
		}

		// Usage data
		for (String cls : classes) {
			ClsUsageData clsData = clsDataMap.get(cls);
			writeClsList(out, clsMap, clsData.getClsDeps());
			writeClsList(out, clsMap, clsData.getClsUsage());
			writeMthList(out, mthMap, clsData.getClsUseInMth());

			writeUVInt(out, clsData.getMthUsage().size());
			for (MthUsageData mthData : clsData.getMthUsage().values()) {
				writeUVInt(out, mthMap.get(mthData.getMthRef()));
				writeMthList(out, mthMap, mthData.getUsage());
				writeMthList(out, mthMap, mthData.getUses());
				writeUnresolvedMthList(out, uMthMap, mthData.getUnresolvedUsage());
				out.writeBoolean(mthData.callsSelf());
			}

			writeUVInt(out, clsData.getFldUsage().size());
			for (FldUsageData fldData : clsData.getFldUsage().values()) {
				out.writeUTF(fldData.getFldRef().getShortId());
				writeMthList(out, mthMap, fldData.getUsage());
			}
		}
	}

	private static List<ClassNode> readClsNodeList(DataInputStream in, ClassNode[] classes) throws IOException {
		int count = readUVInt(in);
		if (count == 0) {
			return Collections.emptyList();
		}
		List<ClassNode> list = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			list.add(classes[readUVInt(in)]);
		}
		return list;
	}

	private static void writeClsList(DataOutputStream out, Map<String, Integer> clsMap, List<String> clsList) throws IOException {
		if (Utils.isEmpty(clsList)) {
			writeUVInt(out, 0);
			return;
		}
		writeUVInt(out, clsList.size());
		for (String cls : clsList) {
			Integer clsId = clsMap.get(cls);
			if (clsId == null) {
				throw new JadxRuntimeException("Unknown class in usage: " + cls);
			}
			writeUVInt(out, clsId);
		}
	}

	private static List<MethodNode> readMethodNodeList(DataInputStream in, RootNode root, MthRef[] refs) throws IOException {
		int count = readUVInt(in);
		if (count == 0) {
			return Collections.emptyList();
		}
		List<MethodNode> list = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			int index = readUVInt(in);
			list.add(refs[index].resolve(root));
		}
		return list;
	}

	private static void writeMthList(DataOutputStream out, Map<MthRef, Integer> mthMap, List<MthRef> mthList) throws IOException {
		if (Utils.isEmpty(mthList)) {
			writeUVInt(out, 0);
			return;
		}
		writeUVInt(out, mthList.size());
		for (MthRef mth : mthList) {
			writeUVInt(out, mthMap.get(mth));
		}
	}

	private static List<MethodInfo> readMethodInfoList(DataInputStream in, RootNode root, IMethodRef[] refs,
			MethodInfo[] methods) throws IOException {
		int count = readUVInt(in);
		if (count == 0) {
			return Collections.emptyList();
		}
		List<MethodInfo> list = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			int index = readUVInt(in);
			MethodInfo method = methods[index];
			if (method == null) {
				method = MethodInfo.fromRef(root, refs[index]);
				methods[index] = method;
			}
			list.add(method);
		}
		return list;
	}

	private static void writeUnresolvedMthList(DataOutputStream out, Map<IMethodRef, Integer> uMthMap, List<IMethodRef> mthList)
			throws IOException {
		if (Utils.isEmpty(mthList)) {
			writeUVInt(out, 0);
			return;
		}
		writeUVInt(out, mthList.size());
		for (IMethodRef mth : mthList) {
			writeUVInt(out, uMthMap.get(mth));
		}
	}

	private static String buildInputsHash(RootNode root, List<File> inputs) {
		JadxDecompiler decompiler = root.getDecompiler();
		if (decompiler != null) {
			return decompiler.getAnalysisFingerprint();
		}
		List<Path> paths = inputs.stream()
				.filter(f -> !f.getName().endsWith(".jadx.kts"))
				.map(File::toPath)
				.collect(Collectors.toList());
		return FileUtils.buildInputsContentHash(paths);
	}
}
