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
