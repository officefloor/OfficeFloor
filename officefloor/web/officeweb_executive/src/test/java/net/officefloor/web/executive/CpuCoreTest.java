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

import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.web.executive.CpuCore.LogicalCpu;
import net.openhft.affinity.AffinityLock;
import net.openhft.affinity.CpuLayout;

/**
 * Test the {@link CpuCore}.
 * 
 * @author Daniel Sagenschneider
 */
public class CpuCoreTest extends OfficeFrameTestCase {

	/**
	 * Ensure can configure from default {@link CpuLayout}.
	 */
	public void testDefaultCpuLayout() {

		// Obtain the default CPU layout
		CpuCore[] cores = CpuCore.getCores();

		// Ensure correct number of cores
		CpuLayout layout = AffinityLock.cpuLayout();
		assertEquals("Incorrect number of cores", layout.sockets() * layout.coresPerSocket(), cores.length);

		// Create the mapping
		Map<Integer, Integer> cpuIdToCoreId = new HashMap<>();
		for (CpuCore core : cores) {
			for (LogicalCpu cpu : core.getCpus()) {
				cpuIdToCoreId.put(cpu.getCpuId(), core.getCoreId());
			}
		}

		// Ensure correct mappings for the CPUs
		for (int cpuId = 0; cpuId < layout.cpus(); cpuId++) {
			int expectedCoreId = layout.coreId(cpuId);
			int actualCoreId = cpuIdToCoreId.get(cpuId);
			assertEquals("Incorrect Core for CPU " + cpuId, expectedCoreId, actualCoreId);
		}
	}

	/**
	 * Ensure can configure from custom {@link CpuLayout}.
	 */
	public void testCustomCpuLayout() {

		// Create mock CpuLayout
		CpuLayout layout = this.createMock(CpuLayout.class);

		// Record obtaining CPU information
		this.recordReturn(layout, layout.cpus(), 3);
		this.recordReturn(layout, layout.coreId(0), 3);
		this.recordReturn(layout, layout.coreId(1), 1);
		this.recordReturn(layout, layout.coreId(2), 3);

		// Record loading the layout
		this.replayMockObjects();
		CpuCore[] cores = CpuCore.runWithCpuLayout(layout, () -> CpuCore.getCores());

		// Validate the layout
		this.verifyMockObjects();

		// Ensure correct CPU cores
		assertEquals("Incorrect number of cores", 2, cores.length);
		CpuCore coreOne = cores[0];
		assertEquals("Incorrect core one", 1, coreOne.getCoreId());
		assertEquals("Incorrect number of core one CPUs", 1, coreOne.getCpus().length);
		assertEquals("Incorrect core one CPU", 1, coreOne.getCpus()[0].getCpuId());
		CpuCore coreTwo = cores[1];
		assertEquals("Incorrect core two", 3, coreTwo.getCoreId());
		assertEquals("Incorrect number of core two CPUs", 2, coreTwo.getCpus().length);
		assertEquals("Incorrect core two CPU one", 0, coreTwo.getCpus()[0].getCpuId());
		assertEquals("Incorrect core two CPU two", 2, coreTwo.getCpus()[1].getCpuId());
	}
}