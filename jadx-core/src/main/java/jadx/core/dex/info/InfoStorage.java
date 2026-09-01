package jadx.core.dex.info;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import jadx.core.dex.instructions.args.ArgType;
import jadx.core.utils.Utils;

public class InfoStorage {

	private Map<ArgType, ClassInfo> classes = new HashMap<>();
	private Map<String, ArgType> types = new HashMap<>();
	private Map<String, ClassInfo> classesByInputName = new HashMap<>();
	private Map<FieldInfo, FieldInfo> fields = new HashMap<>();
	// use only one MethodInfo instance
	private Map<MethodInfo, MethodInfo> uniqueMethods = new HashMap<>();
	// can contain same method with different ids (from different files)
	private MethodInfo[][] methodsBySource = new MethodInfo[0][];
	private final Object methodsBySourceLock = new Object();

	private final Map<String, PackageInfo> packages = new HashMap<>();

	public void prepare(int classesCount, int methodsCount, int fieldsCount, int typesCount) {
		if (!classes.isEmpty() || !types.isEmpty() || !classesByInputName.isEmpty()
				|| !fields.isEmpty() || !uniqueMethods.isEmpty() || methodsBySource.length != 0) {
			return;
		}
		classes = newMap(typesCount);
		types = newMap(typesCount);
		classesByInputName = newMap(classesCount);
		fields = newMap(fieldsCount);
		uniqueMethods = newMap(methodsCount);
	}

	private static <K, V> Map<K, V> newMap(int expectedSize) {
		return expectedSize > 0 ? Utils.newHashMap(expectedSize) : new HashMap<>();
	}

	public ClassInfo getCls(ArgType type) {
		return classes.get(type);
	}

	public ClassInfo putCls(ClassInfo cls) {
		synchronized (classes) {
			ClassInfo prev = classes.put(cls.getType(), cls);
			return prev == null ? cls : prev;
		}
	}

	public ArgType getType(String type) {
		synchronized (types) {
			return types.computeIfAbsent(type, ArgType::parse);
		}
	}

	public @Nullable ClassInfo getClsByInputName(String name) {
		synchronized (classesByInputName) {
			return classesByInputName.get(name);
		}
	}

	public ClassInfo putClsByInputName(String name, ClassInfo cls) {
		synchronized (classesByInputName) {
			ClassInfo prev = classesByInputName.putIfAbsent(name, cls);
			return prev == null ? cls : prev;
		}
	}

	public MethodInfo getByUniqId(int id) {
		int source = id >>> 16;
		int index = id & 0xFFFF;
		synchronized (methodsBySourceLock) {
			if (source >= methodsBySource.length) {
				return null;
			}
			MethodInfo[] methods = methodsBySource[source];
			return methods == null || index >= methods.length ? null : methods[index];
		}
	}

	public void putByUniqId(int id, MethodInfo mth) {
		int source = id >>> 16;
		int index = id & 0xFFFF;
		synchronized (methodsBySourceLock) {
			if (source >= methodsBySource.length) {
				methodsBySource = Arrays.copyOf(methodsBySource, growSize(methodsBySource.length, source + 1, 0x10000));
			}
			MethodInfo[] methods = methodsBySource[source];
			if (methods == null) {
				methods = new MethodInfo[growSize(0, index + 1, 0x10000)];
				methodsBySource[source] = methods;
			} else if (index >= methods.length) {
				methods = Arrays.copyOf(methods, growSize(methods.length, index + 1, 0x10000));
				methodsBySource[source] = methods;
			}
			methods[index] = mth;
		}
	}

	private static int growSize(int current, int required, int limit) {
		int grown = Math.max(16, current);
		while (grown < required) {
			grown = Math.min(grown << 1, limit);
		}
		return grown;
	}

	public MethodInfo putMethod(MethodInfo newMth) {
		synchronized (uniqueMethods) {
			MethodInfo prev = uniqueMethods.get(newMth);
			if (prev != null) {
				return prev;
			}
			uniqueMethods.put(newMth, newMth);
			return newMth;
		}
	}

	public FieldInfo getField(FieldInfo field) {
		synchronized (fields) {
			FieldInfo f = fields.get(field);
			if (f != null) {
				return f;
			}
			fields.put(field, field);
			return field;
		}
	}

	public @Nullable PackageInfo getPkg(String fullName) {
		return packages.get(fullName);
	}

	public void putPkg(PackageInfo pkg) {
		packages.put(pkg.getFullName(), pkg);
	}
}
