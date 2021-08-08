/*-
 * #%L
 * Procedure
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

package net.officefloor.activity.procedure.spi;

import java.lang.reflect.Method;

import net.officefloor.activity.procedure.Procedure;

/**
 * Source providing a {@link Procedure}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ProcedureSource {

	/**
	 * <p>
	 * Name of this source.
	 * <p>
	 * Note that this is the name used in configuration to identify this
	 * {@link ProcedureSource}. Therefore, it can not change without causing
	 * configurations to be updated.
	 * <p>
	 * The reasons for using this logical name over {@link Class} names is:
	 * <ul>
	 * <li>Class names can be quite long</li>
	 * <li>Class names are not easily readable</li>
	 * <li>Enables swapping plugins for same logical service name</li>
	 * </ul>
	 * 
	 * @return Name of this source.
	 */
	String getSourceName();

	/**
	 * Provides the available {@link Procedure} instances for the resource.
	 * 
	 * @param context {@link ProcedureListContext}.
	 * @throws Exception If fails to list {@link Procedure} instances.
	 */
	void listProcedures(ProcedureListContext context) throws Exception;

	/**
	 * Loads the {@link Method} for the {@link Procedure}.
	 * 
	 * @param context {@link ProcedureMethodContext}.
	 * @return {@link Method} for the {@link ProcedureSource}.
	 * @throws Exception If fails to load the method.
	 */
	Method loadMethod(ProcedureMethodContext context) throws Exception;

}
