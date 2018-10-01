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
package net.officefloor.tutorial.threadaffinityhttpserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import net.officefloor.jdbc.test.DataSourceRule;
import net.officefloor.jpa.hibernate.HibernateJpaManagedObjectSource;
import net.officefloor.jpa.test.EntityManagerRule;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests the thread affinity.
 * 
 * @author Daniel Sagenschneider
 */
public class ThreadAffinityHttpServerTest {

	// START SNIPPET: tutorial
	@ClassRule
	public static DataSourceRule dataSource = new DataSourceRule("datasource.properties");

	@Rule
	public EntityManagerRule entityManager = new EntityManagerRule("entitymanager.properties",
			new HibernateJpaManagedObjectSource(), dataSource);

	@Rule
	public MockWoofServerRule server = new MockWoofServerRule();

	@Test
	public void sameThreadPoolDueToAffinity() throws Exception {

		// On multiple calls, should be same core (as locks affinity)
		String previousCore = null;
		for (int i = 0; i < 100; i++) {

			// GET entry
			MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/"));
			String html = response.getEntity(null);
			assertEquals("Should be successful: " + html, 200, response.getStatus().getStatusCode());

			// Parse out the core
			Pattern pattern = Pattern.compile(".*CORE-(\\d+)-.*", Pattern.DOTALL);
			Matcher matcher = pattern.matcher(html);
			assertTrue("Should be able to obtain thread affinity core", matcher.matches());
			String core = matcher.group(1);

			// Ensure same as previous core (ignoring first call)
			if (previousCore != null) {
				assertEquals("Should be locked to same core", previousCore, core);
			}

			// Set up for next call
			previousCore = core;
		}
	}
	// END SNIPPET: tutorial

}