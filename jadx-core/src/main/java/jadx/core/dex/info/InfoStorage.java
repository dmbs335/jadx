package jadx.core.dex.info;

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
	private Map<Integer, MethodInfo> methods = new HashMap<>();

	private final Map<String, PackageInfo> packages = new HashMap<>();

	public void prepare(int classesCount, int methodsCount, int fieldsCount, int typesCount) {
		if (!classes.isEmpty() || !types.isEmpty() || !classesByInputName.isEmpty()
				|| !fields.isEmpty() || !uniqueMethods.isEmpty() || !methods.isEmpty()) {
			return;
		}
		classes = newMap(typesCount);
		types = newMap(typesCount);
		classesByInputName = newMap(classesCount);
		fields = newMap(fieldsCount);
		uniqueMethods = newMap(methodsCount);
		methods = newMap(methodsCount);
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
		synchronized (methods) {
			return methods.get(id);
		}
	}

	public void putByUniqId(int id, MethodInfo mth) {
		synchronized (methods) {
			methods.put(id, mth);
		}
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
