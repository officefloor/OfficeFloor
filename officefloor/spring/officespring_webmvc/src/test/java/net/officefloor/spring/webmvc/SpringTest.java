/*-
 * #%L
 * OfficeFloor integration of WAR
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

package net.officefloor.spring.webmvc;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.woof.compile.CompileWoof;
import net.officefloor.woof.mock.MockWoofServer;

/**
 * Ensure can provide {@link OfficeFloor} running of Spring App.
 * 
 * @author Daniel Sagenschneider
 */
public class SpringTest extends OfficeFrameTestCase {

	/**
	 * Ensure can service simple GET.
	 */
	public void testSimpleGet() throws Exception {
		this.doSpringTest("/simple", "Simple Spring");
	}

	/**
	 * Undertakes Spring test.
	 * 
	 * @param path           Path to Spring controller.
	 * @param expectedEntity Expected entity in response.
	 */
	private void doSpringTest(String path, String expectedEntity) throws Exception {

		// Undertake test
		CompileWoof compile = new CompileWoof(true);
		try (MockWoofServer server = compile.open()) {
			MockHttpResponse response = server.send(MockHttpServer.mockRequest(path));
			response.assertResponse(200, expectedEntity);
		}
	}

}