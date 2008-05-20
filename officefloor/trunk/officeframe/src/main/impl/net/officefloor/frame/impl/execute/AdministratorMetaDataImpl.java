/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.frame.impl.execute;

import java.util.Map;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.AdministratorContainer;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.DutyMetaData;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.ExtensionInterfaceMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.Team;

/**
 * Implementation of the {@link AdministratorMetaData}.
 * 
 * @author Daniel
 */
public class AdministratorMetaDataImpl<I extends Object, A extends Enum<A>>
		implements AdministratorMetaData<I, A> {

	/**
	 * Index of the {@link Administrator} for this
	 * {@link AdministratorContainer} within the {@link ProcessState} or
	 * {@link AdministratorMetaData#NON_PROCESS_INDEX}.
	 */
	protected final int processStateAdministratorIndex;

	/**
	 * {@link AdministratorSource}.
	 */
	protected final AdministratorSource<I, A> administratorSource;

	/**
	 * {@link ExtensionInterfaceMetaData}.
	 */
	protected final ExtensionInterfaceMetaData<I>[] eiMetaData;

	/**
	 * Indexes on the {@link Work} of the required {@link ManagedObject}
	 * instances.
	 */
	protected final int[] requiredManagedObjects;

	/**
	 * {@link Team}.
	 */
	protected final Team team;

	/**
	 * {@link EscalationProcedure}.
	 */
	protected final EscalationProcedure escalationProcedure;

	/**
	 * <p>
	 * Registry of {@link DutyMetaData} by its {@link Duty} key.
	 * <p>
	 * This is treated as <code>final</code>.
	 */
	protected Map<A, DutyMetaData> dutyMetaData;

	/**
	 * Initiate with meta-data of the {@link Administrator} scoped to the
	 * {@link ProcessState}.
	 * 
	 * @param processStateAdministratorIndex
	 *            Index of the {@link Administrator} within the
	 *            {@link ProcessState}.
	 */
	public AdministratorMetaDataImpl(int processStateAdministratorIndex) {
		this.processStateAdministratorIndex = processStateAdministratorIndex;
		this.eiMetaData = null;
		this.administratorSource = null;
		this.team = null;
		this.escalationProcedure = null;
		this.requiredManagedObjects = null;
	}

	/**
	 * Initiate with meta-data of the {@link Administrator} scope to the
	 * {@link Work}.
	 * 
	 * @param administratorSource
	 *            {@link AdministratorSource}.
	 * @param eiMetaData
	 *            {@link ExtensionInterfaceMetaData}.
	 * @param team
	 *            {@link Team}.
	 * @param escalationProcedure
	 *            {@link EscalationProcedure}.
	 */
	public AdministratorMetaDataImpl(
			AdministratorSource<I, A> administratorSource,
			ExtensionInterfaceMetaData<I>[] eiMetaData, Team team,
			EscalationProcedure escalationProcedure) {
		this.processStateAdministratorIndex = AdministratorMetaData.NON_PROCESS_INDEX;
		this.eiMetaData = eiMetaData;
		this.administratorSource = administratorSource;
		this.team = team;
		this.escalationProcedure = escalationProcedure;

		// Create the listing of required managed objects
		this.requiredManagedObjects = new int[this.eiMetaData.length];
		for (int i = 0; i < this.requiredManagedObjects.length; i++) {
			this.requiredManagedObjects[i] = this.eiMetaData[i]
					.getManagedObjectIndex();
		}
	}

	/**
	 * Loads the remaining state.
	 * 
	 * @param dutyMetaData
	 *            {@link DutyMetaData} for each {@link Duty} of the
	 *            {@link Administrator}.
	 */
	public void loadRemainingState(Map<A, DutyMetaData> dutyMetaData) {
		this.dutyMetaData = dutyMetaData;
	}

	/*
	 * ========================================================================
	 * AdministratorMetaData
	 * ========================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.AdministratorMetaData#createAdministratorContainer()
	 */
	@Override
	public AdministratorContainer<I, A> createAdministratorContainer() {

		// Create the container for the Administrator
		AdministratorContainer<I, A> administratorContainer;
		if (this.processStateAdministratorIndex == AdministratorMetaData.NON_PROCESS_INDEX) {
			// Source specific to this work
			administratorContainer = new AdministratorContainerImpl<I, A, None>(
					this);
		} else {
			// Source from the process state
			administratorContainer = new AdministratorContainerProxy<I, A>(
					this.processStateAdministratorIndex);
		}

		// Return the Administrator Container
		return administratorContainer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.AdministratorMetaData#getProcessStateAdministratorIndex()
	 */
	@Override
	public int getProcessStateAdministratorIndex() {
		return this.processStateAdministratorIndex;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.AdministratorMetaData#getAdministratorSource()
	 */
	@Override
	public AdministratorSource<I, A> getAdministratorSource() {
		return this.administratorSource;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.AdministratorMetaData#getExtensionInterfaceMetaData()
	 */
	@Override
	public ExtensionInterfaceMetaData<I>[] getExtensionInterfaceMetaData() {
		return this.eiMetaData;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.AdministratorMetaData#getDutyMetaData(A)
	 */
	@Override
	public DutyMetaData getDutyMetaData(A key) {
		return this.dutyMetaData.get(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.JobMetaData#getTeam()
	 */
	@Override
	public Team getTeam() {
		return this.team;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.JobMetaData#getRequiredManagedObjects()
	 */
	@Override
	public int[] getRequiredManagedObjects() {
		return this.requiredManagedObjects;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.JobMetaData#getEscalationProcedure()
	 */
	@Override
	public EscalationProcedure getEscalationProcedure() {
		return this.escalationProcedure;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.JobMetaData#getNextTaskInFlow()
	 */
	@Override
	public TaskMetaData<?, ?, ?, ?> getNextTaskInFlow() {
		// Never a next task for an administrator duty
		return null;
	}

}
