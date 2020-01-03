package net.officefloor.gef.configurer.internal;

import java.util.function.Consumer;

/**
 * {@link ValueInput} allowing for optionally rendering following
 * {@link ValueInput} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface OptionalValueInput<M> extends ValueInput {

	/**
	 * Specifies the optional loader.
	 * 
	 * @param loader Loads the optional content.
	 */
	void setOptionalLoader(Consumer<ValueRendererFactory<M, ? extends ValueInput>[]> loader);

}