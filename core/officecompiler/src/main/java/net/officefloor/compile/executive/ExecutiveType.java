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

package net.officefloor.compile.executive;

import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.TeamOversight;

/**
 * <code>Type definition</code> of an {@link Executive}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ExecutiveType {

	/**
	 * Obtains the {@link ExecutionStrategyType} definitions for the
	 * {@link ExecutionStrategy} instances available from the {@link Executive}.
	 * 
	 * @return {@link ExecutionStrategyType} definitions for the
	 *         {@link ExecutionStrategy} instances available from the
	 *         {@link Executive}.
	 */
	ExecutionStrategyType[] getExecutionStrategyTypes();

	/**
	 * Indicates if provides {@link TeamOversight}.
	 * 
	 * @return <code>true</code> if provides {@link TeamOversight}.
	 */
	boolean isProvidingTeamOversight();

}
