package jadx.api.plugins.input.data.impl;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;

import org.jetbrains.annotations.Nullable;

import jadx.api.plugins.input.ICodeLoader;
import jadx.api.plugins.input.data.IClassData;

public class MergeCodeLoader implements ICodeLoader {

	private final List<ICodeLoader> codeLoaders;
	private final @Nullable Closeable closeable;

	public MergeCodeLoader(List<ICodeLoader> codeLoaders) {
		this(codeLoaders, null);
	}

	public MergeCodeLoader(List<ICodeLoader> codeLoaders, @Nullable Closeable closeable) {
		this.codeLoaders = codeLoaders;
		this.closeable = closeable;
	}

	@Override
	public void visitClasses(Consumer<IClassData> consumer) {
		for (ICodeLoader codeLoader : codeLoaders) {
			codeLoader.visitClasses(consumer);
		}
	}

	@Override
	public int getClassesCount() {
		return sumCounts(ICodeLoader::getClassesCount);
	}

	@Override
	public int getMethodsCount() {
		return sumCounts(ICodeLoader::getMethodsCount);
	}

	@Override
	public int getFieldsCount() {
		return sumCounts(ICodeLoader::getFieldsCount);
	}

	@Override
	public int getTypesCount() {
		return sumCounts(ICodeLoader::getTypesCount);
	}

	private int sumCounts(ToIntFunction<ICodeLoader> countGetter) {
		long count = 0;
		for (ICodeLoader codeLoader : codeLoaders) {
			int loaderCount = countGetter.applyAsInt(codeLoader);
			if (loaderCount < 0) {
				return -1;
			}
			count += loaderCount;
			if (count > Integer.MAX_VALUE) {
				return -1;
			}
		}
		return (int) count;
	}

	@Override
	public boolean isEmpty() {
		for (ICodeLoader codeLoader : codeLoaders) {
			if (!codeLoader.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void close() throws IOException {
		for (ICodeLoader codeLoader : codeLoaders) {
			codeLoader.close();
		}
		if (closeable != null) {
			closeable.close();
		}
	}
}
