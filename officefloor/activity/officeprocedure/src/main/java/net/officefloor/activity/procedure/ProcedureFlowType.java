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

import net.officefloor.frame.internal.structure.Flow;

/**
 * <code>Type definition</code> of a {@link Flow} possibly instigated by a
 * {@link Procedure}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ProcedureFlowType {

	/**
	 * Obtains the name for the {@link ProcedureFlowType}.
	 * 
	 * @return Name for the {@link ProcedureFlowType}.
	 */
	String getFlowName();

	/**
	 * Obtains the type of the argument passed by the {@link Procedure} to the
	 * {@link Flow}.
	 * 
	 * @return Type of argument passed to {@link Flow}. May be <code>null</code> to
	 *         indicate no argument.
	 */
	Class<?> getArgumentType();

}
