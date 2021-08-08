/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.frame.impl.execute.function;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.function.NameAwareManagedFunctionFactory;
import net.officefloor.frame.api.function.OfficeAwareManagedFunctionFactory;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ParameterCapture;

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
		ParameterCapture<Office> office = new ParameterCapture<>();
		functionFactory.setOffice(office.capture());

		// Test
		this.replayMockObjects();
		this.constructFunction("task", functionFactory);
		OfficeFloor officeFloor = this.constructOfficeFloor();
		officeFloor.openOfficeFloor();

		// Verify functionality
		this.verifyMockObjects();

		// Ensure Office available
		assertNotNull("Ensure Office provided", office.getValue());

		// Ensure correct Office (contains WorkFactory)
		assertEquals("Incorrect Office", "task", office.getValue().getFunctionNames()[0]);
	}

	/**
	 * Ensure both name and {@link Office} aware functionality is provided for the
	 * {@link MockNameAndOfficeAwareWorkFactory}.
	 */
	public void testNameAndOfficeAwareManagedFunctionFactory() throws Exception {

		// Mock
		final String BOUND_NAME = "task";
		final MockNameAndOfficeAwareManagedFunctionFactory functionFactory = this
				.createMock(MockNameAndOfficeAwareManagedFunctionFactory.class);

		// Record providing name
		functionFactory.setBoundFunctionName(BOUND_NAME);

		// Record providing Office
		ParameterCapture<Office> office = new ParameterCapture<>();
		functionFactory.setOffice(office.capture());

		// Test
		this.replayMockObjects();
		this.constructFunction(BOUND_NAME, functionFactory);
		OfficeFloor officeFloor = this.constructOfficeFloor();
		officeFloor.openOfficeFloor();

		// Verify functionality
		this.verifyMockObjects();

		// Ensure Office available
		assertNotNull("Ensure Office provided", office.getValue());

		// Ensure correct Office (contains WorkFactory)
		assertEquals("Incorrect Office", BOUND_NAME, office.getValue().getFunctionNames()[0]);
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
