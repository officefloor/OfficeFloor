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

import net.officefloor.activity.procedure.spi.ProcedureSource;

/**
 * Procedure.
 * 
 * @author Daniel Sagenschneider
 */
public interface Procedure {

	/**
	 * Obtains the name of the {@link ProcedureSource}.
	 * 
	 * @return Name of the {@link ProcedureSource}.
	 */
	String getServiceName();

	/**
	 * Obtains the name of the {@link Procedure}.
	 * 
	 * @return Name of the {@link Procedure}. May be <code>null</code> to indicate
	 *         manually selected.
	 */
	String getProcedureName();

	/**
	 * Obtains the specification of properties for the {@link Procedure}.
	 * 
	 * @return Property specification.
	 */
	ProcedureProperty[] getProperties();

	/**
	 * Determines if the {@link Procedure}.
	 * 
	 * @param serviceName   Service name.
	 * @param procedureName Name of {@link Procedure}. May be <code>null</code> for
	 *                      manually selected.
	 * @return <code>true</code> if this {@link Procedure} matches.
	 */
	default boolean isProcedure(String serviceName, String procedureName) {
		boolean isServiceNameMatch = serviceName == null ? false : serviceName.equals(this.getServiceName());
		boolean isProcedureNameMatch = procedureName == null ? (this.getProcedureName() == null)
				: procedureName.equals(this.getProcedureName());
		return isServiceNameMatch && isProcedureNameMatch;
	}
}
