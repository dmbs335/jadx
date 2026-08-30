package jadx.plugins.input.dex;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import jadx.api.plugins.options.impl.BasePluginOptionsBuilder;

public class DexInputOptions extends BasePluginOptionsBuilder {

	private boolean verifyChecksum;
	private Set<String> auditExcludedSha256 = Collections.emptySet();

	@Override
	public void registerOptions() {
		boolOption(DexInputPlugin.PLUGIN_ID + ".verify-checksum")
				.description("verify dex file checksum before load")
				.defaultValue(true)
				.setter(v -> verifyChecksum = v);
		strOption(DexInputPlugin.PLUGIN_ID + ".audit-excluded-sha256")
				.description("comma-separated SHA-256 fingerprints for explicitly audited embedded DEX exclusions")
				.defaultValue("")
				.setter(this::setAuditExcludedSha256);
	}

	public boolean isVerifyChecksum() {
		return verifyChecksum;
	}

	public boolean isAuditExcluded(String sha256) {
		return auditExcludedSha256.contains(sha256);
	}

	private void setAuditExcludedSha256(String value) {
		if (value == null || value.trim().isEmpty()) {
			auditExcludedSha256 = Collections.emptySet();
			return;
		}
		Set<String> hashes = new LinkedHashSet<>();
		for (String token : value.split("[,;\\s]+")) {
			String hash = token.trim().toLowerCase(Locale.ROOT);
			if (!hash.matches("[0-9a-f]{64}")) {
				throw new IllegalArgumentException("Invalid SHA-256 fingerprint: " + token);
			}
			hashes.add(hash);
		}
		auditExcludedSha256 = Collections.unmodifiableSet(hashes);
	}
}
