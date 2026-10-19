package jadx.core.dex.instructions.args;

public enum PrimitiveType {
	BOOLEAN("Z", "boolean", "java.lang.Boolean"),
	CHAR("C", "char", "java.lang.Character"),
	BYTE("B", "byte", "java.lang.Byte"),
	SHORT("S", "short", "java.lang.Short"),
	INT("I", "int", "java.lang.Integer"),
	FLOAT("F", "float", "java.lang.Float"),
	LONG("J", "long", "java.lang.Long"),
	DOUBLE("D", "double", "java.lang.Double"),
	OBJECT("L", "OBJECT", "java.lang.Object"),
	ARRAY("[", "ARRAY", "java.lang.Object"),
	VOID("V", "void", "java.lang.Void");

	private final String shortName;
	private final String longName;
	private final String boxClassName;

	PrimitiveType(String shortName, String longName, String boxClassName) {
		this.shortName = shortName;
		this.longName = longName;
		this.boxClassName = boxClassName;
	}

	public String getShortName() {
		return shortName;
	}

	public String getLongName() {
		return longName;
	}

	public ArgType getBoxType() {
		return BoxTypes.TYPES[ordinal()];
	}

	// ArgType initializes its primitive constants from this enum. Do not initialize
	// ArgType while constructing the enum, or either initialization order can see nulls.
	private static final class BoxTypes {
		private static final ArgType[] TYPES = init();

		private static ArgType[] init() {
			PrimitiveType[] primitives = values();
			ArgType[] types = new ArgType[primitives.length];
			for (PrimitiveType primitive : primitives) {
				types[primitive.ordinal()] = primitive == ARRAY
						? ArgType.OBJECT_ARRAY
						: ArgType.object(primitive.boxClassName);
			}
			return types;
		}
	}

	@Override
	public String toString() {
		return longName;
	}

	public boolean isObjectOrArray() {
		return this == OBJECT || this == ARRAY;
	}
}
