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

package net.officefloor.test;

import net.officefloor.compile.state.autowire.AutoWireStateManager;

/**
 * Context for the {@link TestDependencyService}.
 * 
 * @author Daniel Sagenschneider
 */
public interface TestDependencyServiceContext {

	/**
	 * Obtains the qualifier of required dependency.
	 * 
	 * @return Qualifier of required dependency. May be <code>null</code>.
	 */
	String getQualifier();

	/**
	 * Obtains the type of required dependency.
	 * 
	 * @return Type of required dependency. May be <code>null</code>.
	 */
	Class<?> getObjectType();

	/**
	 * Obtains the {@link AutoWireStateManager}.
	 * 
	 * @return {@link AutoWireStateManager}.
	 */
	AutoWireStateManager getStateManager();

	/**
	 * Obtains the load timeout for the {@link AutoWireStateManager}.
	 * 
	 * @return Load timeout for the {@link AutoWireStateManager}.
	 */
	long getLoadTimeout();

}
