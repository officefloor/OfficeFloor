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
package net.officefloor.frame.impl.execute.function;

import org.easymock.AbstractMatcher;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.function.NameAwareManagedFunctionFactory;
import net.officefloor.frame.api.function.OfficeAwareManagedFunctionFactory;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;

/**
 * Ensures the additional managed interface functionality is provided to the
 * {@link ManagedFunctionFactory} sub interfaces.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionFactoryTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure able to open {@link OfficeFloor} with just
	 * {@link ManagedFunctionFactory}.
	 */
	public void testManagedFunctionFactory() throws Exception {

		// Mock
		final ManagedFunctionFactory<?, ?> functionFactory = this.createMock(ManagedFunctionFactory.class);

		// Should not invoke additional functionality

		// Test
		this.replayMockObjects();
		this.constructFunction("task", functionFactory);
		OfficeFloor officeFloor = this.constructOfficeFloor();
		officeFloor.openOfficeFloor();

		// Verify functionality
		this.verifyMockObjects();
	}

	/**
	 * Ensure name aware functionality is provided for the
	 * {@link NameAwareManagedFunctionFactory}.
	 */
	public void testNameAwareManagedFunctionFactory() throws Exception {

		// Mock
		final String BOUND_NAME = "bound";
		final NameAwareManagedFunctionFactory<?, ?> functionFactory = this
				.createMock(NameAwareManagedFunctionFactory.class);

		// Record providing bound name
		functionFactory.setBoundFunctionName(BOUND_NAME);

		// Test
		this.replayMockObjects();
		this.constructFunction(BOUND_NAME, functionFactory);
		OfficeFloor officeFloor = this.constructOfficeFloor();
		officeFloor.openOfficeFloor();

		// Verify functionality
		this.verifyMockObjects();
	}

	/**
	 * Ensure {@link Office} aware functionality is provided for the
	 * {@link OfficeAwareManagedFunctionFactory}.
	 */
	public void testOfficeAwareManagedFunctionFactory() throws Exception {

		// Mock
		final OfficeAwareManagedFunctionFactory<?, ?> functionFactory = this
				.createMock(OfficeAwareManagedFunctionFactory.class);

		// Record providing Office
		functionFactory.setOffice(null);
		final Office[] office = new Office[1];
		this.control(functionFactory).setDefaultMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				office[0] = (Office) actual[0];
				return true;
			}
		});

		// Test
		this.replayMockObjects();
		this.constructFunction("task", functionFactory);
		OfficeFloor officeFloor = this.constructOfficeFloor();
		officeFloor.openOfficeFloor();

		// Verify functionality
		this.verifyMockObjects();

		// Ensure Office available
		assertNotNull("Ensure Office provided", office[0]);

		// Ensure correct Office (contains WorkFactory)
		assertEquals("Incorrect Office", "task", office[0].getFunctionNames()[0]);
	}

	/**
	 * Ensure both name and {@link Office} aware functionality is provided for
	 * the {@link MockNameAndOfficeAwareWorkFactory}.
	 */
	public void testNameAndOfficeAwareManagedFunctionFactory() throws Exception {

		// Mock
		final String BOUND_NAME = "task";
		final MockNameAndOfficeAwareManagedFunctionFactory functionFactory = this
				.createMock(MockNameAndOfficeAwareManagedFunctionFactory.class);

		// Record providing name and Office
		functionFactory.setBoundFunctionName(BOUND_NAME);
		functionFactory.setOffice(null);
		final Office[] office = new Office[1];
		this.control(functionFactory).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				office[0] = (Office) actual[0];
				return true;
			}
		});

		// Test
		this.replayMockObjects();
		this.constructFunction(BOUND_NAME, functionFactory);
		OfficeFloor officeFloor = this.constructOfficeFloor();
		officeFloor.openOfficeFloor();

		// Verify functionality
		this.verifyMockObjects();

		// Ensure Office available
		assertNotNull("Ensure Office provided", office[0]);

		// Ensure correct Office (contains WorkFactory)
		assertEquals("Incorrect Office", BOUND_NAME, office[0].getFunctionNames()[0]);
	}

	/**
	 * Interface for mock objects that implements both
	 * {@link NameAwareManagedFunctionFactory} and
	 * {@link OfficeAwareManagedFunctionFactory}.
	 */
	public static interface MockNameAndOfficeAwareManagedFunctionFactory
			extends NameAwareManagedFunctionFactory<None, None>, OfficeAwareManagedFunctionFactory<None, None> {
	}

}