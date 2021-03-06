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

package net.officefloor.frame.api.managedobject.source;

import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Meta-data of the {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectSourceMetaData<O extends Enum<O>, F extends Enum<F>> {

	/**
	 * <p>
	 * Obtains the {@link Class} of the {@link ManagedObject} instances from the
	 * {@link ManagedObjectSource}.
	 * <p>
	 * This is to enable coupled configuration rather than specifying in a possibly
	 * unrelated configuration file.
	 * <p>
	 * Note this does not prevent the configuration passed to the
	 * {@link ManagedObjectSource#init(ManagedObjectSourceContext)} method to
	 * specify this. {@link Class} must however be the same given the same
	 * configuration.
	 * 
	 * @return {@link Class} of the {@link ManagedObject}.
	 */
	Class<? extends ManagedObject> getManagedObjectClass();

	/**
	 * <p>
	 * Obtains the {@link Class} of the object returned from
	 * {@link ManagedObject#getObject()}.
	 * <p>
	 * This is to enable coupled configuration rather than specifying in a possibly
	 * unrelated configuration file.
	 * <p>
	 * Note this does not prevent the configuration passed to the
	 * {@link ManagedObjectSource#init(ManagedObjectSourceContext)} method to
	 * specify this. {@link Class} must however be the same given the same
	 * configuration.
	 * 
	 * @return The {@link Class} of the object being managed by the
	 *         {@link ManagedObject}.
	 */
	Class<?> getObjectClass();

	/**
	 * Obtains the list of {@link ManagedObjectDependencyMetaData} instances should
	 * this {@link ManagedObjectSource} provide a {@link CoordinatingManagedObject}.
	 * 
	 * @return Meta-data of the required dependencies for this
	 *         {@link ManagedObjectSource}.
	 */
	ManagedObjectDependencyMetaData<O>[] getDependencyMetaData();

	/**
	 * Obtains the list of {@link ManagedObjectFlowMetaData} instances should this
	 * {@link ManagedObjectSource} require instigating a {@link Flow}.
	 * 
	 * @return Meta-data of {@link Flow} instances instigated by this
	 *         {@link ManagedObjectSource}.
	 */
	ManagedObjectFlowMetaData<F>[] getFlowMetaData();

	/**
	 * Obtains the list of {@link ManagedObjectExecutionMetaData} instances should
	 * the {@link ManagedObjectSource} require {@link ExecutionStrategy}.
	 * 
	 * @return Meta-data of {@link ExecutionStrategy} instances required by this
	 *         {@link ManagedObjectSource}.
	 */
	ManagedObjectExecutionMetaData[] getExecutionMetaData();

	/**
	 * Obtains the meta-data regarding the extension interfaces that this
	 * {@link ManagedObject} implements.
	 * 
	 * @return Meta-data regarding the extension interfaces that this
	 *         {@link ManagedObject} implements.
	 */
	ManagedObjectExtensionMetaData<?>[] getExtensionInterfacesMetaData();

}
