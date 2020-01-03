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

package net.officefloor.frame.api.managedobject;

import java.util.logging.Logger;

/**
 * Context for the {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectContext {

	/**
	 * <p>
	 * Obtains the name this {@link ManagedObject} is bound under.
	 * <p>
	 * This is useful to have a unique name identifying the {@link ManagedObject}.
	 * 
	 * @return Name this {@link ManagedObject} is bound under.
	 */
	String getBoundName();

	/**
	 * Obtains the {@link Logger} for the {@link ManagedObject}.
	 * 
	 * @return {@link Logger} for the {@link ManagedObject}.
	 */
	Logger getLogger();

	/**
	 * Undertakes a {@link ProcessSafeOperation}.
	 * 
	 * @param <R>       Return type from operation
	 * @param <T>       Possible {@link Throwable} type from operation.
	 * @param operation {@link ProcessSafeOperation}.
	 * @return Return value.
	 * @throws T Possible {@link Throwable}.
	 */
	<R, T extends Throwable> R run(ProcessSafeOperation<R, T> operation) throws T;

}
