package net.officefloor.gef.configurer;

import java.util.function.Function;

/**
 * Generic builder.
 * 
 * @author Daniel Sagenschneider
 */
public interface Builder<M, V, B extends Builder<M, V, B>> {

	/**
	 * Configures obtaining the initial value.
	 * 
	 * @param getInitialValue
	 *            Obtains the initial value.
	 * @return <code>this</code>.
	 */
	B init(Function<M, V> getInitialValue);

	/**
	 * Validates the text value.
	 * 
	 * @param validator
	 *            {@link ValueValidator}.
	 * @return <code>this</code>.
	 */
	B validate(ValueValidator<M, V> validator);

	/**
	 * Specifies the {@link ValueLoader} to load the value to the model.
	 * 
	 * @param valueLoader
	 *            {@link ValueLoader} to load the value to the model.
	 * @return <code>this</code>.
	 */
	B setValue(ValueLoader<M, V> valueLoader);

}