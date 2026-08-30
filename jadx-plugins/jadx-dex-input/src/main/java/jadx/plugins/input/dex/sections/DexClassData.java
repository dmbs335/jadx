package jadx.plugins.input.dex.sections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.api.plugins.input.data.IClassData;
import jadx.api.plugins.input.data.IFieldData;
import jadx.api.plugins.input.data.IMethodData;
import jadx.api.plugins.input.data.ISeqConsumer;
import jadx.api.plugins.input.data.annotations.EncodedValue;
import jadx.api.plugins.input.data.annotations.IAnnotation;
import jadx.api.plugins.input.data.attributes.IJadxAttribute;
import jadx.api.plugins.input.data.attributes.types.SourceFileAttr;
import jadx.plugins.input.dex.sections.annotations.AnnotationsOffsets;
import jadx.plugins.input.dex.sections.annotations.AnnotationsParser;
import jadx.plugins.input.dex.utils.SmaliUtils;

public class DexClassData extends SectionReader implements IClassData {
	private static final Logger LOG = LoggerFactory.getLogger(DexClassData.class);
	public static final int SIZE = 8 * 4;

	private final AnnotationsParser annotationsParser;
	private final int inputFileOffset;

	public DexClassData(SectionReader sectionReader) {
		super(sectionReader);
		this.annotationsParser = new AnnotationsParser(sectionReader, this);
		this.inputFileOffset = getOffset();
	}

	@Override
	public int getInputFileOffset() {
		return this.inputFileOffset;
	}

	@Override
	public DexClassData copy() {
		return new DexClassData(this);
	}

	@Override
	public String getType() {
		int typeIdx = pos(0).readInt();
		String clsType = getType(typeIdx);
		if (clsType == null) {
			throw new NullPointerException("Unknown class type");
		}
		return clsType;
	}

	@Override
	public int getAccessFlags() {
		return pos(4).readInt();
	}

	@Nullable
	@Override
	public String getSuperType() {
		int typeIdx = pos(2 * 4).readInt();
		return getType(typeIdx);
	}

	@Override
	public List<String> getInterfacesTypes() {
		int offset = pos(3 * 4).readInt();
		if (offset == 0) {
			return Collections.emptyList();
		}
		return absPos(offset).readTypeList();
	}

	@Nullable
	private String getSourceFile() {
		int strIdx = pos(4 * 4).readInt();
		return getString(strIdx);
	}

	@Override
	public String getInputFileName() {
		return getDexReader().getInputFileName();
	}

	public int getAnnotationsOff() {
		return pos(5 * 4).readInt();
	}

	public int getClassDataOff() {
		return pos(6 * 4).readInt();
	}

	public int getStaticValuesOff() {
		return pos(7 * 4).readInt();
	}

	@Override
	public void visitFieldsAndMethods(ISeqConsumer<IFieldData> fieldConsumer, ISeqConsumer<IMethodData> mthConsumer) {
		int classDataOff = getClassDataOff();
		if (classDataOff == 0) {
			return;
		}
		SectionReader data = copy(classDataOff);
		int staticFieldsCount = data.readUleb128();
		int instanceFieldsCount = data.readUleb128();
		int directMthCount = data.readUleb128();
		int virtualMthCount = data.readUleb128();

		fieldConsumer.init(staticFieldsCount + instanceFieldsCount);
		mthConsumer.init(directMthCount + virtualMthCount);

		annotationsParser.setOffset(getAnnotationsOff());
		visitFields(fieldConsumer, data, staticFieldsCount, instanceFieldsCount);
		visitMethods(mthConsumer, data, directMthCount, virtualMthCount);
	}

	private void visitFields(Consumer<IFieldData> fieldConsumer, SectionReader data, int staticFieldsCount, int instanceFieldsCount) {
		AnnotationsOffsets annotationOffsets = annotationsParser.readFieldsAnnotationOffsets();
		DexFieldData fieldData = new DexFieldData(annotationsParser);
		fieldData.setParentClassType(getType());
		readFields(fieldConsumer, data, fieldData, staticFieldsCount, annotationOffsets, true);
		readFields(fieldConsumer, data, fieldData, instanceFieldsCount, annotationOffsets, false);
	}

	private void readFields(Consumer<IFieldData> fieldConsumer, SectionReader data, DexFieldData fieldData, int count,
			AnnotationsOffsets annotationOffsets, boolean staticFields) {
		List<EncodedValue> constValues = staticFields ? getStaticFieldInitValues(data.copy()) : null;
		int fieldId = 0;
		for (int i = 0; i < count; i++) {
			fieldId += data.readUleb128();
			int accFlags = data.readUleb128();
			fillFieldData(fieldData, fieldId);
			fieldData.setAccessFlags(accFlags);
			fieldData.setAnnotationsOffset(annotationOffsets.get(fieldId));
			fieldData.setConstValue(staticFields && i < constValues.size() ? constValues.get(i) : null);
			fieldConsumer.accept(fieldData);
		}
	}

	private void visitMethods(Consumer<IMethodData> mthConsumer, SectionReader data, int directMthCount, int virtualMthCount) {
		DexMethodData methodData = new DexMethodData(annotationsParser);
		methodData.setMethodRef(new DexMethodRef());
		AnnotationsOffsets annotationOffsets = annotationsParser.readMethodsAnnotationOffsets();
		AnnotationsOffsets paramsAnnotationOffsets = annotationsParser.readMethodParamsAnnRefOffsets();

		readMethods(mthConsumer, data, methodData, directMthCount, annotationOffsets, paramsAnnotationOffsets);
		readMethods(mthConsumer, data, methodData, virtualMthCount, annotationOffsets, paramsAnnotationOffsets);
	}

	private void readMethods(Consumer<IMethodData> mthConsumer, SectionReader data, DexMethodData methodData, int count,
			AnnotationsOffsets annotationOffsets, AnnotationsOffsets paramsAnnotationOffsets) {
		DexCodeReader dexCodeReader = new DexCodeReader(super.copy());
		int mthIdx = 0;
		for (int i = 0; i < count; i++) {
			mthIdx += data.readUleb128();
			int accFlags = data.readUleb128();
			int codeOff = data.readUleb128();

			DexMethodRef methodRef = methodData.getMethodRef();
			methodRef.reset();
			initMethodRef(mthIdx, methodRef);
			methodData.setAccessFlags(accFlags);
			if (codeOff == 0) {
				methodData.setCodeReader(null);
			} else {
				dexCodeReader.setMthId(mthIdx);
				dexCodeReader.setOffset(codeOff);
				methodData.setCodeReader(dexCodeReader);
			}
			methodData.setAnnotationsOffset(annotationOffsets.get(mthIdx));
			methodData.setParamAnnotationsOffset(paramsAnnotationOffsets.get(mthIdx));
			mthConsumer.accept(methodData);
		}
	}

	private List<EncodedValue> getStaticFieldInitValues(SectionReader reader) {
		int staticValuesOff = getStaticValuesOff();
		if (staticValuesOff == 0) {
			return Collections.emptyList();
		}
		reader.absPos(staticValuesOff);
		return annotationsParser.parseEncodedArray(reader);
	}

	private List<IAnnotation> getAnnotations() {
		annotationsParser.setOffset(getAnnotationsOff());
		return annotationsParser.readClassAnnotations();
	}

	@Override
	public List<IJadxAttribute> getAttributes() {
		List<IJadxAttribute> list = new ArrayList<>();
		String sourceFile = getSourceFile();
		if (sourceFile != null && !sourceFile.isEmpty()) {
			list.add(new SourceFileAttr(sourceFile));
		}
		DexAnnotationsConvert.forClass(getType(), list, getAnnotations());
		return list;
	}

	public int getClassDefOffset() {
		return pos(0).getAbsPos();
	}

	@Override
	public String getDisassembledCode() {
		byte[] dexBuf = getDexReader().getBuf().array();
		return SmaliUtils.getSmaliCode(dexBuf, getClassDefOffset());
	}

	@Override
	public String toString() {
		return getType();
	}
}
