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
package net.officefloor.frame.impl.execute.thread;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.execute.function.Promise;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.EscalationProcedure;
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
	 * Maximum {@link Promise} chain length.
	 */
	private final int maximumPromiseChainLength;

	/**
	 * Break chain {@link Team}.
	 */
	private final Team breakChainTeam;

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
	 * @param managedObjectMetaData
	 *            {@link ManagedObjectMetaData} instances.
	 * @param governanceMetaData
	 *            {@link GovernanceMetaData} instances.
	 * @param maximumPromiseChainLength
	 *            Maximum {@link Promise} chain length.
	 * @param breakChainTeam
	 *            Break chain {@link Team}.
	 * @param officeEscalationProcedure
	 *            {@link Office} {@link EscalationProcedure}.
	 * @param officeFloorEscalation
	 *            {@link OfficeFloor} {@link EscalationFlow}.
	 */
	public ThreadMetaDataImpl(ManagedObjectMetaData<?>[] managedObjectMetaData,
			GovernanceMetaData<?, ?>[] governanceMetaData, int maximumPromiseChainLength, Team breakChainTeam,
			EscalationProcedure officeEscalationProcedure, EscalationFlow officeFloorEscalation) {
		this.managedObjectMetaData = managedObjectMetaData;
		this.governanceMetaData = governanceMetaData;
		this.maximumPromiseChainLength = maximumPromiseChainLength;
		this.breakChainTeam = breakChainTeam;
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
	public int getMaximumPromiseChainLength() {
		return this.maximumPromiseChainLength;
	}

	@Override
	public Team getBreakChainTeam() {
		return this.breakChainTeam;
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