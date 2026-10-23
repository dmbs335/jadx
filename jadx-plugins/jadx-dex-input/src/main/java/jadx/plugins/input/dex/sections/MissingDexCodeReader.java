package jadx.plugins.input.dex.sections;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import jadx.api.plugins.input.data.IDebugInfo;
import jadx.api.plugins.input.data.ITry;
import jadx.api.plugins.input.insns.InsnData;
import jadx.plugins.input.dex.DexException;

/**
 * Preserve a malformed concrete method with code_off == 0 so it produces a method-level error
 * instead of being rendered as a declaration without a body.
 */
final class MissingDexCodeReader extends DexCodeReader {
	MissingDexCodeReader(SectionReader in) {
		super(in);
	}

	@Override
	public MissingDexCodeReader copy() {
		return new MissingDexCodeReader(super.copy());
	}

	@Override
	public int getRegistersCount() {
		return 0;
	}

	@Override
	public int getArgsStartReg() {
		return 0;
	}

	@Override
	public int getUnitsCount() {
		return 0;
	}

	@Override
	public void visitInstructions(Consumer<InsnData> insnConsumer) {
		throw new DexException("Concrete DEX method has no code item");
	}

	@Override
	public @Nullable IDebugInfo getDebugInfo() {
		return null;
	}

	@Override
	public int getCodeOffset() {
		return 0;
	}

	@Override
	public List<ITry> getTries() {
		return Collections.emptyList();
	}
}
