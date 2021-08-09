/*-
 * #%L
 * Web Executive
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
