/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.web.executive;

import java.io.IOException;
import java.util.BitSet;

import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.compile.AbstractWebCompileTestCase;
import net.openhft.affinity.Affinity;

/**
 * Tests the {@link WebThreadAffinityExecutiveSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class WebThreadAffinityExecutiveSourceTest extends AbstractWebCompileTestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Provide web thread affinity
		this.compile.officeFloor((context) -> {
			fail("TODO provide executive via compiler");
		});
	}

	/**
	 * Ensure function run with affinity.
	 */
	public void testAffinity() throws Exception {
		this.compile.web((context) -> {
			context.link(false, "/path", EnsureThreadAffinity.class);
		});
		this.officeFloor = this.compile.compileAndOpenOfficeFloor();

		// Service request and capture affinity
		EnsureThreadAffinity.affinity = null;
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/path"));
		response.assertResponse(200, "TEST");

		// Ensure have affinity
		assertNotNull("Should have affinity", EnsureThreadAffinity.affinity);
	}

	public static class EnsureThreadAffinity {

		private static volatile BitSet affinity;

		public static void service(ServerHttpConnection connection) throws IOException {
			affinity = Affinity.getAffinity();
			connection.getResponse().getEntityWriter().write("TEST");
		}
	}

}