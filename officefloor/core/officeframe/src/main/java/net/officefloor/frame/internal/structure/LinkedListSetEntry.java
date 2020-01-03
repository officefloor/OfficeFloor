package net.officefloor.frame.internal.structure;

/**
 * Entry for a {@link LinkedListSet}.
 * 
 * @author Daniel Sagenschneider
 */
public interface LinkedListSetEntry<I extends LinkedListSetEntry<I, O>, O> {

	/**
	 * <p>
	 * Obtains the owner of the {@link LinkedListSet} that this
	 * {@link LinkedListSetEntry} may be added.
	 * <p>
	 * {@link LinkedListSetEntry} instances may only be added to the
	 * {@link LinkedListSet} they were intended for and can not be shared
	 * between {@link LinkedListSet} instances. This constraint:
	 * <ol>
	 * <li>ensures the integrity of the {@link FunctionState}, {@link Flow},
	 * {@link ThreadState}, {@link ProcessState} structure, and</li>
	 * <li>improves uniqueness performance as {@link #getNext()} and
	 * {@link #getPrev()} both returning <code>null</code> indicates not
	 * added</li>
	 * </ol>
	 * 
	 * @return Owner of the {@link LinkedListSet} that may contain this
	 *         {@link LinkedListSetEntry}.
	 */
	O getLinkedListSetOwner();

	/**
	 * <p>
	 * Obtains the previous {@link LinkedListSetEntry} in the
	 * {@link LinkedListSet}.
	 * <p>
	 * Should this {@link LinkedListSetEntry} not be in a {@link LinkedListSet}
	 * then this must return <code>null</code>.
	 * 
	 * @return Previous {@link LinkedListSetEntry} in the {@link LinkedListSet}.
	 */
	I getPrev();

	/**
	 * Specifies the previous {@link LinkedListSetEntry} in the
	 * {@link LinkedListSet}.
	 * 
	 * @param entry
	 *            Previous {@link LinkedListSetEntry} in the
	 *            {@link LinkedListSet}.
	 */
	void setPrev(I entry);

	/**
	 * <p>
	 * Obtains the next {@link LinkedListSetEntry} in the {@link LinkedListSet}.
	 * <p>
	 * Should this {@link LinkedListSetEntry} not be in a {@link LinkedListSet}
	 * then this must return <code>null</code>.
	 * 
	 * @return Next {@link LinkedListSetEntry} in the {@link LinkedListSet}.
	 */
	I getNext();

	/**
	 * Specifies the next {@link LinkedListSetEntry} in the
	 * {@link LinkedListSet}.
	 * 
	 * @param entry
	 *            Next {@link LinkedListSetEntry} in the {@link LinkedListSet}.
	 */
	void setNext(I entry);

}