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
package net.officefloor.compile.impl.util;

/**
 * <p>
 * Extracts a {@link String} value from the input object.
 * <p>
 * Typically the extracted {@link String} will be used as a comparable key in
 * sorting a list of objects.
 * 
 * @author Daniel Sagenschneider
 */
public interface StringExtractor<T> {

	/**
	 * Extracts the {@link String} from the {@link Object}.
	 * 
	 * @param object
	 *            {@link Object} to extract the {@link String} from.
	 * @return Extracted {@link String}.
	 */
	String toString(T object);
}