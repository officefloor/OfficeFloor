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
import net.officefloor.activity.procedure.ProcedureProperty;

/**
 * Builds the specification for the {@link Procedure}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ProcedureSpecification {

	/**
	 * Adds a property.
	 * 
	 * @param name Name of property that is also used as the label.
	 */
	void addProperty(String name);

	/**
	 * Adds a property.
	 * 
	 * @param name  Name of property.
	 * @param label Label for the property.
	 */
	void addProperty(String name, String label);

	/**
	 * Adds a property.
	 * 
	 * @param property {@link ProcedureProperty}.
	 */
	void addProperty(ProcedureProperty property);

}
