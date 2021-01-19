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

package net.officefloor.frame.impl.construct.officefloor;

import java.util.Map;
import java.util.concurrent.ThreadFactory;

import net.officefloor.frame.api.build.OfficeFloorListener;
import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.construct.managedobjectsource.RawManagedObjectMetaData;
import net.officefloor.frame.impl.construct.team.RawTeamMetaData;
import net.officefloor.frame.internal.structure.BackgroundScheduling;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.ManagedExecutionFactory;
import net.officefloor.frame.internal.structure.OfficeFloorMetaData;
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
	 * Default {@link ExecutionStrategy}.
	 */
	private final ThreadFactory[] defaultExecutionStrategy;

	/**
	 * {@link ExecutionStrategy} instances by name.
	 */
	private final Map<String, ThreadFactory[]> executionStrategies;

	/**
	 * Registry of {@link RawTeamMetaData} by the {@link Team} name.
	 */
	private final Map<String, RawTeamMetaData> teamRegistry;

	/**
	 * {@link BackgroundScheduling} instances.
	 */
	private final BackgroundScheduling[] backgroundSchedulings;

	/**
	 * Object to notify on start up completion.
	 */
	private final Object startupNotify;

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
	 * {@link OfficeFloorListener} instances.
	 */
	private final OfficeFloorListener[] officeFloorListeners;

	/**
	 * {@link OfficeFloorMetaData}.
	 */
	OfficeFloorMetaData officeFloorMetaData;

	/**
	 * Initiate.
	 * 
	 * @param executive                {@link Executive}.
	 * @param defaultExecutionStrategy Default {@link ExecutionStrategy}.
	 * @param executionStrategies      {@link ExecutionStrategy} instances by name.
	 * @param teamRegistry             Registry of {@link RawTeamMetaData} by the
	 *                                 {@link Team} name.
	 * @param backgroundSchedulings    {@link BackgroundScheduling} instances.
	 * @param startupNotify            Object to notify on start up completion.
	 * @param threadLocalAwareExecutor {@link ThreadLocalAwareExecutor}.
	 * @param managedExecutionFactory  {@link ManagedExecutionFactory}.
	 * @param mosRegistry              Registry of {@link RawManagedObjectMetaData}
	 *                                 by the {@link ManagedObjectSource} name.
	 * @param officeFloorEscalation    {@link EscalationProcedure}.
	 * @param officeFloorListeners     {@link OfficeFloorListener} instances.
	 */
	public RawOfficeFloorMetaData(Executive executive, ThreadFactory[] defaultExecutionStrategy,
			Map<String, ThreadFactory[]> executionStrategies, Map<String, RawTeamMetaData> teamRegistry,
			BackgroundScheduling[] backgroundSchedulings, Object startupNotify,
			ThreadLocalAwareExecutor threadLocalAwareExecutor, ManagedExecutionFactory managedExecutionFactory,
			Map<String, RawManagedObjectMetaData<?, ?>> mosRegistry, EscalationFlow officeFloorEscalation,
			OfficeFloorListener[] officeFloorListeners) {
		this.executive = executive;
		this.defaultExecutionStrategy = defaultExecutionStrategy;
		this.executionStrategies = executionStrategies;
		this.teamRegistry = teamRegistry;
		this.backgroundSchedulings = backgroundSchedulings;
		this.startupNotify = startupNotify;
		this.threadLocalAwareExecutor = threadLocalAwareExecutor;
		this.managedExecutionFactory = managedExecutionFactory;
		this.mosRegistry = mosRegistry;
		this.officeFloorEscalation = officeFloorEscalation;
		this.officeFloorListeners = officeFloorListeners;
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
	 * Obtains the default {@link ExecutionStrategy}.
	 * 
	 * @return Default {@link ExecutionStrategy}.
	 */
	public ThreadFactory[] getDefaultExecutionStrategy() {
		return this.defaultExecutionStrategy;
	}

	/**
	 * Obtains the {@link ExecutionStrategy} instances by name.
	 * 
	 * @return {@link ExecutionStrategy} instances by name.
	 */
	public Map<String, ThreadFactory[]> getExecutionStrategies() {
		return this.executionStrategies;
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
	 * Obtains the {@link BackgroundScheduling} instances.
	 * 
	 * @return {@link BackgroundScheduling} instances.
	 */
	public BackgroundScheduling[] getBackgroundSchedulings() {
		return this.backgroundSchedulings;
	}

	/**
	 * Obtains the object to notify on start up completion.
	 * 
	 * @return Object to notify on start up completion.
	 */
	public Object getStartupNotify() {
		return this.startupNotify;
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

	/**
	 * Obtains the {@link OfficeFloorListener} instances.
	 * 
	 * @return {@link OfficeFloorListener} instances.
	 */
	public OfficeFloorListener[] getOfficeFloorListeners() {
		return this.officeFloorListeners;
	}

}
