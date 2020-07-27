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
