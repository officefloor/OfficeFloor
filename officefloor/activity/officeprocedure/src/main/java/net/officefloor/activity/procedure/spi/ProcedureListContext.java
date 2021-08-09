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

import net.officefloor.activity.procedure.Procedure;
import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.source.SourceContext;

/**
 * Context for listing the {@link Procedure} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface ProcedureListContext {

	/**
	 * Obtains the resource to list the {@link Procedure} instances.
	 * 
	 * @return Resource to list the {@link Procedure} instances.
	 */
	String getResource();

	/**
	 * Adds an available {@link Procedure}.
	 * 
	 * @param procedureName Name of the {@link Procedure}. May be <code>null</code>
	 *                      to indicate for manual selection of {@link Procedure}.
	 * @return {@link ProcedureSpecification} to detail requirements for the
	 *         {@link Procedure}.
	 */
	ProcedureSpecification addProcedure(String procedureName);

	/**
	 * <p>
	 * Obtains the {@link SourceContext}.
	 * <p>
	 * Note that the {@link Property} values will not be available from this
	 * {@link SourceContext}. It is typically only provided to enable to load
	 * resource as a {@link Class}.
	 * 
	 * @return {@link SourceContext}.
	 */
	SourceContext getSourceContext();

}
