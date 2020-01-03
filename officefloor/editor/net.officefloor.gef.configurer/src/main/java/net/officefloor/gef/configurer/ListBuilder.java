package net.officefloor.gef.configurer;

import java.util.List;
import java.util.function.Supplier;

/**
 * Builder of a list of models.
 * 
 * @author Daniel Sagenschneider
 */
public interface ListBuilder<M, I> extends ItemBuilder<I>, Builder<M, List<I>, ListBuilder<M, I>> {

	/**
	 * Provides means to add an item to the list.
	 * 
	 * @param itemFactory
	 *            {@link Supplier} for the new item.
	 * @return <code>this</code>.
	 */
	ListBuilder<M, I> addItem(Supplier<I> itemFactory);

	/**
	 * Allows for deleting an item from the list.
	 * 
	 * @return <code>this</code>.
	 */
	ListBuilder<M, I> deleteItem();

}