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

package net.officefloor.frame.api.managedobject.function;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionDependency;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.team.Team;

/**
 * Context for the {@link ManagedObjectFunctionEnhancer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectFunctionEnhancerContext {

	/**
	 * Obtains the name of the {@link ManagedFunction}.
	 * 
	 * @return Name of the {@link ManagedFunction}.
	 */
	String getFunctionName();

	/**
	 * Obtains the {@link ManagedFunctionFactory}.
	 * 
	 * @return {@link ManagedFunctionFactory}.
	 */
	ManagedFunctionFactory<?, ?> getManagedFunctionFactory();

	/**
	 * Indicates if using the {@link ManagedObject} from the
	 * {@link ManagedObjectSource}.
	 * 
	 * @return <code>true</code> if using the {@link ManagedObject} from the
	 *         {@link ManagedObjectSource}.
	 */
	boolean isUsingManagedObject();

	/**
	 * Obtains the {@link ManagedObjectFunctionDependency} instances for the
	 * {@link ManagedFunction}.
	 * 
	 * @return {@link ManagedObjectFunctionDependency} instances for the
	 *         {@link ManagedFunction}.
	 */
	ManagedObjectFunctionDependency[] getFunctionDependencies();

	/**
	 * Obtains the name of the responsible {@link Team}.
	 * 
	 * @return Name of the responsible {@link Team} or <code>null</code> if
	 *         {@link Team} assigned.
	 */
	String getResponsibleTeam();

	/**
	 * Specifies the responsible {@link Team}.
	 * 
	 * @param teamName Name of the responsible {@link Team}.
	 */
	void setResponsibleTeam(String teamName);

}
