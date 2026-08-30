package jadx.storage.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class SourceSymbolExtractor {
	private static final Pattern PACKAGE = Pattern.compile("^\\s*package\\s+([A-Za-z_$][\\w$]*(?:\\.[A-Za-z_$][\\w$]*)*)\\s*;");
	private static final Pattern TYPE = Pattern.compile("\\b(?:class|interface|enum|record)\\s+([A-Za-z_$][\\w$]*)");

	private SourceSymbolExtractor() {
	}

	static String extract(String path, String content) throws IOException {
		String packageName = "";
		String typeName = "";
		try (BufferedReader reader = new BufferedReader(new StringReader(content))) {
			String line;
			while ((line = reader.readLine()) != null) {
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
				if (!packageName.isEmpty() && !typeName.isEmpty()) {
					break;
				}
			}
		}
		if (typeName.isEmpty()) {
			return path;
		}
		return packageName.isEmpty() ? typeName : packageName + '.' + typeName;
	}
}
