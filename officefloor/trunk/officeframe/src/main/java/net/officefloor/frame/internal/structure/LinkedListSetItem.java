/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.frame.internal.structure;

/**
 * Item copied out of a {@link LinkedListSet}.
 * 
 * @author Daniel Sagenschneider
 */
public interface LinkedListSetItem<E> {

	/**
	 * Obtains the {@link LinkedListSetEntry} copied out of the
	 * {@link LinkedListSet}.
	 * 
	 * @return {@link LinkedListSetEntry} copied out of the
	 *         {@link LinkedListSet}.
	 */
	E getEntry();

	/**
	 * Obtains the next {@link LinkedListSetItem} copied out of the
	 * {@link LinkedListSet}.
	 * 
	 * @return Next {@link LinkedListSetItem} copied out of the
	 *         {@link LinkedListSet}.
	 */
	LinkedListSetItem<E> getNext();

}