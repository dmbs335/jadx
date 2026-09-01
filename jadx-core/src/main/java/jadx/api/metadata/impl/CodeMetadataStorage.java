package jadx.api.metadata.impl;

import java.util.Collections;
import java.util.Map;
import java.util.function.BiFunction;

import org.jetbrains.annotations.Nullable;

import jadx.api.metadata.ICodeAnnotation;
import jadx.api.metadata.ICodeAnnotation.AnnType;
import jadx.api.metadata.ICodeMetadata;
import jadx.api.metadata.ICodeNodeRef;
import jadx.api.metadata.annotations.NodeDeclareRef;
import jadx.api.impl.CodePositionMap;
import jadx.core.utils.Utils;

public class CodeMetadataStorage implements ICodeMetadata {

	public static ICodeMetadata build(Map<Integer, Integer> lines, Map<Integer, ICodeAnnotation> map) {
		if (map.isEmpty() && lines.isEmpty()) {
			return ICodeMetadata.EMPTY;
		}
		return new CodeMetadataStorage(lines, CodePositionMap.copyOf(map));
	}

	public static ICodeMetadata empty() {
		return new CodeMetadataStorage(Collections.emptyMap(), CodePositionMap.copyOf(Collections.emptyMap()));
	}

	// <decomp file line number> -> <dex debug line number>
	private final Map<Integer, Integer> lines;

	// <character index into the file> -> <code annotation>
	// the key is what is returned by AbstractCodeArea#getCaretPos() when clicking in a code panel.
	private final CodePositionMap<ICodeAnnotation> positionMap;

	private CodeMetadataStorage(Map<Integer, Integer> lines, CodePositionMap<ICodeAnnotation> positionMap) {
		this.lines = lines;
		this.positionMap = positionMap;
	}

	@Override
	public ICodeAnnotation getAt(int position) {
		return positionMap.get(position);
	}

	@Override
	public @Nullable ICodeAnnotation getClosestUp(int position) {
		int index = positionMap.lowerIndex(position);
		return index >= 0 ? positionMap.valueAt(index) : null;
	}

	@Override
	public @Nullable ICodeAnnotation searchUp(int position, AnnType annType) {
		for (int i = positionMap.floorIndex(position); i >= 0; i--) {
			ICodeAnnotation v = positionMap.valueAt(i);
			if (v.getAnnType() == annType) {
				return v;
			}
		}
		return null;
	}

	@Override
	public @Nullable ICodeAnnotation searchUp(int position, int limitPos, AnnType annType) {
		for (int i = positionMap.floorIndex(position); i >= 0 && positionMap.keyAt(i) >= limitPos; i--) {
			ICodeAnnotation v = positionMap.valueAt(i);
			if (v.getAnnType() == annType) {
				return v;
			}
		}
		return null;
	}

	@Override
	public <T> @Nullable T searchUp(int startPos, BiFunction<Integer, ICodeAnnotation, T> visitor) {
		for (int i = positionMap.floorIndex(startPos); i >= 0; i--) {
			T value = visitor.apply(positionMap.keyAt(i), positionMap.valueAt(i));
			if (value != null) {
				return value;
			}
		}
		return null;
	}

	@Override
	public <T> @Nullable T searchDown(int startPos, BiFunction<Integer, ICodeAnnotation, T> visitor) {
		for (int i = positionMap.ceilingIndex(startPos); i < positionMap.size(); i++) {
			T value = visitor.apply(positionMap.keyAt(i), positionMap.valueAt(i));
			if (value != null) {
				return value;
			}
		}
		return null;
	}

	@Override
	public ICodeNodeRef getNodeAt(int position) {
		int nesting = 0;
		for (int i = positionMap.floorIndex(position); i >= 0; i--) {
			ICodeAnnotation ann = positionMap.valueAt(i);
			switch (ann.getAnnType()) {
				case END:
					nesting++;
					break;

				case DECLARATION:
					ICodeNodeRef node = ((NodeDeclareRef) ann).getNode();
					AnnType nodeType = node.getAnnType();
					if (nodeType == AnnType.CLASS || nodeType == AnnType.METHOD) {
						if (nesting == 0) {
							return node;
						}
						nesting--;
					}
					break;
			}
		}
		return null;
	}

	@Override
	public ICodeNodeRef getNodeBelow(int position) {
		for (int i = positionMap.ceilingIndex(position); i < positionMap.size(); i++) {
			ICodeAnnotation ann = positionMap.valueAt(i);
			if (ann.getAnnType() == AnnType.DECLARATION) {
				ICodeNodeRef node = ((NodeDeclareRef) ann).getNode();
				AnnType nodeType = node.getAnnType();
				if (nodeType == AnnType.CLASS || nodeType == AnnType.METHOD) {
					return node;
				}
			}
		}
		return null;
	}

	@Override
	public Map<Integer, ICodeAnnotation> getAsMap() {
		return positionMap;
	}

	@Override
	public Map<Integer, Integer> getLineMapping() {
		return lines;
	}

	@Override
	public String toString() {
		return "CodeMetadata{\nlines=" + lines
				+ "\nannotations=\n " + Utils.listToString(positionMap.entrySet(), "\n ") + "\n}";
	}
}
