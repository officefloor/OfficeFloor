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

package net.officefloor.frame.api.build;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.thread.OptionalThreadLocal;

/**
 * <p>
 * Provides additional means to obtain the {@link ManagedObject} from
 * {@link ThreadLocal}.
 * <p>
 * This is typically used for integrating third party libraries that expect to
 * obtain objects from {@link ThreadLocal} state.
 * 
 * @author Daniel Sagenschneider
 */
public interface ThreadDependencyMappingBuilder extends DependencyMappingBuilder {

	/**
	 * Obtains the {@link OptionalThreadLocal} for the {@link ManagedObject}.
	 * 
	 * @param <T> Type of object.
	 * @return {@link OptionalThreadLocal} for the {@link ManagedObject}.
	 */
	<T> OptionalThreadLocal<T> getOptionalThreadLocal();

}
