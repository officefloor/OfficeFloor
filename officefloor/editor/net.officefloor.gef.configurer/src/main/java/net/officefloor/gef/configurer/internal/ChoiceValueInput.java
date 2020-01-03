package net.officefloor.gef.configurer.internal;

import java.util.function.Supplier;

import javafx.beans.property.ReadOnlyProperty;

/**
 * {@link ValueInput} allowing for rendering choice of following
 * {@link ValueInput} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface ChoiceValueInput<M> extends ValueInput {

	/**
	 * Obtains the array of {@link ValueRendererFactory} instances.
	 * 
	 * @return Array of {@link ValueRendererFactory} instances.
	 */
	Supplier<ValueRendererFactory<M, ? extends ValueInput>[]>[] getChoiceValueRendererFactories();

	/**
	 * Obtains the index into choice {@link ValueRenderer} listing to display.
	 * 
	 * @return Index into choice {@link ValueRenderer} listing to display.
	 */
	ReadOnlyProperty<Integer> getChoiceIndex();

}