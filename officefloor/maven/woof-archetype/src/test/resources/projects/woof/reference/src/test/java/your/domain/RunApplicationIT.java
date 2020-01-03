/*-
 * #%L
 * WoOF Archetype
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

package your.domain;

import static org.junit.Assert.assertEquals;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.Rule;
import org.junit.Test;

import net.officefloor.server.http.HttpClientRule;

/**
 * <p>
 * Integration tests the application.
 * <p>
 * TODO consider using Integration Test tools.
 */
public class RunApplicationIT {

	@Rule
	public HttpClientRule httpClient = new HttpClientRule();

	@Test
	public void ensureApplicationAvailable() throws Exception {

		// Connect to application and obtain page
		HttpGet get = new HttpGet("http://localhost:7878/hi/Integration");
		get.addHeader("accept", "application/json");
		HttpResponse response = this.httpClient.execute(get);

		// Ensure correct response
		assertEquals("Incorrect status", 200, response.getStatusLine().getStatusCode());
		assertEquals("Incorrect content type", "application/json", response.getFirstHeader("content-type").getValue());
		assertEquals("Incorrect response", "{\"message\":\"Hello Integration\"}",
				EntityUtils.toString(response.getEntity()));
	}

}
