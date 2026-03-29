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

package net.officefloor.compile.state.autowire;

import net.officefloor.frame.api.manage.Office;

/**
 * Visitor for the {@link AutoWireStateManager} of each {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AutoWireStateManagerVisitor {

	/**
	 * Visits the {@link AutoWireStateManagerFactory} for the {@link Office}.
	 * 
	 * @param officeName                  Name of the {@link Office}.
	 * @param autoWireStateManagerFactory {@link AutoWireStateManagerFactory}.
	 * @throws Exception If fails to visit the {@link AutoWireStateManagerFactory}.
	 */
	void visit(String officeName, AutoWireStateManagerFactory autoWireStateManagerFactory) throws Exception;

}
