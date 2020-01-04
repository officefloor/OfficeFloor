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

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Configuration of a {@link Flow} instigated by a {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectFlowConfiguration<F extends Enum<F>> {

	/**
	 * Obtains the name to identify this {@link Flow}.
	 * 
	 * @return Name identifying this {@link Flow}.
	 */
	String getFlowName();

	/**
	 * Obtains the key for this {@link Flow}.
	 * 
	 * @return Key for this flow. May be <code>null</code> if {@link Flow}
	 *         instances are {@link Indexed}.
	 */
	F getFlowKey();

	/**
	 * Obtains the {@link ManagedFunctionReference} for this {@link Flow}.
	 * 
	 * @return {@link ManagedFunctionReference} to the {@link Flow}.
	 */
	ManagedFunctionReference getManagedFunctionReference();

}
