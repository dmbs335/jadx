package jadx.api.plugins.input;

import java.io.Closeable;
import java.util.function.Consumer;

import jadx.api.plugins.input.data.IClassData;

public interface ICodeLoader extends Closeable {

	void visitClasses(Consumer<IClassData> consumer);

	/**
	 * Return the exact number of classes this loader will visit, or {@code -1} if unknown.
	 * The count lets consumers size transient indexes without growing and rehashing them.
	 */
	default int getClassesCount() {
		return -1;
	}

	/** Return the number of method references this loader can expose, or {@code -1}. */
	default int getMethodsCount() {
		return -1;
	}

	/** Return the number of field references this loader can expose, or {@code -1}. */
	default int getFieldsCount() {
		return -1;
	}

	/** Return the number of type references this loader can expose, or {@code -1}. */
	default int getTypesCount() {
		return -1;
	}

	boolean isEmpty();
}
