package jadx.tests.integration.others.subclassfield;

import java.util.function.IntSupplier;

public class SubclassFieldLambdaReader {
	public static int read(SubclassFieldCrossClassLambda target) {
		IntSupplier reader = () -> target.initial;
		return reader.getAsInt();
	}
}
