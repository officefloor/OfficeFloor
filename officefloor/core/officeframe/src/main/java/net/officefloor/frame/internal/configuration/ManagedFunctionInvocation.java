/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.internal.configuration;

import net.officefloor.frame.api.function.ManagedFunction;

/**
 * Provides reference details to invoke a {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionInvocation extends ManagedFunctionReference {

	/**
	 * Obtains the argument to invoke the {@link ManagedFunction}.
	 * 
	 * @return Argument to invoke the {@link ManagedFunction}.
	 */
	Object getArgument();

	/*
	 * ====================== ManagedFunctionReference ==================
	 */

	@Override
	default Class<?> getArgumentType() {

		// Provide argument type from the argument
		Object argument = this.getArgument();
		return argument != null ? argument.getClass() : null;
	}

}
