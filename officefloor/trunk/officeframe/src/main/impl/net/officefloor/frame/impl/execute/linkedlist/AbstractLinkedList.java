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
import net.officefloor.frame.internal.structure.LinkedListItem;

/**
 * Abstract {@link LinkedList}.
 * 
 * @author Daniel
 */
public abstract class AbstractLinkedList<E extends LinkedListEntry<E, R>, R>
		implements LinkedList<E, R> {

	/**
	 * Head of the {@link LinkedList}.
	 */
	private E head = null;

	/**
	 * Tail of the {@link LinkedList}.
	 */
	private E tail = null;

	/*
	 * ====================== LinkedList ====================================
	 */

	@Override
	public E getHead() {
		return this.head;
	}

	@Override
	public void setHead(E head) {
		this.head = head;
	}

	@Override
	public E getTail() {
		return this.tail;
	}

	@Override
	public void setTail(E tail) {
		this.tail = tail;
	}

	@Override
	public void addLinkedListEntry(E entry) {
	
		// TODO do not re-add entry to linked list
		
		// Append to end of linked list
		if (this.head == null) {
			// Empty linked list (first entry)
			this.head = entry;
		} else {
			this.tail.setNext(entry);
			entry.setPrev(this.tail);
		}
		this.tail = entry;
	}

	@Override
	public E purgeLinkedList(R removeParameter) {
	
		// Obtain the head of list
		E entry = this.head;

		// Purge the list
		this.head = null;
		this.tail = null;

		// Determine if last entry removed
		if (entry != null) {
			this.lastLinkedListEntryRemoved(removeParameter);
		}

		// Return the head of the list
		return entry;
	}

	@Override
	public LinkedListItem<E> copyLinkedList() {
		
		// Determine if have items in list
		if (this.head == null) {
			// No items
			return null;
		}

		// Copy the list (going backwards)
		E currentEntry = this.tail;
		LinkedListItem<E> currentItem = null;
		LinkedListItem<E> nextItem = null;
		while (currentEntry != null) {
			// Create item for entry
			currentItem = new LinkedListItemImpl<E, R>(currentEntry, nextItem);

			// Next iteration
			nextItem = currentItem;
			currentEntry = (E) currentEntry.getPrev();
		}

		// Return the copied list
		return currentItem;
	}

	/**
	 * Implementation of the {@link LinkedListItem}.
	 */
	private static class LinkedListItemImpl<E extends LinkedListEntry<E, R>, R>
			implements LinkedListItem<E> {

		/**
		 * {@link LinkedListEntry}.
		 */
		private final E entry;

		/**
		 * Next {@link LinkedListItem}.
		 */
		private final LinkedListItem<E> next;

		/**
		 * Initiate.
		 * 
		 * @param entry
		 *            {@link LinkedListEntry}.
		 * @param next
		 *            Next {@link LinkedListItem}.
		 */
		public LinkedListItemImpl(E entry, LinkedListItem<E> next) {
			this.entry = entry;
			this.next = next;
		}

		/*
		 * ====================== LinkedListItem =============================
		 */
		
		@Override
		public E getEntry() {
			return this.entry;
		}

		@Override
		public LinkedListItem<E> getNext() {
			return this.next;
		}
	}

}