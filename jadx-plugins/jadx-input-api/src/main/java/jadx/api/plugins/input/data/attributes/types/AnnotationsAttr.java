package jadx.api.plugins.input.data.attributes.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.jetbrains.annotations.Nullable;

import jadx.api.plugins.input.data.annotations.AnnotationVisibility;
import jadx.api.plugins.input.data.annotations.IAnnotation;
import jadx.api.plugins.input.data.attributes.JadxAttrType;
import jadx.api.plugins.input.data.attributes.PinnedAttribute;

public class AnnotationsAttr extends PinnedAttribute {

	public static @Nullable AnnotationsAttr pack(List<IAnnotation> annotationList) {
		if (annotationList.isEmpty()) {
			return null;
		}
		IAnnotation first = null;
		Map<String, IAnnotation> annMap = null;
		for (IAnnotation ann : annotationList) {
			if (ann.getVisibility() == AnnotationVisibility.SYSTEM) {
				continue;
			}
			if (first == null) {
				first = ann;
				continue;
			}
			if (annMap == null) {
				annMap = new HashMap<>(annotationList.size());
				annMap.put(first.getAnnotationClass(), first);
			}
			annMap.put(ann.getAnnotationClass(), ann);
		}
		if (first == null) {
			return null;
		}
		if (annMap == null) {
			return new AnnotationsAttr(Collections.singletonMap(first.getAnnotationClass(), first));
		}
		if (annMap.size() == 1) {
			IAnnotation annotation = annMap.values().iterator().next();
			return new AnnotationsAttr(Collections.singletonMap(annotation.getAnnotationClass(), annotation));
		}
		return new AnnotationsAttr(annMap);
	}

	private final Map<String, IAnnotation> map;

	public AnnotationsAttr(Map<String, IAnnotation> map) {
		this.map = map;
	}

	public @Nullable IAnnotation get(String className) {
		return map.get(className);
	}

	public Collection<IAnnotation> getAll() {
		return map.values();
	}

	public void forEach(BiConsumer<? super String, ? super IAnnotation> action) {
		map.forEach(action);
	}

	public List<IAnnotation> getList() {
		if (map.isEmpty()) {
			return Collections.emptyList();
		}
		List<IAnnotation> list = new ArrayList<>(map.size());
		forEach((type, annotation) -> list.add(annotation));
		return list;
	}

	public int size() {
		return map.size();
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public JadxAttrType<AnnotationsAttr> getAttrType() {
		return JadxAttrType.ANNOTATION_LIST;
	}

	@Override
	public String toString() {
		return map.toString();
	}
}
