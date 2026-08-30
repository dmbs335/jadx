package jadx.tests.integration.others;

import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

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

	@Test
	public void testSyntheticFieldAddDoesNotInvertClassProcessingLocks() throws Exception {
		ClassNode cls = getClassNodeFromSmali();
		CountDownLatch classNodeLocked = new CountDownLatch(1);
		CountDownLatch classInfoLocked = new CountDownLatch(1);
		AtomicReference<Throwable> failure = new AtomicReference<>();

		Thread classThenInfo = daemonThread("class-then-info", failure, () -> {
			synchronized (cls) {
				classNodeLocked.countDown();
				await(classInfoLocked);
				synchronized (cls.getClassInfo()) {
					// Matches ClassNode.decompile() -> ProcessClass.process().
				}
			}
		});
		Thread infoThenField = daemonThread("info-then-field", failure, () -> {
			await(classNodeLocked);
			synchronized (cls.getClassInfo()) {
				classInfoLocked.countDown();
				addSyntheticField(cls, "concurrentSyntheticField");
			}
		});

		classThenInfo.start();
		infoThenField.start();
		classThenInfo.join(TimeUnit.SECONDS.toMillis(2));
		infoThenField.join(TimeUnit.SECONDS.toMillis(2));

		assertThat(classThenInfo.isAlive()).isFalse();
		assertThat(infoThenField.isAlive()).isFalse();
		assertThat(failure.get()).isNull();
		assertThat(cls.getFields())
				.extracting(FieldNode::getName)
				.contains("concurrentSyntheticField");
	}

	private static Thread daemonThread(String name, AtomicReference<Throwable> failure, Runnable action) {
		Thread thread = new Thread(() -> {
			try {
				action.run();
			} catch (Throwable th) {
				failure.compareAndSet(null, th);
			}
		}, name);
		thread.setDaemon(true);
		return thread;
	}

	private static void await(CountDownLatch latch) {
		try {
			if (!latch.await(2, TimeUnit.SECONDS)) {
				throw new AssertionError("Timed out waiting for lock-order test latch");
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new AssertionError("Lock-order test interrupted", e);
		}
	}

	private static FieldNode addSyntheticField(ClassNode cls, String name) {
		FieldInfo fieldInfo = FieldInfo.from(cls.root(), cls.getClassInfo(), name, ArgType.INT);
		FieldNode syntheticField = new FieldNode(cls, fieldInfo, 0);
		cls.addField(syntheticField);
		return syntheticField;
	}
}
