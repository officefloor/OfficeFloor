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
package net.officefloor.frame.api.build;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * Context for the {@link OfficeEnhancer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeEnhancerContext {

	/**
	 * Obtains the {@link FlowBuilder} registered under the input
	 * {@link ManagedFunction} name.
	 * 
	 * @param functionName
	 *            Name of the {@link ManagedFunction}.
	 * @return {@link FlowBuilder} for the {@link ManagedFunction}.
	 */
	FlowBuilder<?> getFlowBuilder(String functionName);

	/**
	 * Obtains the {@link FlowBuilder} registered by the
	 * {@link ManagedObjectSource} under the input {@link ManagedFunction} name.
	 * 
	 * @param managedObjectSourceName
	 *            {@link ManagedObjectSource} name registered with the
	 *            {@link OfficeFloorBuilder}.
	 * @param functionName
	 *            Name of the {@link ManagedFunction}.
	 * @return {@link FlowBuilder} for the {@link ManagedFunction}.
	 */
	FlowBuilder<?> getFlowBuilder(String managedObjectSourceName, String functionName);

}