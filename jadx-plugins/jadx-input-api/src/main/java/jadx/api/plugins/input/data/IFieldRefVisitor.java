package jadx.api.plugins.input.data;

/**
 * Receives one field reference without requiring the input plugin to materialize an
 * {@link IFieldRef} object.
 */
@FunctionalInterface
public interface IFieldRefVisitor {
	void accept(String parentClassType, String name, String type);
}
