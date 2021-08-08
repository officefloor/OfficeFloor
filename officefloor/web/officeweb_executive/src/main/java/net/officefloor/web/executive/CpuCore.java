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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.openhft.affinity.Affinity;
import net.openhft.affinity.AffinityLock;
import net.openhft.affinity.CpuLayout;

/**
 * Core information linked to logical CPUs.
 * 
 * @author Daniel Sagenschneider
 */
public class CpuCore {

	/**
	 * Configured {@link CpuLayout}.
	 */
	private static ThreadLocal<CpuLayout> cpuLayout = new ThreadLocal<>();

	/**
	 * <p>
	 * Runs with custom {@link CpuLayout}.
	 * <p>
	 * This for example, allows restricting access to particular CPU cores or
	 * logical CPUs.
	 *
	 * @param                 <R> Return type.
	 * @param                 <T> Possible {@link Throwable} type.
	 * @param layout          {@link CpuLayout}.
	 * @param customExecution {@link CustomExecution}.
	 * @return Possible return value.
	 * @throws T Failure on running.
	 */
	public static <R, T extends Throwable> R runWithCpuLayout(CpuLayout layout, CustomExecution<R, T> customExecution)
			throws T {
		CpuLayout existing = cpuLayout.get();
		try {

			// Use the custom layout
			cpuLayout.set(layout);

			// Undertake custom execution
			return customExecution.run();

		} finally {
			// Reset CPU layout
			cpuLayout.set(existing);
		}
	}

	/**
	 * {@link FunctionalInterface} for custom {@link CpuLayout} execution.
	 */
	@FunctionalInterface
	public static interface CustomExecution<R, T extends Throwable> {

		/**
		 * Runs the custom {@link CpuLayout} execution.
		 *
		 * @return Possible return value.
		 * @throws T Failure on running.
		 */
		R run() throws T;
	}

	/**
	 * Obtains the {@link CpuCore} instances.
	 * 
	 * @return {@link CpuCore} instances.
	 */
	public static CpuCore[] getCores() {

		// Obtain the affinity (for bitset sizing)
		BitSet affinity = Affinity.getAffinity();
		int affinityBitSize = affinity.size();

		// Ensure have CPU layout
		CpuLayout layout = cpuLayout.get();
		if (layout == null) {
			layout = AffinityLock.cpuLayout();
		}

		// Create the listing of cores against logical CPUs
		Map<Integer, List<Integer>> allocation = new HashMap<>();
		int cpuCount = layout.cpus();
		for (int cpuId = 0; cpuId < cpuCount; cpuId++) {
			int core = layout.coreId(cpuId);
			List<Integer> coreCpus = allocation.get(core);
			if (coreCpus == null) {
				coreCpus = new ArrayList<>();
				allocation.put(core, coreCpus);
			}
			coreCpus.add(cpuId);
		}

		// Create the Core listing
		List<CpuCore> cores = new ArrayList<>(allocation.size());
		allocation.keySet().stream().sorted().forEachOrdered((coreId) -> {

			// Obtain the logical CPUs
			List<Integer> coreCpuIds = allocation.get(coreId);
			if ((coreCpuIds == null) || (coreCpuIds.size() == 0)) {
				return; // no logical CPUs available on core (possibly restricted)
			}
			LogicalCpu[] cpus = coreCpuIds.stream().map((cpuId) -> new LogicalCpu(cpuId, affinityBitSize))
					.toArray(LogicalCpu[]::new);

			// Create the core affintity bitset
			BitSet coreAffinity = new BitSet(affinityBitSize);
			for (LogicalCpu cpu : cpus) {
				coreAffinity.set(cpu.getCpuId(), true);
			}

			// Load the core information
			cores.add(new CpuCore(coreId, coreAffinity, cpus));
		});

		// Return the cores
		return cores.toArray(new CpuCore[cores.size()]);
	}

	/**
	 * Core identifier.
	 */
	private final int coreId;

	/**
	 * {@link BitSet} mask to have affinity to the {@link CpuCore}.
	 */
	private final BitSet coreAffinity;

	/**
	 * {@link LogicalCpu} instances for the {@link CpuCore}.
	 */
	private final LogicalCpu[] cpus;

	/**
	 * Instantiate.
	 * 
	 * @param coreId       Core identifier.
	 * @param coreAffinity Affinity {@link BitSet} for all {@link LogicalCpu}
	 *                     instances on the {@link CpuCore}.
	 * @param cpus         {@link LogicalCpu} instances for the {@link CpuCore}.
	 */
	public CpuCore(int coreId, BitSet coreAffinity, LogicalCpu[] cpus) {
		this.coreId = coreId;
		this.coreAffinity = coreAffinity;
		this.cpus = cpus;
	}

	/**
	 * Obtains the Core identifier.
	 * 
	 * @return Core identifier.
	 */
	public int getCoreId() {
		return this.coreId;
	}

	/**
	 * Obtains the {@link CpuCore} {@link BitSet} affinity.
	 * 
	 * @return {@link CpuCore} {@link BitSet} affinity.
	 */
	public BitSet getCoreAffinity() {
		return this.coreAffinity;
	}

	/**
	 * Obtains the {@link LogicalCpu} instances on this {@link CpuCore}.
	 * 
	 * @return {@link LogicalCpu} instances on this {@link CpuCore}.
	 */
	public LogicalCpu[] getCpus() {
		return this.cpus;
	}

	/**
	 * Logical CPU on a Core.
	 */
	public static class LogicalCpu {

		/**
		 * CPU identifier.
		 */
		private final int cpuId;

		/**
		 * {@link BitSet} mask to have affinity to the {@link LogicalCpu}.
		 */
		private final BitSet cpuAffinity;

		/**
		 * Instantiate.
		 * 
		 * @param cpuId           CPU identifier.
		 * @param affinityBitSize Size of {@link BitSet} for affinity.
		 */
		private LogicalCpu(int cpuId, int affinityBitSize) {
			this.cpuId = cpuId;

			// Generate the bitset affinity
			this.cpuAffinity = new BitSet(affinityBitSize);
			this.cpuAffinity.set(this.cpuId, true);
		}

		/**
		 * Obtains the CPU identifier.
		 * 
		 * @return CPU identifier.
		 */
		public int getCpuId() {
			return this.cpuId;
		}

		/**
		 * Obtains the {@link BitSet} {@link LogicalCpu} affinity.
		 * 
		 * @return {@link BitSet} {@link LogicalCpu} affinity.
		 */
		public BitSet getCpuAffinity() {
			return this.cpuAffinity;
		}
	}

}
