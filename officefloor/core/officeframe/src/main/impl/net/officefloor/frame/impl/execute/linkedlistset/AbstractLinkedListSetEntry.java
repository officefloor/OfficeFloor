package net.officefloor.frame.impl.execute.linkedlistset;

import net.officefloor.frame.internal.structure.LinkedListSetEntry;

/**
 * Abstract {@link LinkedListSetEntry}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractLinkedListSetEntry<E extends LinkedListSetEntry<E, O>, O>
		implements LinkedListSetEntry<E, O> {

	/**
	 * Previous entry in the linked list.
	 */
	private E prev = null;

	/**
	 * Next entry in the linked list.
	 */
	private E next = null;

	/*
	 * =================== LinkedListEntry =================================
	 * 
	 * Methods are final to stop accidental overriding by other linking
	 * structures.
	 */

	@Override
	public final E getPrev() {
		return this.prev;
	}

	@Override
	public final void setPrev(E entry) {
		this.prev = entry;
	}

	@Override
	public final E getNext() {
		return this.next;
	}

	@Override
	public final void setNext(E entry) {
		this.next = entry;
	}

}