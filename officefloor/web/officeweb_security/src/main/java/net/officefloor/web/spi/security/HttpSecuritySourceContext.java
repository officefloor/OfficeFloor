/*-
 * #%L
 * Web Security
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

package net.officefloor.web.spi.security;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedObjectScope;

/**
 * Context for the {@link HttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecuritySourceContext extends SourceContext {

	/**
	 * <p>
	 * Adds a {@link HttpSecuritySupportingManagedObject}.
	 * <p>
	 * Note that the {@link ManagedObjectSource} can not invoke {@link Flow} or use
	 * {@link Team} instances. Should this be required, use the
	 * {@link HttpSecurityExecuteContext} to invoke {@link Flow} instances.
	 * 
	 * @param managedObjectName   Name of the {@link ManagedObject}.
	 * @param managedObjectSource {@link ManagedObjectSource} for the
	 *                            {@link ManagedObject}.
	 * @param managedObjectScope  {@link ManagedObjectScope} for the resulting
	 *                            {@link ManagedObject}.
	 * @return {@link HttpSecuritySupportingManagedObject} to configure the
	 *         {@link ManagedObject}.
	 */
	<O extends Enum<O>> HttpSecuritySupportingManagedObject<O> addSupportingManagedObject(String managedObjectName,
			ManagedObjectSource<O, ?> managedObjectSource, ManagedObjectScope managedObjectScope);

}
