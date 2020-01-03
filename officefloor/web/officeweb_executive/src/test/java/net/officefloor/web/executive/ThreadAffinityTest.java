/*-
 * #%L
 * Web Executive
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

package net.officefloor.web.executive;

import java.util.Arrays;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.openhft.affinity.Affinity;
import net.openhft.affinity.AffinityLock;
import net.openhft.affinity.CpuLayout;

/**
 * Ensure able to use {@link Affinity}.
 * 
 * @author Daniel Sagenschneider
 */
public class ThreadAffinityTest extends OfficeFrameTestCase {

	/**
	 * Ensure {@link Affinity} available for use.
	 */
	public void testAffinityAvailable() {
		assertTrue(Affinity.class.getName()
				+ " does not have JNA support installed.  Please install from http://github.com/OpenHFT/Java-Thread-Affinity",
				Affinity.isJNAAvailable());

		// Indicate layout of CPU
		CpuLayout layout = AffinityLock.cpuLayout();
		System.out.println("CPU layout:\n\tSockets: " + layout.sockets() + "\n\tCores per socket:"
				+ layout.coresPerSocket() + "\n\tThreads per core: " + layout.coresPerSocket());
		assertEquals("Affinity information does not match Java runtime", Runtime.getRuntime().availableProcessors(),
				layout.sockets() * layout.coresPerSocket() * layout.threadsPerCore());

		// Provide details on the cores
		CpuCore[] cores = CpuCore.getCores();
		assertEquals("Incorrect number of cores", layout.sockets() * layout.coresPerSocket(), cores.length);
		System.out.println("Core layout:");
		int totalCpus = 0;
		for (int coreId = 0; coreId < cores.length; coreId++) {
			CpuCore core = cores[coreId];
			assertEquals("Incorrect core identifier", coreId, core.getCoreId());
			String[] values = Arrays.asList(core.getCpus()).stream()
					.map((cpu) -> String.valueOf(cpu.getCpuId() + " [" + cpu.getCpuAffinity() + "]"))
					.toArray(String[]::new);
			System.out.println(
					"\t" + core.getCoreId() + " [" + core.getCoreAffinity() + "]: " + String.join(", ", values));
			totalCpus += core.getCpus().length;
		}
		assertEquals("Incorrect number of CPUs", Runtime.getRuntime().availableProcessors(), totalCpus);

		// Determine if hyper threading on CPU
		boolean isHyperThreading = (layout.threadsPerCore() >= 2);
		System.out.println(isHyperThreading ? "(hyper threading available)" : "(core per thread - no hyper threading)");
	}

}
