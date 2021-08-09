/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.plugin.managedobject.clazz;

import java.sql.Connection;

import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link ClassStandAlone}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassStandAloneTest extends OfficeFrameTestCase {

	/**
	 * Ensure able to instantiate a new instances for unit testing.
	 */
	public void testCreate() throws Throwable {

		final Connection connection = this.createMock(Connection.class);
		final String QUALIFIED_DEPENDENCY = "SELECT NAME FROM QUALIFIED";
		final String UNQUALIFIED_DEPENDENCY = "SELECT * FROM UNQUALIFIED";
		final Integer PROCESS_PARAMETER = Integer.valueOf(200);
		Closure<Boolean> doProcessInvoked = new Closure<>();
		Closure<Object> parameterisedProcess = new Closure<>();

		// Replay mock objects
		this.replayMockObjects();

		// Create the instance
		ClassStandAlone standAlone = new ClassStandAlone();
		standAlone.registerDependency(UNQUALIFIED_DEPENDENCY);
		standAlone.registerDependency(MockQualifier.class.getName(), QUALIFIED_DEPENDENCY);
		standAlone.registerDependency(Connection.class, connection);
		standAlone.registerFlow("doProcess", (index, parameter, mo) -> doProcessInvoked.value = true);
		standAlone.registerFlow("parameterisedProcess",
				(index, parameter, mo) -> parameterisedProcess.value = parameter);
		MockClass mockClass = standAlone.create(MockClass.class);

		// Verify the dependencies injected
		mockClass.verifyDependencyInjection(UNQUALIFIED_DEPENDENCY, QUALIFIED_DEPENDENCY, UNQUALIFIED_DEPENDENCY,
				QUALIFIED_DEPENDENCY, UNQUALIFIED_DEPENDENCY, QUALIFIED_DEPENDENCY, mockClass.getLogger(), connection,
				UNQUALIFIED_DEPENDENCY, QUALIFIED_DEPENDENCY);

		// Verify the process interfaces injected
		mockClass.verifyProcessInjection(PROCESS_PARAMETER);
		assertTrue("Should invoke doProcess", doProcessInvoked.value);
		assertSame("Incorrect parameter in process", PROCESS_PARAMETER, parameterisedProcess.value);

		// Verify mock objects
		this.verifyMockObjects();
	}

}
