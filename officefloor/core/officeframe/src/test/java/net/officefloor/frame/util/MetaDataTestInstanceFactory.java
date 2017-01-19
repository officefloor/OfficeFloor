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
package net.officefloor.frame.util;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.impl.execute.escalation.EscalationProcedureImpl;
import net.officefloor.frame.impl.execute.managedfunction.ManagedFunctionMetaDataImpl;
import net.officefloor.frame.impl.execute.team.TeamManagementImpl;
import net.officefloor.frame.impl.spi.team.PassiveTeam;
import net.officefloor.frame.internal.structure.AdministrationMetaData;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.TeamManagement;

/**
 * Factory for the creation of test instances of various objects.
 * 
 * @author Daniel Sagenschneider
 */
public class MetaDataTestInstanceFactory {

	/**
	 * Creates the {@link ManagedFunctionMetaData}.
	 * 
	 * @param function
	 *            {@link ManagedFunction}.
	 * @return {@link ManagedFunctionMetaData}.
	 */
	@Deprecated
	public static <O extends Enum<O>, F extends Enum<F>> ManagedFunctionMetaData<O, F> createFunctionMetaData(
			final ManagedFunction<O, F> task) {

		// Function Factory
		final ManagedFunctionFactory<O, F> functionFactory = new ManagedFunctionFactory<O, F>() {
			@Override
			public ManagedFunction<O, F> createManagedFunction() {
				return task;
			}
		};

		// Create the team
		TeamManagement teamManagement = new TeamManagementImpl(new PassiveTeam());

		// Create and initialise the meta-data
		ManagedFunctionMetaDataImpl<O, F> metaData = new ManagedFunctionMetaDataImpl<O, F>("TEST_FUNCTION",
				functionFactory, "TEST_DIFFERENTIATOR", Object.class, teamManagement, new ManagedObjectIndex[0],
				new ManagedObjectMetaData<?>[0], new ManagedObjectIndex[0], new boolean[0]);
		metaData.loadOfficeMetaData(null, new FlowMetaData[0], null, new EscalationProcedureImpl(),
				new AdministrationMetaData<?, ?, ?>[0], new AdministrationMetaData<?, ?, ?>[0]);

		// Return the meta-data
		return metaData;
	}

	/**
	 * All access via static methods.
	 */
	private MetaDataTestInstanceFactory() {
	}

}