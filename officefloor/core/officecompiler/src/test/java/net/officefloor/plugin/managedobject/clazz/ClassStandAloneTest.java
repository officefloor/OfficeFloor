/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
