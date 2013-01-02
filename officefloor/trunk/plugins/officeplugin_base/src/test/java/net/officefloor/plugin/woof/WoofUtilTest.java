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
package net.officefloor.plugin.woof;

import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link WoofUtil}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofUtilTest extends OfficeFrameTestCase {

	/**
	 * Ensure appropriately reports if WoOF resource.
	 */
	public void testWoofResource() {

		// Valid WoOF resource
		assertTrue("By file name", WoofUtil.isWoofResource("test.woof.html"));
		assertTrue("By path",
				WoofUtil.isWoofResource("path/to/resource.woof.html"));
		assertTrue("By case insensitive",
				WoofUtil.isWoofResource("insensitive.WOOF.html"));
		assertTrue("By different file type",
				WoofUtil.isWoofResource("another.woof.txt"));

		// Not WoOF resource
		assertFalse("No woof part in name", WoofUtil.isWoofResource("test.txt"));
		assertFalse("Without extension", WoofUtil.isWoofResource("test.woof"));
		assertFalse("Without file name", WoofUtil.isWoofResource("woof.html"));
		assertFalse("Not in file name",
				WoofUtil.isWoofResource("not/resource.woof.dir/test.html"));
	}

}