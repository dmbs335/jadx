package jadx.core.clsp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.core.Consts;
import jadx.core.dex.info.MethodInfo;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.nodes.ClassNode;
import jadx.core.dex.nodes.IMethodDetails;
import jadx.core.dex.nodes.RootNode;
import jadx.core.utils.ImmutableArraySet;
import jadx.core.utils.Utils;
import jadx.core.utils.exceptions.DecodeException;
import jadx.core.utils.exceptions.JadxRuntimeException;

/**
 * Classes hierarchy graph with methods additional info
 */
public class ClspGraph {
	private static final Logger LOG = LoggerFactory.getLogger(ClspGraph.class);
	private static final int ARRAY_SET_MAX_SIZE = 6;
	private static final int CLASS_LOOKUP_CACHE_SIZE = 256;
	private static final int CLASS_LOOKUP_CACHE_MASK = CLASS_LOOKUP_CACHE_SIZE - 1;

	private final RootNode root;
	private Map<String, ClspClass> nameMap;
	private Map<String, List<ArgType>> implementsCache;
	private final ThreadLocal<ClassLookupCache> classLookupCache = ThreadLocal.withInitial(ClassLookupCache::new);

	private final Set<String> missingClasses = ConcurrentHashMap.newKeySet();

	public ClspGraph(RootNode rootNode) {
		this.root = rootNode;
	}

	public void loadClsSetFile() throws IOException, DecodeException {
		ClsSet set = new ClsSet(root);
		set.loadFromClstFile();
		addClasspath(set);
	}

	public void addClasspath(ClsSet set) {
		if (nameMap == null) {
			nameMap = Utils.newHashMap(set.getClassesCount());
			set.addToMap(nameMap);
		} else {
			throw new JadxRuntimeException("Classpath already loaded");
		}
	}

	public void addApp(List<ClassNode> classes) {
		classLookupCache.remove();
		if (nameMap == null) {
			nameMap = Utils.newHashMap(classes.size());
		} else if (!classes.isEmpty()) {
			Map<String, ClspClass> expandedMap = Utils.newHashMap(nameMap.size() + classes.size());
			expandedMap.putAll(nameMap);
			nameMap = expandedMap;
		}
		for (ClassNode cls : classes) {
			addClass(cls);
		}
	}

	public void initCache() {
		fillSuperTypesCache();
		fillImplementsCache();
	}

	public boolean isClsKnown(String fullName) {
		return lookupClass(fullName) != null;
	}

	public ClspClass getClsDetails(ArgType type) {
		return lookupClass(type.getObject());
	}

	public ClspClass getClsDetails(String fullClsName) {
		return lookupClass(fullClsName);
	}

	public Map<String, ClspClass> getClsNameMap() {
		return nameMap;
	}

	@Nullable
	public ArgType getClsType(String fullName) {
		ClspClass cls = lookupClass(fullName);
		return cls == null ? null : cls.getClsType();
	}

	@Nullable
	public IMethodDetails getMethodDetails(MethodInfo methodInfo) {
		ClspClass cls = lookupClass(methodInfo.getDeclClass().getRawName());
		if (cls == null) {
			return null;
		}
		ClspMethod clspMethod = getMethodFromClass(cls, methodInfo);
		if (clspMethod != null) {
			return clspMethod;
		}
		// deep search
		for (ArgType parent : cls.getParents()) {
			ClspClass clspParent = getClspClass(parent);
			if (clspParent != null) {
				ClspMethod methodFromParent = getMethodFromClass(clspParent, methodInfo);
				if (methodFromParent != null) {
					return methodFromParent;
				}
			}
		}
		// unknown method
		return new SimpleMethodDetails(methodInfo);
	}

	private ClspMethod getMethodFromClass(ClspClass cls, MethodInfo methodInfo) {
		return cls.getMethodsMap().get(methodInfo.getShortId());
	}

	private void addClass(ClassNode cls) {
		ArgType clsType = cls.getClassInfo().getType();
		String rawName = clsType.getObject();
		ClspClass clspClass = new ClspClass(clsType, -1, cls.getAccessFlags().rawValue(), ClspClassSource.APP);
		clspClass.setParents(ClsSet.makeParentsArray(cls));
		nameMap.put(rawName, clspClass);
	}

	/**
	 * @return {@code clsName} instanceof {@code implClsName}
	 */
	public boolean isImplements(String clsName, String implClsName) {
		Set<String> anc = getSuperTypes(clsName);
		return anc.contains(implClsName);
	}

	public List<ArgType> getImplementationTypes(String clsName) {
		List<ArgType> list = implementsCache.get(clsName);
		return list == null ? Collections.emptyList() : list;
	}

	public List<String> getImplementations(String clsName) {
		List<ArgType> types = getImplementationTypes(clsName);
		if (types.isEmpty()) {
			return Collections.emptyList();
		}
		List<String> names = new ArrayList<>(types.size());
		for (ArgType type : types) {
			names.add(type.getObject());
		}
		return names;
	}

	private void fillImplementsCache() {
		Map<String, List<ArgType>> map = Utils.newHashMap(nameMap.size());
		List<String> classes = new ArrayList<>(nameMap.keySet());
		Collections.sort(classes);
		for (String clsName : classes) {
			ArgType clsType = nameMap.get(clsName).getClsType();
			for (String st : getSuperTypes(clsName)) {
				map.computeIfAbsent(st, v -> new ArrayList<>()).add(clsType);
			}
		}
		implementsCache = map;
	}

	public @Nullable String getCommonAncestor(String clsName, String implClsName) {
		if (clsName.equals(implClsName)) {
			return clsName;
		}
		ClspClass cls = lookupClass(implClsName);
		if (cls == null) {
			missingClasses.add(clsName);
			return null;
		}
		if (isImplements(clsName, implClsName)) {
			return implClsName;
		}
		Set<String> anc = getSuperTypes(clsName);
		return searchCommonParent(anc, cls);
	}

	private @Nullable String searchCommonParent(Set<String> anc, ClspClass cls) {
		for (ArgType p : cls.getParents()) {
			String name = p.getObject();
			if (anc.contains(name)) {
				return name;
			}
			ClspClass nCls = getClspClass(p);
			if (nCls != null) {
				String r = searchCommonParent(anc, nCls);
				if (r != null) {
					return r;
				}
			}
		}
		return null;
	}

	public Set<String> getSuperTypes(String clsName) {
		ClspClass cls = lookupClass(clsName);
		Set<String> result = cls == null ? null : cls.getSuperTypes();
		return result == null ? Collections.emptySet() : result;
	}

	private static final Set<String> OBJECT_SINGLE_SET = Collections.singleton(Consts.CLASS_OBJECT);

	private void fillSuperTypesCache() {
		Set<String> tmpSet = new HashSet<>();
		for (Map.Entry<String, ClspClass> entry : nameMap.entrySet()) {
			ClspClass cls = entry.getValue();
			tmpSet.clear();
			addSuperTypes(cls, tmpSet);
			Set<String> result;
			int size = tmpSet.size();
			switch (size) {
				case 0: {
					result = Collections.emptySet();
					break;
				}
				case 1: {
					String supCls = tmpSet.iterator().next();
					if (supCls.equals(Consts.CLASS_OBJECT)) {
						result = OBJECT_SINGLE_SET;
					} else {
						result = Collections.singleton(supCls);
					}
					break;
				}
				default: {
					result = size <= ARRAY_SET_MAX_SIZE
							? new ImmutableArraySet<>(tmpSet)
							: new HashSet<>(tmpSet);
					break;
				}
			}
			cls.setSuperTypes(result);
		}
	}

	private void addSuperTypes(ClspClass cls, Set<String> result) {
		for (ArgType parentType : cls.getParents()) {
			if (parentType == null) {
				continue;
			}
			ClspClass parentCls = getClspClass(parentType);
			if (parentCls != null) {
				String parentName = parentCls.getName();
				boolean isNew = result.add(parentName);
				if (isNew) {
					Set<String> cached = parentCls.getSuperTypes();
					if (cached == null) {
						addSuperTypes(parentCls, result);
					} else {
						result.addAll(cached);
					}
				}
			} else {
				// parent type is unknown
				result.add(parentType.getObject());
			}
		}
	}

	private @Nullable ClspClass getClspClass(ArgType clsType) {
		ClspClass clspClass = lookupClass(clsType.getObject());
		if (clspClass == null) {
			missingClasses.add(clsType.getObject());
		}
		return clspClass;
	}

	private @Nullable ClspClass lookupClass(String name) {
		ClassLookupCache cache = classLookupCache.get();
		int index = spreadHash(name.hashCode()) & CLASS_LOOKUP_CACHE_MASK;
		String cachedName = cache.names[index];
		if (name.equals(cachedName)) {
			return cache.classes[index];
		}
		ClspClass cls = nameMap.get(name);
		cache.names[index] = name;
		cache.classes[index] = cls;
		return cls;
	}

	private static int spreadHash(int hash) {
		return hash ^ (hash >>> 16);
	}

	private static final class ClassLookupCache {
		private final String[] names = new String[CLASS_LOOKUP_CACHE_SIZE];
		private final ClspClass[] classes = new ClspClass[CLASS_LOOKUP_CACHE_SIZE];
	}

	public void printMissingClasses() {
		int count = missingClasses.size();
		if (count == 0) {
			return;
		}
		LOG.warn("Found {} references to unknown classes", count);
		if (LOG.isDebugEnabled()) {
			List<String> clsNames = new ArrayList<>(missingClasses);
			Collections.sort(clsNames);
			for (String cls : clsNames) {
				LOG.debug("  {}", cls);
			}
		}
	}

}
