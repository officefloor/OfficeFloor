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

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorExecutive;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeamOversight;
import net.officefloor.frame.impl.spi.team.ExecutorFixedTeamSource;
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
			OfficeFloorDeployer deployer = context.getOfficeFloorDeployer();

			// Provide team for servicing
			deployer.enableAutoWireTeams();
			OfficeFloorTeam team = deployer.addTeam("TEAM", ExecutorFixedTeamSource.class.getName());
			team.setTeamSize(50);
			team.addTypeQualification(null, ServerHttpConnection.class.getName());

			// Configure thread affinity
			OfficeFloorExecutive executive = deployer.setExecutive(WebThreadAffinityExecutiveSource.class.getName());
			OfficeFloorTeamOversight oversight = executive.getOfficeFloorTeamOversight("THREAD_AFFINITY");
			deployer.addTeamAugmentor((teamAugment) -> teamAugment.setTeamOversight(oversight));
		});
	}

	/**
	 * Ensure function run with affinity.
	 */
	public void testAffinity() throws Exception {

		this.compile.web((context) -> {
			OfficeArchitect architect = context.getOfficeArchitect();

			// Configure team
			architect.enableAutoWireTeams();
			architect.addOfficeTeam("TEAM").addTypeQualification(null, ServerHttpConnection.class.getName());

			// Configure web handling
			context.link(false, "/path", EnsureThreadAffinity.class);
		});
		this.officeFloor = this.compile.compileAndOpenOfficeFloor();

		// Service request and capture affinity
		EnsureThreadAffinity.executingThread = null;
		EnsureThreadAffinity.affinity = null;
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/path"));
		response.assertResponse(200, "TEST");

		// Ensure have affinity
		assertNotNull("Should have executing thread", EnsureThreadAffinity.executingThread);
		assertNotSame("Should be executive with different thread", Thread.currentThread(),
				EnsureThreadAffinity.executingThread);
		assertNotNull("Should have affinity", EnsureThreadAffinity.affinity);

		// Ensure thread bound to only one core
		boolean isBoundToCore = false;
		for (CpuCore core : CpuCore.getCores()) {
			if (EnsureThreadAffinity.affinity.equals(core.getCoreAffinity())) {
				isBoundToCore = true;
			}
		}
		assertTrue("Executing thread should be bound to a core", isBoundToCore);
	}

	public static class EnsureThreadAffinity {

		private static volatile Thread executingThread;

		private static volatile BitSet affinity;

		public static void service(ServerHttpConnection connection) throws IOException {
			executingThread = Thread.currentThread();
			affinity = Affinity.getAffinity();
			connection.getResponse().getEntityWriter().write("TEST");
		}
	}

}