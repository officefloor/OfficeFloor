/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
