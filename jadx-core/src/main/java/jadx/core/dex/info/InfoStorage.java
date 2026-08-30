package jadx.core.dex.info;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import jadx.core.dex.instructions.args.ArgType;

public class InfoStorage {

	private final Map<ArgType, ClassInfo> classes = new HashMap<>();
	private final Map<String, ArgType> types = new HashMap<>();
	private final Map<String, ClassInfo> classesByInputName = new HashMap<>();
	private final Map<FieldInfo, FieldInfo> fields = new HashMap<>();
	// use only one MethodInfo instance
	private final Map<MethodInfo, MethodInfo> uniqueMethods = new HashMap<>();
	// can contain same method with different ids (from different files)
	private final Map<Integer, MethodInfo> methods = new HashMap<>();

	private final Map<String, PackageInfo> packages = new HashMap<>();

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
