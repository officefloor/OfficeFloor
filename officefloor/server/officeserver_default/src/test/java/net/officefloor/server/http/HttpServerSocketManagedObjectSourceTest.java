/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.server.http;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorInputManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.HttpClientTestUtil;
import net.officefloor.server.http.HttpServerSocketManagedObjectSource;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * Tests the {@link HttpServerSocketManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpServerSocketManagedObjectSourceTest extends OfficeFrameTestCase {

	private OfficeFloor officeFloor;

	@Override
	protected void tearDown() throws Exception {

		// Ensure close OfficeFloor
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}

		// Remaining tear down
		super.tearDown();
	}

	/**
	 * Ensure can configure and service request.
	 */
	public void testServiceRequest() throws Exception {

		// Compile the OfficeFloor to service request
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.officeFloor((extension) -> {

			// Obtain the OfficeFloor deployer
			OfficeFloorDeployer deployer = extension.getOfficeFloorDeployer();

			// Configure the HTTP managed object source
			OfficeFloorManagedObjectSource httpMos = deployer.addManagedObjectSource("HTTP",
					HttpServerSocketManagedObjectSource.class.getName());
			httpMos.addProperty(HttpServerSocketManagedObjectSource.PROPERTY_PORT, String.valueOf(7878));
			OfficeFloorInputManagedObject inputHttp = deployer.addInputManagedObject("HTTP",
					ServerHttpConnection.class.getName());
			inputHttp.addTypeQualification(null, ServerHttpConnection.class.getName());
			deployer.link(httpMos, inputHttp);

			// Configure input
			DeployedOffice office = extension.getDeployedOffice();
			deployer.link(httpMos.getManagingOffice(), office);
		});
		compile.office((extension) -> {
			extension.addSection("SECTION", MockSection.class);
		});
		this.officeFloor = compile.compileAndOpenOfficeFloor();

		// Ensure can get response
		try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient()) {
			HttpResponse response = client.execute(new HttpGet("http://localhost:7878"));
			assertEquals("Should be succesful", HttpStatus.OK.getStatusCode(),
					response.getStatusLine().getStatusCode());
			assertEquals("Incorrect content", "test", HttpClientTestUtil.getEntityBody(response));
		}
	}

	/**
	 * Mock section.
	 */
	public static class MockSection {

		public void service(ServerHttpConnection connection) throws IOException {
			connection.getHttpResponse().getEntityWriter().write("test");
		}
	}

}