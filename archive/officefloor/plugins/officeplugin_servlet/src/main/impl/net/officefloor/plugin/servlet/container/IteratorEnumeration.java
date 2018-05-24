/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.servlet.container;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * {@link Enumeration} wrapping an {@link Iterator}.
 * 
 * @author Daniel Sagenschneider
 */
public class IteratorEnumeration<E> implements Enumeration<E> {

	/**
	 * {@link Iterator}.
	 */
	private final Iterator<E> iterator;

	/**
	 * Initiate.
	 * 
	 * @param iterator
	 *            {@link Iterator}.
	 */
	public IteratorEnumeration(Iterator<E> iterator) {
		this.iterator = iterator;
	}

	/*
	 * ==================== Enumeration ======================
	 */

	@Override
	public boolean hasMoreElements() {
		return this.iterator.hasNext();
	}

	@Override
	public E nextElement() {
		return this.iterator.next();
	}
}