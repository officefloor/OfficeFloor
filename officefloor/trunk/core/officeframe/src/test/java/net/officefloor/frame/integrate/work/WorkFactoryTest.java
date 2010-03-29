/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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
package net.officefloor.frame.integrate.work;

import net.officefloor.frame.api.build.NameAwareWorkFactory;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;

/**
 * Ensures the additional managed interface functionality is provided to the
 * {@link WorkFactory} sub interfaces.
 * 
 * @author Daniel Sagenschneider
 */
public class WorkFactoryTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure able to open {@link OfficeFloor} with just {@link WorkFactory}.
	 */
	public void testWorkFactory() throws Exception {

		// Mock
		final WorkFactory<?> workFactory = this.createMock(WorkFactory.class);

		// Should not invoke additional functionality

		// Test
		this.replayMockObjects();
		this.constructWork("WORK", workFactory);
		OfficeFloor officeFloor = this.constructOfficeFloor();
		officeFloor.openOfficeFloor();

		// Verify functionality
		this.verifyMockObjects();
	}

	/**
	 * Ensure name aware functionality is provided for the
	 * {@link NameAwareWorkFactory}.
	 */
	public void testNameAwareWorkFactory() throws Exception {

		// Mock
		final String WORK_NAME = "WORK";
		final NameAwareWorkFactory<?> workFactory = this
				.createMock(NameAwareWorkFactory.class);

		// Record providing bound work name
		workFactory.setBoundWorkName(WORK_NAME);

		// Test
		this.replayMockObjects();
		this.constructWork(WORK_NAME, workFactory);
		OfficeFloor officeFloor = this.constructOfficeFloor();
		officeFloor.openOfficeFloor();

		// Verify functionality
		this.verifyMockObjects();
	}

}