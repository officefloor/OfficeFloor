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
package net.officefloor.plugin.ejb;

import java.util.Properties;

import javax.ejb.EJBException;
import javax.ejb.Remote;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.openejb.client.LocalInitialContextFactory;

import com.sun.jndi.url.mock.mockURLContextFactory;

import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link OfficeFloorEjb}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorEjbTest extends OfficeFrameTestCase {

	/**
	 * Ensure reports <code>officeFloorJndiName</code> configuration missing.
	 */
	public void testMissingOfficeFloorJndiName() throws Exception {
		this.doMissingConfigurationTest("MissingOfficeFloorJndiNameLocal", "officeFloorJndiName");
	}

	/**
	 * Ensure reports <code>officeName</code> configuration missing.
	 */
	public void testMissingOfficeName() throws Exception {
		this.doMissingConfigurationTest("MissingOfficeNameLocal", "officeName");
	}

	/**
	 * Ensure reports <code>workName</code> configuration missing.
	 */
	public void testMissingFunctionName() throws Exception {
		this.doMissingConfigurationTest("MissingFunctionNameLocal", "functionName");
	}

	/**
	 * Does the testing of missing configuration.
	 * 
	 * @param jndiName
	 *            jndi-name.
	 * @param envEntryName
	 *            env-entry-name.
	 */
	private void doMissingConfigurationTest(String jndiName, String envEntryName) throws Exception {
		try {
			EjbOrchestrator orchestrator = (EjbOrchestrator) this.lookup(jndiName);
			orchestrator.orchestrate(null);
		} catch (EJBException ex) {
			Throwable cause = ex.getCause();
			assertEquals("Incorrect missing configuration",
					"env-entry for name '" + envEntryName + "' must be provided", cause.getMessage());
		}
	}

	/**
	 * Tests invoking the {@link OfficeFloor} for orchestration.
	 */
	public void testOrchestrate() throws Exception {

		final Context context = this.createMock(Context.class);
		final OfficeFloor officeFloor = this.createMock(OfficeFloor.class);
		final Office office = this.createMock(Office.class);
		final FunctionManager functionManager = this.createMock(FunctionManager.class);
		final Object parameter = "PARAMETER";

		// Register the context
		mockURLContextFactory.reset();
		mockURLContextFactory.setContext(context);

		// Record
		this.recordReturn(context, context.lookup("mock:test"), officeFloor);
		this.recordReturn(officeFloor, officeFloor.getOffice("OFFICE"), office);
		this.recordReturn(office, office.getFunctionManager("SECTION.function"), functionManager);
		functionManager.invokeProcess(parameter, null);

		// Replay
		this.replayMockObjects();

		// Obtain the OfficeFloor EJB
		EjbOrchestrator orchestrator = (EjbOrchestrator) this.lookup("ConfiguredLocal");

		// Orchestrate the EJBs
		orchestrator.orchestrate(parameter);

		// Verify functionality
		this.verifyMockObjects();
	}

	/**
	 * Tests {@link Remote} invoking the {@link OfficeFloor} for orchestration.
	 */
	public void testOrchestrateRemotely() throws Exception {

		final Context context = this.createMock(Context.class);
		final OfficeFloor officeFloor = this.createMock(OfficeFloor.class);
		final Office office = this.createMock(Office.class);
		final FunctionManager functionManager = this.createMock(FunctionManager.class);
		final String parameter = "PARAMETER";

		// Register the context
		mockURLContextFactory.reset();
		mockURLContextFactory.setContext(context);

		// Record
		this.recordReturn(context, context.lookup("mock:test"), officeFloor);
		this.recordReturn(officeFloor, officeFloor.getOffice("OFFICE"), office);
		this.recordReturn(office, office.getFunctionManager("SECTION.function"), functionManager);
		functionManager.invokeProcess(parameter, null);

		// Replay
		this.replayMockObjects();

		// Obtain the OfficeFloor EJB
		EjbOrchestratorRemote orchestrator = (EjbOrchestratorRemote) this.lookup("ConfiguredRemote");

		// Orchestrate the EJBs
		String returnValue = orchestrator.orchestrateRemotely(parameter);

		// Ensure return value is parameter
		assertEquals("Return value should be the parameter", parameter, returnValue);

		// Verify functionality
		this.verifyMockObjects();
	}

	/**
	 * Looks up the JNDI Object.
	 * 
	 * @param jndiName
	 *            JNDI name of the Object.
	 * @return Looked up JNDI Object.
	 */
	private Object lookup(String jndiName) throws NamingException {

		// Create the initial context
		Properties properties = new Properties();
		properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());
		Context initialContext = new InitialContext(properties);

		// Lookup and return the value
		Object object = initialContext.lookup(jndiName);
		assertNotNull("Ensure have JNDI Object", object);
		return object;
	}

}