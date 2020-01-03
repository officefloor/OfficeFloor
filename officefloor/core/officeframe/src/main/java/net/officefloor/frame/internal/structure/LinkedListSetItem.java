package net.officefloor.frame.internal.structure;

/**
 * Item copied out of a {@link LinkedListSet}.
 * 
 * @author Daniel Sagenschneider
 */
public interface LinkedListSetItem<I> {

	/**
	 * Obtains the {@link LinkedListSetEntry} copied out of the
	 * {@link LinkedListSet}.
	 * 
	 * @return {@link LinkedListSetEntry} copied out of the
	 *         {@link LinkedListSet}.
	 */
	I getEntry();

	/**
	 * Obtains the next {@link LinkedListSetItem} copied out of the
	 * {@link LinkedListSet}.
	 * 
	 * @return Next {@link LinkedListSetItem} copied out of the
	 *         {@link LinkedListSet}.
	 */
	LinkedListSetItem<I> getNext();

}