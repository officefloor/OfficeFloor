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
 * {@link LinkedListEntry} that throws {@link IllegalStateException} on
 * attempting to link into a {@link LinkedList}.
 * 
 * @author Daniel
 */
// TODO have all LinkedListEntries really be linked together.
@Deprecated
public class UnlinkableLinkedListEntry<E extends LinkedListEntry<E, R>, R>
		implements LinkedListEntry<E, R> {

	/*
	 * =================== LinkedListEntry ================================
	 */

	@Override
	public E getNext() {
		throw new IllegalStateException("Should not be linking a "
				+ this.getClass().getSimpleName() + " into a "
				+ LinkedList.class.getSimpleName());
	}

	@Override
	public void setNext(E entry) {
		throw new IllegalStateException("Should not be linking a "
				+ this.getClass().getSimpleName() + " into a "
				+ LinkedList.class.getSimpleName());
	}

	@Override
	public E getPrev() {
		throw new IllegalStateException("Should not be linking a "
				+ this.getClass().getSimpleName() + " into a "
				+ LinkedList.class.getSimpleName());
	}

	@Override
	public void setPrev(E entry) {
		throw new IllegalStateException("Should not be linking a "
				+ this.getClass().getSimpleName() + " into a "
				+ LinkedList.class.getSimpleName());
	}

	@Override
	public void removeFromLinkedList(R lastRemovedParameter) {
		throw new IllegalStateException("Should not be linking a "
				+ this.getClass().getSimpleName() + " into a "
				+ LinkedList.class.getSimpleName());
	}

}