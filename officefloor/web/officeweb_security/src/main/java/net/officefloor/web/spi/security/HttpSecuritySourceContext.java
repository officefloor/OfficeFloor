/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.web.spi.security;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.internal.structure.Flow;

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
	 * @return {@link HttpSecuritySupportingManagedObject} to configure the
	 *         {@link ManagedObject}.
	 */
	HttpSecuritySupportingManagedObject addSupportingManagedObject(String managedObjectName,
			ManagedObjectSource<?, ?> managedObjectSource);

}