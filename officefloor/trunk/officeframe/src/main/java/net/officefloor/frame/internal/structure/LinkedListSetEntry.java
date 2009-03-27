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
 * Entry for a {@link LinkedListSet}.
 * 
 * @author Daniel
 */
public interface LinkedListSetEntry<E extends LinkedListSetEntry<E, O>, O> {

	/**
	 * <p>
	 * Obtains the owner of the {@link LinkedListSet} that this
	 * {@link LinkedListSetEntry} may be added.
	 * <p>
	 * {@link LinkedListSetEntry} instances may only be added to the
	 * {@link LinkedListSet} they were intended for and can not be shared
	 * between {@link LinkedListSet} instances. This constraint:
	 * <ol>
	 * <li>ensures the integrity of the {@link JobNode}, {@link Flow},
	 * {@link ThreadState}, {@link ProcessState} structure, and</li>
	 * <li>improves uniqueness performance as {@link #getNext()} and
	 * {@link #getPrev()} both returning <code>null</code> indicates not added</li>
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
	E getPrev();

	/**
	 * Specifies the previous {@link LinkedListSetEntry} in the
	 * {@link LinkedListSet}.
	 * 
	 * @param entry
	 *            Previous {@link LinkedListSetEntry} in the
	 *            {@link LinkedListSet}.
	 */
	void setPrev(E entry);

	/**
	 * <p>
	 * Obtains the next {@link LinkedListSetEntry} in the {@link LinkedListSet}.
	 * <p>
	 * Should this {@link LinkedListSetEntry} not be in a {@link LinkedListSet}
	 * then this must return <code>null</code>.
	 * 
	 * @return Next {@link LinkedListSetEntry} in the {@link LinkedListSet}.
	 */
	E getNext();

	/**
	 * Specifies the next {@link LinkedListSetEntry} in the
	 * {@link LinkedListSet}.
	 * 
	 * @param entry
	 *            Next {@link LinkedListSetEntry} in the {@link LinkedListSet}.
	 */
	void setNext(E entry);

}