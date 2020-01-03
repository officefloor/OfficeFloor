/*-
 * #%L
 * Test HTTP Server Tutorial
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

package net.officefloor.tutorial.testhttpserver;

import static org.junit.Assert.assertTrue;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.junit.Rule;
import org.junit.Test;

import net.officefloor.server.http.HttpClientRule;

/**
 * Tests the {@link TemplateLogic}.
 * 
 * @author Daniel Sagenschneider
 */
public class TemplateLogicIT {

	// START SNIPPET: integration
	@Rule
	public HttpClientRule client = new HttpClientRule();

	@Test
	public void integrationTest() throws Exception {

		// Send request to add
		HttpPost request = new HttpPost("http://localhost:7878/template+add?a=1&b=2");
		HttpResponse response = client.execute(request);

		// Ensure added the values
		String entity = EntityUtils.toString(response.getEntity());
		assertTrue("Should have added the values: " + entity, entity.contains("= 3"));
	}
	// END SNIPPET: integration

}
