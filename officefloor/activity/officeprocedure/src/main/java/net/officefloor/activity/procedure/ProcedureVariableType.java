/*-
 * #%L
 * Procedure
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.activity.procedure;

import net.officefloor.plugin.variable.Var;

/**
 * <code>Type definition</code> of {@link Var} required by the
 * {@link Procedure}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ProcedureVariableType {

	/**
	 * Obtains the name for the {@link Var}.
	 * 
	 * @return Name for the {@link Var}.
	 */
	String getVariableName();

	/**
	 * Obtains the type of the {@link Var}.
	 * 
	 * @return Type of the {@link Var}.
	 */
	String getVariableType();

}
