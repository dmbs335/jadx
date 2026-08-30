package jadx.plugins.input.dex.sections.annotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jadx.api.plugins.input.data.annotations.AnnotationVisibility;
import jadx.api.plugins.input.data.annotations.EncodedValue;
import jadx.api.plugins.input.data.annotations.IAnnotation;
import jadx.api.plugins.input.data.annotations.JadxAnnotation;
import jadx.plugins.input.dex.DexException;
import jadx.plugins.input.dex.sections.DexClassData;
import jadx.plugins.input.dex.sections.SectionReader;

public class AnnotationsParser extends SectionReader {
	private final DexClassData ext;

	private int offset;
	private int fieldsCount;
	private int methodsCount;
	private int paramsRefCount;

	public AnnotationsParser(SectionReader in, DexClassData ext) {
		super(in);
		this.ext = ext;
	}

	public void setOffset(int offset) {
		this.offset = offset;
		if (offset == 0) {
			this.fieldsCount = 0;
			this.methodsCount = 0;
			this.paramsRefCount = 0;
		} else {
			super.setOffset(offset);
			pos(4);
			this.fieldsCount = readInt();
			this.methodsCount = readInt();
			this.paramsRefCount = readInt();
		}
	}

	public List<IAnnotation> readClassAnnotations() {
		if (offset == 0) {
			return Collections.emptyList();
		}
		int classAnnotationsOffset = absPos(offset).readInt();
		return readAnnotationList(classAnnotationsOffset);
	}

	public AnnotationsOffsets readFieldsAnnotationOffsets() {
		if (fieldsCount == 0) {
			return AnnotationsOffsets.empty();
		}
		pos(4 * 4);
		return AnnotationsOffsets.read(this, fieldsCount);
	}

	public AnnotationsOffsets readMethodsAnnotationOffsets() {
		if (methodsCount == 0) {
			return AnnotationsOffsets.empty();
		}
		pos(4 * 4 + fieldsCount * 2 * 4);
		return AnnotationsOffsets.read(this, methodsCount);
	}

	public AnnotationsOffsets readMethodParamsAnnRefOffsets() {
		if (paramsRefCount == 0) {
			return AnnotationsOffsets.empty();
		}
		pos(4 * 4 + fieldsCount * 2 * 4 + methodsCount * 2 * 4);
		return AnnotationsOffsets.read(this, paramsRefCount);
	}

	public List<IAnnotation> readAnnotationList(int offset) {
		if (offset == 0) {
			return Collections.emptyList();
		}
		absPos(offset);
		int size = readInt();
		if (size == 0) {
			return Collections.emptyList();
		}
		List<IAnnotation> list = new ArrayList<>(size);
		int pos = getAbsPos();
		for (int i = 0; i < size; i++) {
			absPos(pos + i * 4);
			int annOffset = readInt();
			absPos(annOffset);
			list.add(readAnnotation(this, ext, true));
		}
		return list;
	}

	public List<List<IAnnotation>> readAnnotationRefList(int offset) {
		if (offset == 0) {
			return Collections.emptyList();
		}
		absPos(offset);
		int size = readInt();
		if (size == 0) {
			return Collections.emptyList();
		}
		List<List<IAnnotation>> list = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			int refOff = readInt();
			int pos = getAbsPos();
			list.add(readAnnotationList(refOff));
			absPos(pos);
		}
		return list;
	}

	public static IAnnotation readAnnotation(SectionReader in, SectionReader ext, boolean readVisibility) {
		AnnotationVisibility visibility = null;
		if (readVisibility) {
			int v = in.readUByte();
			visibility = getVisibilityValue(v);
		}
		int typeIndex = in.readUleb128();
		int size = in.readUleb128();
		String type = ext.getType(typeIndex);
		if (size == 0) {
			return new JadxAnnotation(visibility, type);
		}
		Map<String, EncodedValue> values = new LinkedHashMap<>(size);
		for (int i = 0; i < size; i++) {
			String name = ext.getStringCached(in.readUleb128());
			values.put(name, EncodedValueParser.parseValue(in, ext));
		}
		return new JadxAnnotation(visibility, type, values);
	}

	private static AnnotationVisibility getVisibilityValue(int value) {
		switch (value) {
			case 0:
				return AnnotationVisibility.BUILD;
			case 1:
				return AnnotationVisibility.RUNTIME;
			case 2:
				return AnnotationVisibility.SYSTEM;
			default:
				throw new DexException("Unknown annotation visibility value: " + value);
		}
	}

	public EncodedValue parseEncodedValue(SectionReader in) {
		return EncodedValueParser.parseValue(in, ext);
	}

	public List<EncodedValue> parseEncodedArray(SectionReader in) {
		return EncodedValueParser.parseEncodedArray(in, ext);
	}
}
