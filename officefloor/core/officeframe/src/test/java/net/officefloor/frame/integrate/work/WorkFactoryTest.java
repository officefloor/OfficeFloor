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
package net.officefloor.frame.integrate.work;

import net.officefloor.frame.api.build.NameAwareManagedFunctionFactory;
import net.officefloor.frame.api.build.OfficeAwareManagedFunctionFactory;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;

import org.easymock.AbstractMatcher;

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
	 * {@link NameAwareManagedFunctionFactory}.
	 */
	public void testNameAwareWorkFactory() throws Exception {

		// Mock
		final String WORK_NAME = "WORK";
		final NameAwareManagedFunctionFactory<?> workFactory = this
				.createMock(NameAwareManagedFunctionFactory.class);

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

	/**
	 * Ensure {@link Office} aware functionality is provided for the
	 * {@link OfficeAwareManagedFunctionFactory}.
	 */
	public void testOfficeAwareWorkFactory() throws Exception {

		// Mock
		final String WORK_NAME = "WORK";
		final OfficeAwareManagedFunctionFactory<?> workFactory = this
				.createMock(OfficeAwareManagedFunctionFactory.class);

		// Record providing Office
		workFactory.setOffice(null);
		final Office[] office = new Office[1];
		this.control(workFactory).setDefaultMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				office[0] = (Office) actual[0];
				return true;
			}
		});

		// Test
		this.replayMockObjects();
		this.constructWork(WORK_NAME, workFactory);
		OfficeFloor officeFloor = this.constructOfficeFloor();
		officeFloor.openOfficeFloor();

		// Verify functionality
		this.verifyMockObjects();

		// Ensure Office available
		assertNotNull("Ensure Office provided", office[0]);

		// Ensure correct Office (contains WorkFactory)
		assertEquals("Incorrect Office", WORK_NAME, office[0].getWorkNames()[0]);
	}

	/**
	 * Ensure both name and {@link Office} aware functionality is provided for
	 * the {@link MockNameAndOfficeAwareWorkFactory}.
	 */
	public void testNameAndOfficeAwareWorkFactory() throws Exception {

		// Mock
		final String WORK_NAME = "WORK";
		final MockNameAndOfficeAwareWorkFactory workFactory = this
				.createMock(MockNameAndOfficeAwareWorkFactory.class);

		// Record providing name and Office
		workFactory.setBoundWorkName(WORK_NAME);
		workFactory.setOffice(null);
		final Office[] office = new Office[1];
		this.control(workFactory).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				office[0] = (Office) actual[0];
				return true;
			}
		});

		// Test
		this.replayMockObjects();
		this.constructWork(WORK_NAME, workFactory);
		OfficeFloor officeFloor = this.constructOfficeFloor();
		officeFloor.openOfficeFloor();

		// Verify functionality
		this.verifyMockObjects();

		// Ensure Office available
		assertNotNull("Ensure Office provided", office[0]);

		// Ensure correct Office (contains WorkFactory)
		assertEquals("Incorrect Office", WORK_NAME, office[0].getWorkNames()[0]);
	}

	/**
	 * Interface for mock objects that implements both
	 * {@link NameAwareManagedFunctionFactory} and {@link OfficeAwareManagedFunctionFactory}.
	 */
	public static interface MockNameAndOfficeAwareWorkFactory extends
			NameAwareManagedFunctionFactory<Work>, OfficeAwareManagedFunctionFactory<Work> {
	}

}