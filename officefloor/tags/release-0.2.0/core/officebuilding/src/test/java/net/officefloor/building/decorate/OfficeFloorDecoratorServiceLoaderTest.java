/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.building.decorate;

import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link OfficeFloorDecoratorServiceLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorDecoratorServiceLoaderTest extends OfficeFrameTestCase {

	/**
	 * Ensure can load an {@link OfficeFloorDecorator} and that duplicates are
	 * not loaded.
	 */
	public void testLoadUniqueOfficeFloorDecorators() {

		// Load the decorators
		OfficeFloorDecorator[] decorators = OfficeFloorDecoratorServiceLoader
				.loadOfficeFloorDecorators(null);

		// Ensure unique and correct decorator
		assertEquals(
				"Expecting only 1 decorator (duplicate should not be loaded)",
				1, decorators.length);
		assertEquals("Incorrect type of decorator",
				MockOfficeFloorDecorator.class, decorators[0].getClass());
	}

}