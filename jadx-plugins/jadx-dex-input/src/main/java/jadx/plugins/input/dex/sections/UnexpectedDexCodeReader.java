package jadx.plugins.input.dex.sections;

import java.util.function.Consumer;

import jadx.api.plugins.input.insns.InsnData;
import jadx.plugins.input.dex.DexException;

/**
 * A code item on a native or abstract DEX method is invalid and must not be decompiled as
 * executable code.
 */
final class UnexpectedDexCodeReader extends DexCodeReader {
	private final String errorMessage;

	UnexpectedDexCodeReader(SectionReader in, int codeOffset, int methodId, String errorMessage) {
		super(in);
		setOffset(codeOffset);
		setMthId(methodId);
		this.errorMessage = errorMessage;
	}

	@Override
	public UnexpectedDexCodeReader copy() {
		return new UnexpectedDexCodeReader(super.copy(), getCodeOffset(), getMthId(), errorMessage);
	}

	@Override
	public void visitInstructions(Consumer<InsnData> insnConsumer) {
		throw new DexException(errorMessage);
	}
}
