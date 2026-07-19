package jadx.storage.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jadx.storage.api.SecurityFact;

final class TextArtifactAnalyzer {
	private static final String ANALYZER_ID = "jadx-storage-builtin-security";
	private static final String ANALYZER_VERSION = "1";
	private static final Pattern PACKAGE = Pattern.compile("^\\s*package\\s+([A-Za-z_$][\\w$]*(?:\\.[A-Za-z_$][\\w$]*)*)\\s*;");
	private static final Pattern TYPE = Pattern.compile("\\b(?:class|interface|enum|record)\\s+([A-Za-z_$][\\w$]*)");
	private static final Pattern URL = Pattern.compile("(?i)\\b(?:https?|wss?)://[^\\s\\\"'<>]+");
	private static final Pattern PERMISSION = Pattern.compile("android:name\\s*=\\s*\\\"([^\\\"]+)\\\"");

	private static final Rule[] RULES = {
			new Rule("DANGEROUS_API", "android.webkit.WebView.addJavascriptInterface", "addJavascriptInterface("),
			new Rule("WEBVIEW_SETTING", "android.webkit.WebSettings.setJavaScriptEnabled(true)", "setJavaScriptEnabled(true)"),
			new Rule("COMMAND_EXEC", "java.lang.Runtime.exec", "Runtime.getRuntime().exec("),
			new Rule("COMMAND_EXEC", "java.lang.ProcessBuilder", "new ProcessBuilder("),
			new Rule("TLS_OVERRIDE", "javax.net.ssl.HostnameVerifier", "HostnameVerifier"),
			new Rule("TLS_OVERRIDE", "javax.net.ssl.X509TrustManager", "X509TrustManager"),
			new Rule("CRYPTO_API", "javax.crypto.Cipher.getInstance", "Cipher.getInstance("),
			new Rule("CRYPTO_API", "java.security.MessageDigest.getInstance", "MessageDigest.getInstance("),
			new Rule("DYNAMIC_CODE", "dalvik.system.DexClassLoader", "DexClassLoader"),
			new Rule("NATIVE_LOAD", "java.lang.System.loadLibrary", "System.loadLibrary("),
	};

	private TextArtifactAnalyzer() {
	}

	static Analysis analyze(String path, String content) throws IOException {
		String packageName = "";
		String typeName = "";
		List<SecurityFact> facts = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new StringReader(content))) {
			String line;
			int lineNumber = 0;
			while ((line = reader.readLine()) != null) {
				lineNumber++;
				if (packageName.isEmpty()) {
					Matcher matcher = PACKAGE.matcher(line);
					if (matcher.find()) {
						packageName = matcher.group(1);
					}
				}
				if (typeName.isEmpty()) {
					Matcher matcher = TYPE.matcher(line);
					if (matcher.find()) {
						typeName = matcher.group(1);
					}
				}
				String detail = compactDetail(line);
				for (Rule rule : RULES) {
					if (line.contains(rule.needle)) {
						facts.add(new SecurityFact(rule.kind, rule.value, lineNumber, detail));
					}
				}
				Matcher urlMatcher = URL.matcher(line);
				while (urlMatcher.find()) {
					facts.add(new SecurityFact("NETWORK_ENDPOINT", urlMatcher.group(), lineNumber, detail));
				}
				if (path.endsWith("AndroidManifest.xml") && line.contains("uses-permission")) {
					Matcher permissionMatcher = PERMISSION.matcher(line);
					if (permissionMatcher.find()) {
						facts.add(new SecurityFact("ANDROID_PERMISSION", permissionMatcher.group(1), lineNumber, detail));
					}
				}
				if (path.endsWith("AndroidManifest.xml") && line.contains("android:exported=\"true\"")) {
					facts.add(new SecurityFact("EXPORTED_COMPONENT", "android:exported=true", lineNumber, detail));
				}
			}
		}
		String symbol;
		if (typeName.isEmpty()) {
			symbol = path;
		} else if (packageName.isEmpty()) {
			symbol = typeName;
		} else {
			symbol = packageName + '.' + typeName;
		}
		return new Analysis(symbol, facts);
	}

	static String getAnalyzerId() {
		return ANALYZER_ID;
	}

	static String getAnalyzerVersion() {
		return ANALYZER_VERSION;
	}

	private static String compactDetail(String line) {
		String value = line.trim().replaceAll("\\s+", " ");
		return value.length() <= 512 ? value : value.substring(0, 512);
	}

	static final class Analysis {
		private final String symbol;
		private final List<SecurityFact> facts;

		Analysis(String symbol, List<SecurityFact> facts) {
			this.symbol = symbol;
			this.facts = facts;
		}

		String getSymbol() {
			return symbol;
		}

		List<SecurityFact> getFacts() {
			return facts;
		}
	}

	private static final class Rule {
		private final String kind;
		private final String value;
		private final String needle;

		private Rule(String kind, String value, String needle) {
			this.kind = kind;
			this.value = value;
			this.needle = needle;
		}
	}
}
