/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

package net.officefloor.example.weborchestration;

import javax.naming.InitialContext;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.test.issues.FailTestCompilerIssues;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.protocol.HttpStatus;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.openejb.OpenEJB;

/**
 * Ensure integration of {@link OfficeFloor} with {@link OpenEJB}.
 * 
 * @author Daniel Sagenschneider
 */
public class WebOfficeFloorTest extends OfficeFrameTestCase {

	@Override
	protected void setUp() throws Exception {
		// Provide properties for running OpenEJB
		System.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY,
				"org.apache.openejb.client.LocalInitialContextFactory");
		System.setProperty("openejb.deployments.classpath.include",
				".*WebOrchestration.*");
	}

	@Override
	protected void tearDown() throws Exception {
		// Clear properties
		System.clearProperty(InitialContext.INITIAL_CONTEXT_FACTORY);
		System.clearProperty("openejb.deployments.classpath.include");
	}

	/**
	 * Ensure able to initialise the EJBs.
	 */
	public void testInitialiseEjbs() throws Exception {

		// Ensure can setup
		TestResetLocal setup = WebUtil.lookupService(TestResetLocal.class);
		setup.reset();

		// Validate setting up Customer
		Customer customer = setup.setupCustomer();
		assertNotNull("Must be able to setup the customer", customer);
	}

	/**
	 * Ensure able to open the {@link OfficeFloor} and obtain initial state
	 * page.
	 */
	public void testOfficeFloor() throws Throwable {

		// Obtain location of OfficeFloor configuration
		String officeFloorConfiguration = this.getPackageRelativePath(this
				.getClass())
				+ "/WebOfficeFloor.officefloor";

		// Create client to call on the OfficeFloor
		HttpClient client = new HttpClient();
		GetMethod method = null;

		// Open the OfficeFloor
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler();
		compiler.addSystemProperties();
		compiler.setCompilerIssues(new FailTestCompilerIssues());
		OfficeFloor officeFloor = compiler.compile(officeFloorConfiguration);
		officeFloor.openOfficeFloor();
		try {			
			// Connect to OfficeFloor instance
			method = new GetMethod("http://localhost:18989/initialState.task");

			// Ensure able to obtain initial state information
			int status = client.executeMethod(method);
			assertEquals("Ensure successful request", HttpStatus.SC_OK, status);

		} finally {
			// Ensure release connection
			if (method != null) {
				method.releaseConnection();
			}

			// Ensure close OfficeFloor
			officeFloor.closeOfficeFloor();
		}
	}

}