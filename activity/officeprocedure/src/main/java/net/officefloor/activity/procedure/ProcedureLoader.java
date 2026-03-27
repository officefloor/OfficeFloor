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

package net.officefloor.activity.procedure;

import net.officefloor.activity.procedure.section.ProcedureManagedFunctionSource;
import net.officefloor.activity.procedure.spi.ProcedureSource;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.PropertyList;

/**
 * Loader for {@link ProcedureManagedFunctionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ProcedureLoader {

	/**
	 * Lists the available {@link Procedure} instances from the {@link Class}.
	 * 
	 * @param resource Resource.
	 * @return Listing of available {@link Procedure} instances or <code>null</code>
	 *         with issues reported to {@link CompilerIssues}.
	 */
	Procedure[] listProcedures(String resource);

	/**
	 * Loads the {@link ProcedureType} for the {@link Procedure}.
	 * 
	 * @param resource      Resource.
	 * @param sourceName    {@link ProcedureSource} name.
	 * @param procedureName {@link Procedure} name.
	 * @param properties    {@link PropertyList}.
	 * @return {@link ProcedureType} for the {@link Procedure} or <code>null</code>
	 *         with issues reported to {@link CompilerIssues}.
	 */
	ProcedureType loadProcedureType(String resource, String sourceName, String procedureName, PropertyList properties);

}
