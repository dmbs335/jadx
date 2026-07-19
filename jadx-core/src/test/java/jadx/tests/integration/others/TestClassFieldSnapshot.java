package jadx.tests.integration.others;

import java.util.Iterator;

import org.junit.jupiter.api.Test;

import jadx.core.dex.info.FieldInfo;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.nodes.ClassNode;
import jadx.core.dex.nodes.FieldNode;
import jadx.tests.api.SmaliTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

public class TestClassFieldSnapshot extends SmaliTest {

	@Test
	public void testIteratorRemainsStableAfterSyntheticFieldAdd() {
		ClassNode cls = getClassNodeFromSmali();
		Iterator<FieldNode> originalFieldsSnapshot = cls.getFields().iterator();

		FieldNode firstSyntheticField = addSyntheticField(cls, "firstSyntheticField");
		Iterator<FieldNode> appendedFieldsSnapshot = cls.getFields().iterator();
		FieldNode secondSyntheticField = addSyntheticField(cls, "secondSyntheticField");

		assertThatCode(() -> originalFieldsSnapshot.forEachRemaining(field -> {
		})).doesNotThrowAnyException();
		assertThatCode(() -> appendedFieldsSnapshot.forEachRemaining(field -> {
		})).doesNotThrowAnyException();
		assertThat(cls.searchField(firstSyntheticField.getFieldInfo())).isSameAs(firstSyntheticField);
		assertThat(cls.searchField(secondSyntheticField.getFieldInfo())).isSameAs(secondSyntheticField);
	}

	private static FieldNode addSyntheticField(ClassNode cls, String name) {
		FieldInfo fieldInfo = FieldInfo.from(cls.root(), cls.getClassInfo(), name, ArgType.INT);
		FieldNode syntheticField = new FieldNode(cls, fieldInfo, 0);
		cls.addField(syntheticField);
		return syntheticField;
	}
}
