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
package net.officefloor.frame.impl.spi.team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

		// Indicate logical CPUs on same core
		Map<Integer, List<String>> allocation = new HashMap<>();
		for (int cpu = 0; cpu < Runtime.getRuntime().availableProcessors(); cpu++) {
			int core = layout.coreId(cpu);
			List<String> coreCpus = allocation.get(core);
			if (coreCpus == null) {
				coreCpus = new ArrayList<>();
				allocation.put(core, coreCpus);
			}
			coreCpus.add(String.valueOf(cpu));
		}
		System.out.println("Core layout:");
		for (int core = 0; core < (layout.sockets() * layout.coresPerSocket()); core++) {
			System.out.println("\t" + core + ": " + String.join(",", allocation.get(core)));
			allocation.remove(core);
		}
		assertEquals("Affinity information does not line up cores to cpus", 0, allocation.size());

		// Determine if hyper threading on CPU
		boolean isHyperThreading = (layout.threadsPerCore() >= 2);
		System.out.println(isHyperThreading ? "(hyper threading available)" : "(core per thread - no hyper threading)");
	}

}