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

import org.junit.Rule;
import org.junit.Test;

import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.woof.mock.MockWoofServerRule;

public class RunApplicationTest {

	@Rule
	public MockWoofServerRule server = new MockWoofServerRule();

	@Test
	public void ensureApplicationAvailable() throws Exception {
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/hi/UnitTest"));
		response.assertResponse(200, "{\"message\":\"Hello UnitTest\"}", "content-type", "application/json");
	}
}
