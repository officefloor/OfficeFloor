package net.officefloor.gef.configurer;

import java.util.List;
import java.util.function.Function;

/**
 * Builder of multiple models.
 * 
 * @author Daniel Sagenschneider
 */
public interface MultipleBuilder<M, I> extends InputBuilder<I>, Builder<M, List<I>, MultipleBuilder<M, I>> {

	/**
	 * Configures obtaining the label for the particular item.
	 * 
	 * @param getItemLabel
	 *            {@link Function} to obtain the label for a particular item.
	 */
	void itemLabel(Function<I, String> getItemLabel);

}