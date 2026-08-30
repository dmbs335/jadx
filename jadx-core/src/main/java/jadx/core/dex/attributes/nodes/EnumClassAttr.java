package jadx.core.dex.attributes.nodes;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import jadx.api.plugins.input.data.attributes.IJadxAttribute;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.CodeVar;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.mods.ConstructorInsn;
import jadx.core.dex.nodes.ClassNode;
import jadx.core.dex.nodes.FieldNode;
import jadx.core.dex.nodes.IContainer;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.nodes.MethodNode;

public class EnumClassAttr implements IJadxAttribute {

	public static class EnumField {
		private final FieldNode field;
		private final ConstructorInsn constrInsn;
		private final @Nullable String nameStr;
		private ClassNode cls;

		public EnumField(FieldNode field, ConstructorInsn co, @Nullable String nameStr) {
			this.field = field;
			this.constrInsn = co;
			this.nameStr = nameStr;
		}

		public FieldNode getField() {
			return field;
		}

		public ConstructorInsn getConstrInsn() {
			return constrInsn;
		}

		public ClassNode getCls() {
			return cls;
		}

		public void setCls(ClassNode cls) {
			this.cls = cls;
		}

		public @Nullable String getNameStr() {
			return nameStr;
		}

		@Override
		public String toString() {
			return field + "(" + constrInsn + ") " + cls;
		}
	}

	public static class EnumValueHelper {
		private final String name;
		private final ArgType returnType;
		private final InsnNode initInsn;
		private final InsnArg returnArg;

		public EnumValueHelper(String name, ArgType returnType, InsnNode initInsn, InsnArg returnArg) {
			this.name = name;
			this.returnType = returnType;
			this.initInsn = initInsn;
			this.returnArg = returnArg;
		}

		public String getName() {
			return name;
		}

		public ArgType getReturnType() {
			return returnType;
		}

		public InsnNode getInitInsn() {
			return initInsn;
		}

		public InsnArg getReturnArg() {
			return returnArg;
		}
	}

	public static class EnumRegionValueHelper {
		private final String name;
		private final ArgType returnType;
		private final List<CodeVar> declarations;
		private final List<InsnNode> prefixInsns;
		private final IContainer body;
		private final InsnArg returnArg;

		public EnumRegionValueHelper(
				String name,
				ArgType returnType,
				List<CodeVar> declarations,
				List<InsnNode> prefixInsns,
				IContainer body,
				InsnArg returnArg) {
			this.name = name;
			this.returnType = returnType;
			this.declarations = declarations;
			this.prefixInsns = prefixInsns;
			this.body = body;
			this.returnArg = returnArg;
		}

		public String getName() {
			return name;
		}

		public ArgType getReturnType() {
			return returnType;
		}

		public List<CodeVar> getDeclarations() {
			return declarations;
		}

		public List<InsnNode> getPrefixInsns() {
			return prefixInsns;
		}

		public IContainer getBody() {
			return body;
		}

		public InsnArg getReturnArg() {
			return returnArg;
		}
	}

	private final List<EnumField> fields;
	private List<EnumValueHelper> valueHelpers;
	private List<EnumRegionValueHelper> regionValueHelpers;
	private MethodNode staticMethod;

	public EnumClassAttr(List<EnumField> fields) {
		this.fields = fields;
	}

	public List<EnumField> getFields() {
		return fields;
	}

	public List<EnumValueHelper> getValueHelpers() {
		return valueHelpers;
	}

	public void setValueHelpers(List<EnumValueHelper> valueHelpers) {
		this.valueHelpers = valueHelpers;
	}

	public List<EnumRegionValueHelper> getRegionValueHelpers() {
		return regionValueHelpers;
	}

	public void setRegionValueHelpers(List<EnumRegionValueHelper> regionValueHelpers) {
		this.regionValueHelpers = regionValueHelpers;
	}

	public MethodNode getStaticMethod() {
		return staticMethod;
	}

	public void setStaticMethod(MethodNode staticMethod) {
		this.staticMethod = staticMethod;
	}

	@Override
	public AType<EnumClassAttr> getAttrType() {
		return AType.ENUM_CLASS;
	}

	@Override
	public String toString() {
		return "Enum fields: " + fields;
	}
}
