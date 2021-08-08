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

import junit.framework.TestCase;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.plugin.clazz.Dependency;

/**
 * Parent mock class for testing.
 * 
 * @author Daniel Sagenschneider
 */
public class ParentMockClass {

	/**
	 * {@link Connection}.
	 */
	private @Dependency Connection connection;

	/**
	 * Ensure can invoke {@link ProcessState}.
	 */
	private MockProcessInterface processes;

	/**
	 * Field not a dependency.
	 */
	protected String notDependency;

	/**
	 * Verifies the dependencies injected.
	 * 
	 * @param connection Expected {@link Connection}.
	 */
	public void verifyDependencyInjection(Connection connection) {
		// Verify dependency injection
		TestCase.assertEquals("Incorrect connection", connection, this.connection);
	}

	/**
	 * Verifies the processes injected.
	 * 
	 * @param processParameter Parameter for the invoked processes.
	 */
	public void verifyProcessInjection(Integer processParameter) {
		// Verify can invoke processes
		this.processes.doProcess();
		this.processes.parameterisedProcess(processParameter);
	}

}
