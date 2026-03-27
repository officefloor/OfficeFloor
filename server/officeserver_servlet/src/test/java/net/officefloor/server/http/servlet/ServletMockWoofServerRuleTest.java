/*-
 * #%L
 * HttpServlet adapter for OfficeFloor HTTP Server
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

package net.officefloor.server.http.servlet;

import static org.junit.Assert.assertNotEquals;

import java.io.IOException;

import org.junit.runners.model.Statement;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.frame.impl.spi.team.ExecutorCachedTeamSource;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.web.build.HttpInput;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Ensure can integrate with {@link MockWoofServerRule}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletMockWoofServerRuleTest extends OfficeFrameTestCase {

	/**
	 * {@link Thread} used for servicing.
	 */
	private static volatile Thread serviceThread = null;

	/**
	 * Ensure can service with {@link MockWoofServerRule}.
	 */
	public void testRule() throws Throwable {
		try (MockWoofServerRule rule = new MockWoofServerRule((loadContext, compiler) -> {

			// Ensure can work with teams
			compiler.officeFloor((context) -> {
				OfficeFloorDeployer deployer = context.getOfficeFloorDeployer();
				deployer.enableAutoWireTeams();

				// Add team
				deployer.addTeam("TEAM", ExecutorCachedTeamSource.class.getName()).addTypeQualification(null,
						ServerHttpConnection.class.getName());
			});

			// Configure servicing
			loadContext.extend((woofContext) -> {
				OfficeArchitect office = woofContext.getOfficeArchitect();
				office.enableAutoWireTeams();

				// Add the section
				OfficeSection section = office.addOfficeSection("SECTION", ClassSectionSource.class.getName(),
						RuleSection.class.getName());

				// Configure handling
				HttpInput input = woofContext.getWebArchitect().getHttpInput(false, "GET", "/");
				office.link(input.getInput(), section.getOfficeSectionInput("service"));
			});
		})) {
			// Reset
			serviceThread = null;

			// Undertake the rule
			rule.apply(new Statement() {
				@Override
				public void evaluate() throws Throwable {

					// Ensure can service request
					MockHttpResponse response = rule.send(MockWoofServer.mockRequest());
					response.assertResponse(200, "TEST");
				}
			}, null).evaluate();
		}

		// Ensure run with different thread
		assertNotEquals("Should be different servicing thread", Thread.currentThread(), serviceThread);
	}

	public static class RuleSection {
		public void service(ServerHttpConnection connection) throws IOException {
			serviceThread = Thread.currentThread();
			connection.getResponse().getEntityWriter().write("TEST");
		}
	}

}
