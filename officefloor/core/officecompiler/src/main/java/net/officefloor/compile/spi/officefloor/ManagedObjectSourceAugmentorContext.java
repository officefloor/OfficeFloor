/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.compile.spi.officefloor;

import net.officefloor.compile.issues.SourceIssues;
import net.officefloor.compile.managedobject.ManagedObjectExecutionStrategyType;
import net.officefloor.compile.managedobject.ManagedObjectFlowType;
import net.officefloor.compile.managedobject.ManagedObjectTeamType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * Context for the {@link ManagedObjectSourceAugmentor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectSourceAugmentorContext extends SourceIssues {

	/**
	 * Obtains the name of the {@link ManagedObjectSource}.
	 * 
	 * @return Name of the {@link ManagedObjectSource}.
	 */
	String getManagedObjectSourceName();

	/**
	 * Obtains the {@link ManagedObjectType} of the {@link ManagedObjectSource}.
	 * 
	 * @return {@link ManagedObjectType} of the {@link ManagedObjectSource}.
	 */
	ManagedObjectType<?> getManagedObjectType();

	/**
	 * Obtains the {@link AugmentedManagedObjectFlow} for the
	 * {@link ManagedObjectFlowType}.
	 * 
	 * @param managedObjectSourceFlowName Name of the {@link ManagedObjectFlowType}.
	 * @return {@link AugmentedManagedObjectFlow}.
	 */
	AugmentedManagedObjectFlow getManagedObjectFlow(String managedObjectSourceFlowName);

	/**
	 * Obtains the {@link AugmentedManagedObjectTeam} for the
	 * {@link ManagedObjectTeamType}.
	 * 
	 * @param managedObjectSourceTeamName Name of the {@link ManagedObjectTeamType}.
	 * @return {@link AugmentedManagedObjectTeam}.
	 */
	AugmentedManagedObjectTeam getManagedObjectTeam(String managedObjectSourceTeamName);

	/**
	 * Obtains the {@link AugmentedManagedObjectExecutionStrategy} for the
	 * {@link ManagedObjectExecutionStrategyType}.
	 * 
	 * @param managedObjectSourceExecutionStrategyName Name of the
	 *                                                 {@link ManagedObjectExecutionStrategyType}.
	 * @return {@link AugmentedManagedObjectExecutionStrategy}.
	 */
	AugmentedManagedObjectExecutionStrategy getManagedObjectExecutionStrategy(
			String managedObjectSourceExecutionStrategyName);

	/**
	 * Links the {@link AugmentedManagedObjectFlow} to the
	 * {@link DeployedOfficeInput}.
	 * 
	 * @param flow        {@link AugmentedManagedObjectFlow}.
	 * @param officeInput {@link DeployedOfficeInput}.
	 */
	void link(AugmentedManagedObjectFlow flow, DeployedOfficeInput officeInput);

	/**
	 * Links the {@link AugmentedManagedObjectTeam} to the {@link OfficeFloorTeam}.
	 * 
	 * @param responsibility {@link AugmentedManagedObjectTeam}.
	 * @param team           {@link OfficeFloorTeam}.
	 */
	void link(AugmentedManagedObjectTeam responsibility, OfficeFloorTeam team);

	/**
	 * Links the {@link AugmentedManagedObjectExecutionStrategy} to the
	 * {@link OfficeFloorExecutionStrategy}.
	 * 
	 * @param requiredStrategy  {@link AugmentedManagedObjectExecutionStrategy}.
	 * @param executionStrategy {@link OfficeFloorExecutionStrategy}.
	 */
	void link(AugmentedManagedObjectExecutionStrategy requiredStrategy, OfficeFloorExecutionStrategy executionStrategy);

}
