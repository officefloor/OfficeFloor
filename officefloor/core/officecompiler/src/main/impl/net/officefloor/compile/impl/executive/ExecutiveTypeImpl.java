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

package net.officefloor.compile.impl.executive;

import net.officefloor.compile.executive.ExecutionStrategyType;
import net.officefloor.compile.executive.ExecutiveType;
import net.officefloor.frame.api.executive.TeamOversight;

/**
 * {@link ExecutiveType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ExecutiveTypeImpl implements ExecutiveType {

	/**
	 * {@link ExecutionStrategyType} instances.
	 */
	private final ExecutionStrategyType[] executionStrategyTypes;

	/**
	 * Indicates if providing {@link TeamOversight}.
	 */
	private final boolean isProvidingTeamOversight;

	/**
	 * Instantiate.
	 * 
	 * @param executionStrategyTypes   {@link ExecutionStrategyType} instances.
	 * @param isProvidingTeamOversight Indicates if providing {@link TeamOversight}.
	 */
	public ExecutiveTypeImpl(ExecutionStrategyType[] executionStrategyTypes, boolean isProvidingTeamOversight) {
		this.executionStrategyTypes = executionStrategyTypes;
		this.isProvidingTeamOversight = isProvidingTeamOversight;
	}

	/*
	 * ================ ExecutiveType =====================
	 */

	@Override
	public ExecutionStrategyType[] getExecutionStrategyTypes() {
		return this.executionStrategyTypes;
	}

	@Override
	public boolean isProvidingTeamOversight() {
		return this.isProvidingTeamOversight;
	}

}
