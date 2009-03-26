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
package net.officefloor.frame.internal.structure;

/**
 * Linked List.
 * 
 * TODO change name to LinkedListSet and change to be unique entries.
 * 
 * @author Daniel
 */
public interface LinkedList<E extends LinkedListEntry<E, R>, R> {

	/**
	 * Obtains the head of this {@link LinkedList}.
	 * 
	 * @return Head {@link LinkedListEntry} of this {@link LinkedList}.
	 */
	E getHead();

	/**
	 * Specifies the head of this {@link LinkedList}.
	 * 
	 * @param head
	 *            Head of this {@link LinkedList}.
	 */
	void setHead(E head);

	/**
	 * Obtains the tail of this {@link LinkedList}.
	 * 
	 * @return Tail of this {@link LinkedList}.
	 */
	E getTail();

	/**
	 * Specifies the tail of this {@link LinkedList}.
	 * 
	 * @param tail
	 *            Tail of this {@link LinkedList}.
	 */
	void setTail(E tail);

	/**
	 * Adds a {@link LinkedListEntry} to this {@link LinkedList}.
	 * 
	 * @param entry
	 *            {@link LinkedListEntry} to be added to this {@link LinkedList}
	 *            .
	 */
	void addLinkedListEntry(E entry);

	/**
	 * Purges all {@link LinkedListEntry} instances within this
	 * {@link LinkedList}.
	 * 
	 * @param removeParameter
	 *            Parameter for {@link #lastLinkedListEntryRemoved(Object)}.
	 * @return Head of the {@link LinkedList} (before the purge) so that may
	 *         action the {@link LinkedListEntry} instances.
	 */
	E purgeLinkedList(R removeParameter);

	/**
	 * Creates a copy of this {@link LinkedList} returning the head of the copy.
	 * 
	 * @return Head {@link LinkedListItem} of the copy.
	 */
	LinkedListItem<E> copyLinkedList();

	/**
	 * Invoked when the last {@link LinkedListEntry} is removed from this
	 * {@link LinkedList}.
	 * 
	 * @param removeParameter
	 *            Parameter from the
	 *            {@link LinkedListEntry#removeFromLinkedList(Object)} causing
	 *            this to be invoked.
	 */
	void lastLinkedListEntryRemoved(R removeParameter);

}
