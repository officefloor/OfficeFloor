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

import java.util.BitSet;

import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.source.ExecutiveSource;
import net.officefloor.frame.api.executive.source.ExecutiveSourceContext;
import net.officefloor.frame.api.executive.source.impl.AbstractExecutiveSource;
import net.openhft.affinity.Affinity;

/**
 * Web based {@link ExecutiveSource} providing {@link Thread} affinity.
 * 
 * @author Daniel Sagenschneider
 */
public class WebThreadAffinityExecutiveSource extends AbstractExecutiveSource {

	/**
	 * Indicates if {@link Affinity} available.
	 * 
	 * @return <code>true</code> if {@link Affinity} available.
	 */
	public static boolean isThreadAffinityAvailable() {
		BitSet initialAffinity = Affinity.getAffinity();
		try {

			// Test by setting affinity
			BitSet newAffinity = new BitSet(initialAffinity.size());
			newAffinity.set(1);
			Affinity.setAffinity(newAffinity);

			// Affinity available if changed
			BitSet alteredAffinity = Affinity.getAffinity();
			return newAffinity.equals(alteredAffinity);

		} finally {
			Affinity.setAffinity(initialAffinity);
		}
	}

	/*
	 * ==================== ExecutiveSource ==========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
	}

	@Override
	public Executive createExecutive(ExecutiveSourceContext context) throws Exception {

		// Obtain the CPU cores
		CpuCore[] cpuCores = CpuCore.getCores();

		// Return the executive
		return new WebThreadAffinityExecutive(cpuCores, context);
	}

}
