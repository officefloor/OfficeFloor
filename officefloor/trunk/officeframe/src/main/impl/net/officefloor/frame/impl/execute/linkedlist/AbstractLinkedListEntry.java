/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.frame.impl.execute.linkedlist;

import net.officefloor.frame.internal.structure.LinkedList;
import net.officefloor.frame.internal.structure.LinkedListEntry;

/**
 * Abstract {@link LinkedListEntry}.
 * 
 * @author Daniel
 */
public class AbstractLinkedListEntry<E extends LinkedListEntry<E, R>, R>
		implements LinkedListEntry<E, R> {

	/**
	 * {@link LinkedList} that this is bound.
	 */
	private final LinkedList<E, R> linkedList;

	/**
	 * Previous entry in the linked list.
	 */
	private E prev = null;

	/**
	 * Next entry in the linked list.
	 */
	private E next = null;

	/**
	 * Initiate.
	 * 
	 * @param linkedList
	 *            {@link LinkedList} that this is bound.
	 */
	public AbstractLinkedListEntry(LinkedList<E, R> linkedList) {
		this.linkedList = linkedList;
	}

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

	@Override
	public final void removeFromLinkedList(R removeParameter) {
		
		// Flag indicating if last entry in linked list
		boolean isLast = true;

		// Determine if first entry
		if (this.prev == null) {
			// First entry
			this.linkedList.setHead(this.next);
		} else {
			// Middle entry
			this.prev.setNext(this.next);
			isLast = false;
		}

		// Determine if last entry
		if (this.next == null) {
			// Last entry
			this.linkedList.setTail(this.prev);
		} else {
			// Middle entry
			this.next.setPrev(this.prev);
			isLast = false;
		}

		// Flag if last entry
		if (isLast) {
			this.linkedList.lastLinkedListEntryRemoved(removeParameter);
		}
	}

}