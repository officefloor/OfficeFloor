/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

import net.officefloor.frame.api.build.DutyBuilder;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.impl.construct.task.TaskNodeReferenceImpl;
import net.officefloor.frame.internal.configuration.DutyConfiguration;
import net.officefloor.frame.internal.configuration.DutyGovernanceConfiguration;
import net.officefloor.frame.internal.configuration.TaskNodeReference;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.governance.Governance;

/**
 * {@link DutyBuilder} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class DutyBuilderImpl<A extends Enum<A>> implements DutyBuilder,
		DutyConfiguration<A> {

	/**
	 * Name identifying the {@link Duty}.
	 */
	private final String dutyName;

	/**
	 * Registry of {@link Task} instances that may be invoked from the
	 * {@link Duty}.
	 */
	private final Map<Integer, TaskNodeReference> flows = new HashMap<Integer, TaskNodeReference>();

	/**
	 * Registry of {@link Governance} instances that may be invoked from the
	 * {@link Duty}.
	 */
	private final Map<Integer, DutyGovernanceConfiguration<?>> governances = new HashMap<Integer, DutyGovernanceConfiguration<?>>();

	/**
	 * Initiate.
	 * 
	 * @param dutyName
	 *            Name identifying the {@link Duty}.
	 */
	public DutyBuilderImpl(String dutyName) {
		this.dutyName = dutyName;
	}

	/*
	 * ================== DutyBuilder =====================================
	 */

	@Override
	public <F extends Enum<F>> void linkFlow(F key, String workName,
			String taskName, Class<?> argumentType) {
		this.linkFlow(key.ordinal(), workName, taskName, argumentType);
	}

	@Override
	public void linkFlow(int flowIndex, String workName, String taskName,
			Class<?> argumentType) {
		this.flows.put(new Integer(flowIndex), new TaskNodeReferenceImpl(
				workName, taskName, argumentType));
	}

	@Override
	public <G extends Enum<G>> void linkGovernance(G key, String governanceName) {
		// TODO implement DutyBuilder.linkGovernance
		throw new UnsupportedOperationException(
				"TODO implement DutyBuilder.linkGovernance");
	}

	@Override
	public void linkGovernance(int governanceIndex, String governanceName) {
		// TODO implement DutyBuilder.linkGovernance
		throw new UnsupportedOperationException(
				"TODO implement DutyBuilder.linkGovernance");
	}

	/*
	 * ============== DutyConfiguration ===================================
	 */

	@Override
	public String getDutyName() {
		return this.dutyName;
	}

	@Override
	public TaskNodeReference[] getLinkedProcessConfiguration() {

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
		TaskNodeReference[] taskNodes = new TaskNodeReference[arraySize];
		for (Integer key : this.flows.keySet()) {
			TaskNodeReference reference = this.flows.get(key);
			taskNodes[key.intValue()] = reference;
		}

		// Return the listing
		return taskNodes;
	}

	@Override
	public DutyGovernanceConfiguration<?>[] getGovernanceConfiguration() {

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
		DutyGovernanceConfiguration<?>[] governanceList = new DutyGovernanceConfiguration[arraySize];
		for (Integer key : this.governances.keySet()) {
			DutyGovernanceConfiguration<?> governance = this.governances
					.get(key);
			governanceList[key.intValue()] = governance;
		}

		// Return the listing
		return governanceList;
	}

	/**
	 * {@link DutyGovernanceConfiguration} implementation.
	 */
	private static class DutyGovernanceConfigurationImpl<G extends Enum<G>>
			implements DutyGovernanceConfiguration<G> {

		/*
		 * ============= DutyGovernanceConfiguration ==========================
		 */

		@Override
		public String getGovernanceName() {
			// TODO implement DutyGovernanceConfiguration<G>.getGovernanceName
			throw new UnsupportedOperationException(
					"TODO implement DutyGovernanceConfiguration<G>.getGovernanceName");
		}

		@Override
		public int getIndex() {
			// TODO implement DutyGovernanceConfiguration<G>.getIndex
			throw new UnsupportedOperationException(
					"TODO implement DutyGovernanceConfiguration<G>.getIndex");
		}

		@Override
		public G getKey() {
			// TODO implement DutyGovernanceConfiguration<G>.getKey
			throw new UnsupportedOperationException(
					"TODO implement DutyGovernanceConfiguration<G>.getKey");
		}
	}

}