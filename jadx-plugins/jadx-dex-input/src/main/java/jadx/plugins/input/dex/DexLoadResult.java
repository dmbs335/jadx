package jadx.plugins.input.dex;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;

import org.jetbrains.annotations.Nullable;

import jadx.api.plugins.input.ICodeLoader;
import jadx.api.plugins.input.data.IClassData;

public class DexLoadResult implements ICodeLoader {
	private final List<DexReader> dexReaders;
	@Nullable
	private final Closeable closeable;

	public DexLoadResult(List<DexReader> dexReaders, @Nullable Closeable closeable) {
		this.dexReaders = dexReaders;
		this.closeable = closeable;
	}

	@Override
	public void visitClasses(Consumer<IClassData> consumer) {
		for (DexReader dexReader : dexReaders) {
			dexReader.visitClasses(consumer);
		}
	}

	@Override
	public int getClassesCount() {
		return sumCounts(DexReader::getClassesCount);
	}

	@Override
	public int getMethodsCount() {
		return sumCounts(DexReader::getMethodsCount);
	}

	@Override
	public int getFieldsCount() {
		return sumCounts(DexReader::getFieldsCount);
	}

	@Override
	public int getTypesCount() {
		return sumCounts(DexReader::getTypesCount);
	}

	private int sumCounts(ToIntFunction<DexReader> countGetter) {
		long count = 0;
		for (DexReader dexReader : dexReaders) {
			count += countGetter.applyAsInt(dexReader);
			if (count > Integer.MAX_VALUE) {
				return -1;
			}
		}
		return (int) count;
	}

	@Override
	public void close() throws IOException {
		if (closeable != null) {
			closeable.close();
		}
	}

	@Override
	public boolean isEmpty() {
		return dexReaders.isEmpty();
	}
}
