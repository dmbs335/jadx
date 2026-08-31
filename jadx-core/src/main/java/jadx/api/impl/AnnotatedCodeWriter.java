package jadx.api.impl;

import java.util.Collections;
import java.util.Map;

import org.jetbrains.annotations.ApiStatus;

import jadx.api.ICodeInfo;
import jadx.api.ICodeWriter;
import jadx.api.JadxArgs;
import jadx.api.metadata.ICodeAnnotation;
import jadx.api.metadata.ICodeNodeRef;
import jadx.api.metadata.annotations.NodeDeclareRef;
import jadx.core.utils.StringUtils;

public class AnnotatedCodeWriter extends SimpleCodeWriter implements ICodeWriter {

	private int line = 1;
	private int offset;
	private CodePositionMap<ICodeAnnotation> annotations;
	private CodePositionMap<Integer> lineMap;

	public AnnotatedCodeWriter(JadxArgs args) {
		super(args);
	}

	@Override
	public boolean isMetadataSupported() {
		return true;
	}

	@Override
	public AnnotatedCodeWriter addMultiLine(String str) {
		if (str.contains(newLineStr)) {
			buf.append(str.replace(newLineStr, newLineStr + indentStr));
			line += StringUtils.countMatches(str, newLineStr);
			offset = 0;
		} else {
			buf.append(str);
		}
		return this;
	}

	@Override
	public AnnotatedCodeWriter add(String str) {
		buf.append(str);
		offset += str.length();
		return this;
	}

	@Override
	public AnnotatedCodeWriter add(char c) {
		buf.append(c);
		offset++;
		return this;
	}

	@Override
	public ICodeWriter add(ICodeWriter cw) {
		if (!cw.isMetadataSupported()) {
			buf.append(cw.getCodeStr());
			return this;
		}
		AnnotatedCodeWriter code = (AnnotatedCodeWriter) cw;
		line--;
		int startPos = getLength();
		if (code.annotations != null) {
			initAnnotations().putAllShifted(code.annotations, startPos);
		}
		if (code.lineMap != null) {
			initLineMap().putAllShifted(code.lineMap, line);
		}
		line += code.line;
		offset = code.offset;
		buf.append(code.buf);
		return this;
	}

	@Override
	protected void addLine() {
		buf.append(newLineStr);
		line++;
		offset = 0;
	}

	@Override
	protected AnnotatedCodeWriter addLineIndent() {
		buf.append(indentStr);
		offset += indentStr.length();
		return this;
	}

	@Override
	public int getLine() {
		return line;
	}

	@Override
	public int getLineStartPos() {
		return getLength() - offset;
	}

	@Override
	public void attachDefinition(ICodeNodeRef obj) {
		if (obj == null) {
			return;
		}
		attachAnnotation(new NodeDeclareRef(obj));
	}

	@Override
	public void attachAnnotation(ICodeAnnotation obj) {
		if (obj == null) {
			return;
		}
		attachAnnotation(obj, getLength());
	}

	@Override
	public void attachLineAnnotation(ICodeAnnotation obj) {
		if (obj == null) {
			return;
		}
		attachAnnotation(obj, getLineStartPos());
	}

	private void attachAnnotation(ICodeAnnotation obj, int pos) {
		initAnnotations().putValue(pos, obj);
	}

	@Override
	public void attachSourceLine(int sourceLine) {
		if (sourceLine == 0) {
			return;
		}
		attachSourceLine(line, sourceLine);
	}

	private void attachSourceLine(int decompiledLine, int sourceLine) {
		initLineMap().putValue(decompiledLine, sourceLine);
	}

	@Override
	public ICodeInfo finish() {
		String code = buf.toString();
		buf = null;
		return new AnnotatedCodeInfo(code, getRawLineMapping(), getRawAnnotations());
	}

	@Override
	public Map<Integer, ICodeAnnotation> getRawAnnotations() {
		return annotations == null ? Collections.emptyMap() : annotations;
	}

	@ApiStatus.Internal
	public Map<Integer, Integer> getRawLineMapping() {
		return lineMap == null ? Collections.emptyMap() : lineMap;
	}

	private CodePositionMap<ICodeAnnotation> initAnnotations() {
		if (annotations == null) {
			// Annotation buffers are temporary and can grow large. Wider growth reduces repeated
			// backing-array copies before the final navigable metadata index is built.
			annotations = new CodePositionMap<>(2);
		}
		return annotations;
	}

	private CodePositionMap<Integer> initLineMap() {
		if (lineMap == null) {
			lineMap = new CodePositionMap<>();
		}
		return lineMap;
	}
}
