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
package net.officefloor.frame.test;

import junit.framework.AssertionFailedError;

/**
 * Matches a list item.
 * 
 * @author Daniel Sagenschneider
 */
public interface ListItemMatcher<O> {

	/**
	 * Specifies whether the list item matches.
	 * 
	 * @param index
	 *            Index of item within the list.
	 * @param expected
	 *            Expected value.
	 * @param actual
	 *            Actual value.
	 * @throws AssertionError
	 *             If error in assertions.
	 * @throws AssertionFailedError
	 *             If fails assertion.
	 */
	void match(int index, O expected, O actual);
	
}
