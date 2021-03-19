/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.impl.execute.thread;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.thread.ThreadSynchroniserFactory;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ThreadMetaData;

/**
 * {@link ThreadMetaData} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ThreadMetaDataImpl implements ThreadMetaData {

	/**
	 * {@link ManagedObjectMetaData} instances.
	 */
	private final ManagedObjectMetaData<?>[] managedObjectMetaData;

	/**
	 * {@link GovernanceMetaData} instances.
	 */
	private final GovernanceMetaData<?, ?>[] governanceMetaData;

	/**
	 * Maximum {@link FunctionState} chain length.
	 */
	private final int maximumFunctionChainLength;

	/**
	 * {@link ThreadSynchroniserFactory} instances.
	 */
	private final ThreadSynchroniserFactory[] threadSynchronisers;

	/**
	 * {@link Office} {@link EscalationProcedure}.
	 */
	private final EscalationProcedure officeEscalationProcedure;

	/**
	 * {@link OfficeFloor} {@link EscalationFlow}.
	 */
	private final EscalationFlow officeFloorEscalation;

	/**
	 * Initiate.
	 * 
	 * @param managedObjectMetaData      {@link ManagedObjectMetaData} instances.
	 * @param governanceMetaData         {@link GovernanceMetaData} instances.
	 * @param maximumFunctionChainLength Maximum {@link FunctionState} chain length.
	 * @param threadSynchronisers        {@link ThreadSynchroniserFactory}
	 *                                   instances.
	 * @param officeEscalationProcedure  {@link Office} {@link EscalationProcedure}.
	 * @param officeFloorEscalation      {@link OfficeFloor} {@link EscalationFlow}.
	 */
	public ThreadMetaDataImpl(ManagedObjectMetaData<?>[] managedObjectMetaData,
			GovernanceMetaData<?, ?>[] governanceMetaData, int maximumFunctionChainLength,
			ThreadSynchroniserFactory[] threadSynchronisers, EscalationProcedure officeEscalationProcedure,
			EscalationFlow officeFloorEscalation) {
		this.managedObjectMetaData = managedObjectMetaData;
		this.governanceMetaData = governanceMetaData;
		this.maximumFunctionChainLength = maximumFunctionChainLength;
		this.threadSynchronisers = threadSynchronisers;
		this.officeEscalationProcedure = officeEscalationProcedure;
		this.officeFloorEscalation = officeFloorEscalation;
	}

	/*
	 * ================== ThreadMetaData =============================
	 */

	@Override
	public ManagedObjectMetaData<?>[] getManagedObjectMetaData() {
		return this.managedObjectMetaData;
	}

	@Override
	public GovernanceMetaData<?, ?>[] getGovernanceMetaData() {
		return this.governanceMetaData;
	}

	@Override
	public int getMaximumFunctionChainLength() {
		return this.maximumFunctionChainLength;
	}

	@Override
	public ThreadSynchroniserFactory[] getThreadSynchronisers() {
		return this.threadSynchronisers;
	}

	@Override
	public EscalationProcedure getOfficeEscalationProcedure() {
		return this.officeEscalationProcedure;
	}

	@Override
	public EscalationFlow getOfficeFloorEscalation() {
		return this.officeFloorEscalation;
	}

}
