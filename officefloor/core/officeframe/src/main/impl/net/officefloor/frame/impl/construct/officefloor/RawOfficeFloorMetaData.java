/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.impl.construct.officefloor;

import java.util.Map;

import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.construct.managedobjectsource.RawManagedObjectMetaData;
import net.officefloor.frame.impl.construct.team.RawTeamMetaData;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.ManagedExecutionFactory;
import net.officefloor.frame.internal.structure.OfficeFloorMetaData;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadLocalAwareExecutor;

/**
 * Raw {@link OfficeFloorMetaData} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class RawOfficeFloorMetaData {

	/**
	 * {@link Executive}.
	 */
	private final Executive executive;

	/**
	 * Registry of {@link RawTeamMetaData} by the {@link Team} name.
	 */
	private final Map<String, RawTeamMetaData> teamRegistry;

	/**
	 * {@link TeamManagement} to break the {@link FunctionState} chain.
	 */
	private final TeamManagement breakChainTeamManagement;

	/**
	 * {@link ThreadLocalAwareExecutor}.
	 */
	private final ThreadLocalAwareExecutor threadLocalAwareExecutor;

	/**
	 * {@link ManagedExecutionFactory}.
	 */
	private final ManagedExecutionFactory managedExecutionFactory;

	/**
	 * Registry of {@link RawManagedObjectMetaData} by the
	 * {@link ManagedObjectSource} name.
	 */
	private final Map<String, RawManagedObjectMetaData<?, ?>> mosRegistry;

	/**
	 * {@link EscalationFlow} for the {@link OfficeFloor}.
	 */
	private final EscalationFlow officeFloorEscalation;

	/**
	 * {@link OfficeFloorMetaData}.
	 */
	OfficeFloorMetaData officeFloorMetaData;

	/**
	 * Initiate.
	 * 
	 * @param executive                {@link Executive}.
	 * @param teamRegistry             Registry of {@link RawTeamMetaData} by the
	 *                                 {@link Team} name.
	 * @param breakChainTeamManagement {@link TeamManagement} to break the
	 *                                 {@link FunctionState} chain.
	 * @param threadLocalAwareExecutor {@link ThreadLocalAwareExecutor}.
	 * @param managedExecutionFactory  {@link ManagedExecutionFactory}.
	 * @param mosRegistry              Registry of {@link RawManagedObjectMetaData}
	 *                                 by the {@link ManagedObjectSource} name.
	 * @param officeFloorEscalation    {@link EscalationProcedure}.
	 */
	public RawOfficeFloorMetaData(Executive executive, Map<String, RawTeamMetaData> teamRegistry,
			TeamManagement breakChainTeamManagement, ThreadLocalAwareExecutor threadLocalAwareExecutor,
			ManagedExecutionFactory managedExecutionFactory, Map<String, RawManagedObjectMetaData<?, ?>> mosRegistry,
			EscalationFlow officeFloorEscalation) {
		this.executive = executive;
		this.teamRegistry = teamRegistry;
		this.breakChainTeamManagement = breakChainTeamManagement;
		this.threadLocalAwareExecutor = threadLocalAwareExecutor;
		this.managedExecutionFactory = managedExecutionFactory;
		this.mosRegistry = mosRegistry;
		this.officeFloorEscalation = officeFloorEscalation;
	}

	/**
	 * Obtains the {@link Executive}.
	 * 
	 * @return {@link Executive}.
	 */
	public Executive getExecutive() {
		return this.executive;
	}

	/**
	 * Obtains the {@link RawTeamMetaData} for the {@link Team} name.
	 * 
	 * @param teamName Name of the {@link Team}.
	 * @return {@link RawTeamMetaData} or <code>null</code> if not exist for name.
	 */
	public RawTeamMetaData getRawTeamMetaData(String teamName) {
		return this.teamRegistry.get(teamName);
	}

	/**
	 * Obtains the {@link TeamManagement} to break the {@link FunctionState} chain.
	 * 
	 * @return {@link TeamManagement} to break the {@link FunctionState} chain.
	 */
	public TeamManagement getBreakChainTeamManagement() {
		return this.breakChainTeamManagement;
	}

	/**
	 * Obtains the {@link ThreadLocalAwareExecutor}.
	 * 
	 * @return {@link ThreadLocalAwareExecutor}.
	 */
	public ThreadLocalAwareExecutor getThreadLocalAwareExecutor() {
		return this.threadLocalAwareExecutor;
	}

	/**
	 * Obtains the {@link ManagedExecutionFactory}.
	 * 
	 * @return {@link ManagedExecutionFactory}.
	 */
	public ManagedExecutionFactory getManagedExecutionFactory() {
		return this.managedExecutionFactory;
	}

	/**
	 * Obtains the {@link RawManagedObjectMetaData} for the
	 * {@link ManagedObjectSource} name.
	 * 
	 * @param managedObjectSourceName Name of the {@link ManagedObjectSource}.
	 * @return {@link RawManagedObjectMetaData} or <code>null</code> if not exist
	 *         for name.
	 */
	public RawManagedObjectMetaData<?, ?> getRawManagedObjectMetaData(String managedObjectSourceName) {
		return this.mosRegistry.get(managedObjectSourceName);
	}

	/**
	 * Obtains the {@link EscalationFlow} for the {@link OfficeFloor}.
	 * 
	 * @return {@link EscalationFlow}.
	 */
	public EscalationFlow getOfficeFloorEscalation() {
		return this.officeFloorEscalation;
	}

	/**
	 * Obtains the {@link OfficeFloorMetaData}.
	 * 
	 * @return {@link OfficeFloorMetaData}.
	 */
	public OfficeFloorMetaData getOfficeFloorMetaData() {
		return this.officeFloorMetaData;
	}

}