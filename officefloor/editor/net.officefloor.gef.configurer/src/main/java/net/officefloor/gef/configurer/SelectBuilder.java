package net.officefloor.gef.configurer;

import java.util.function.Function;

/**
 * Builder for selecting from a list.
 * 
 * @author Daniel Sagenschneider
 */
public interface SelectBuilder<M, I> extends Builder<M, I, SelectBuilder<M, I>> {

	/**
	 * <p>
	 * Configure obtaining label from item.
	 * <p>
	 * If not configured, will use {@link Object#toString()} of the item.
	 * 
	 * @param getLabel Function to obtain label from item.
	 * @return <code>this</code>.
	 */
	SelectBuilder<M, I> itemLabel(Function<I, String> getLabel);

}