/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.frame.impl.construct.administrator;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.administration.Duty;
import net.officefloor.frame.api.build.DutyBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.impl.construct.function.ManagedFunctionReferenceImpl;
import net.officefloor.frame.internal.configuration.DutyConfiguration;
import net.officefloor.frame.internal.configuration.AdministrationGovernanceConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionReference;

/**
 * {@link DutyBuilder} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class DutyBuilderImpl<A extends Enum<A>> implements DutyBuilder, DutyConfiguration<A> {

	/**
	 * Name identifying the {@link AdministrationDuty}.
	 */
	private final String dutyName;

	/**
	 * Registry of {@link ManagedFunction} instances that may be invoked from
	 * the {@link AdministrationDuty}.
	 */
	private final Map<Integer, ManagedFunctionReference> flows = new HashMap<Integer, ManagedFunctionReference>();

	/**
	 * Registry of {@link Governance} instances that may be invoked from the
	 * {@link AdministrationDuty}.
	 */
	private final Map<Integer, AdministrationGovernanceConfiguration<?>> governances = new HashMap<Integer, AdministrationGovernanceConfiguration<?>>();

	/**
	 * Initiate.
	 * 
	 * @param dutyName
	 *            Name identifying the {@link AdministrationDuty}.
	 */
	public DutyBuilderImpl(String dutyName) {
		this.dutyName = dutyName;
	}

	/*
	 * ================== DutyBuilder =====================================
	 */

	@Override
	public <F extends Enum<F>> void linkFlow(F key, String functionName, Class<?> argumentType) {
		this.linkFlow(key.ordinal(), functionName, argumentType);
	}

	@Override
	public void linkFlow(int flowIndex, String functionName, Class<?> argumentType) {
		this.flows.put(Integer.valueOf(flowIndex), new ManagedFunctionReferenceImpl(functionName, argumentType));
	}

	@Override
	public <G extends Enum<G>> void linkGovernance(G key, String governanceName) {
		this.linkGovernance(key, key.ordinal(), governanceName);
	}

	@Override
	public void linkGovernance(int governanceIndex, String governanceName) {
		this.linkGovernance((Indexed) null, governanceIndex, governanceName);
	}

	/**
	 * Links the {@link Governance}.
	 * 
	 * @param key
	 *            Key to access the {@link Governance} from the {@link AdministrationDuty}.
	 * @param governanceIndex
	 *            Index of the {@link Governance} from the {@link AdministrationDuty}.
	 * @param governanceName
	 *            Name of the {@link Governance} to link.
	 */
	private <G extends Enum<G>> void linkGovernance(G key, int governanceIndex, String governanceName) {
		this.governances.put(Integer.valueOf(governanceIndex),
				new DutyGovernanceConfigurationImpl<G>(governanceName, governanceIndex));
	}

	/*
	 * ============== DutyConfiguration ===================================
	 */

	@Override
	public String getDutyName() {
		return this.dutyName;
	}

	@Override
	public ManagedFunctionReference[] getLinkedProcessConfiguration() {

		// Obtain the array size from maximum index
		int arraySize = -1;
		for (Integer key : this.flows.keySet()) {
			int index = key.intValue();
			if (index > arraySize) {
				arraySize = index;
			}
		}
		arraySize += 1; // size is one up of max index

		// Create the listing of task nodes
		ManagedFunctionReference[] taskNodes = new ManagedFunctionReference[arraySize];
		for (Integer key : this.flows.keySet()) {
			ManagedFunctionReference reference = this.flows.get(key);
			taskNodes[key.intValue()] = reference;
		}

		// Return the listing
		return taskNodes;
	}

	@Override
	public AdministrationGovernanceConfiguration<?>[] getGovernanceConfiguration() {

		// Obtain the array size from maximum index
		int arraySize = -1;
		for (Integer key : this.governances.keySet()) {
			int index = key.intValue();
			if (index > arraySize) {
				arraySize = index;
			}
		}
		arraySize += 1; // size is one up of max index

		// Create the listing of governance
		AdministrationGovernanceConfiguration<?>[] governanceList = new AdministrationGovernanceConfiguration[arraySize];
		for (Integer key : this.governances.keySet()) {
			AdministrationGovernanceConfiguration<?> governance = this.governances.get(key);
			governanceList[key.intValue()] = governance;
		}

		// Return the listing
		return governanceList;
	}

	/**
	 * {@link AdministrationGovernanceConfiguration} implementation.
	 */
	private static class DutyGovernanceConfigurationImpl<G extends Enum<G>> implements AdministrationGovernanceConfiguration<G> {

		/**
		 * Name of the {@link Governance}.
		 */
		private final String governanceName;

		/**
		 * Index of the {@link Governance} for the {@link AdministrationDuty}.
		 */
		private final int index;

		/**
		 * Initiate.
		 * 
		 * @param governanceName
		 *            Name of the {@link Governance}.
		 * @param index
		 *            Index of the {@link Governance} for the {@link AdministrationDuty}.
		 */
		public DutyGovernanceConfigurationImpl(String governanceName, int index) {
			this.governanceName = governanceName;
			this.index = index;
		}

		/*
		 * ============= DutyGovernanceConfiguration ==========================
		 */

		@Override
		public String getGovernanceName() {
			return this.governanceName;
		}

		@Override
		public int getIndex() {
			return this.index;
		}

		@Override
		public G getKey() {
			// TODO implement DutyGovernanceConfiguration<G>.getKey
			throw new UnsupportedOperationException("TODO implement DutyGovernanceConfiguration<G>.getKey");
		}
	}

}