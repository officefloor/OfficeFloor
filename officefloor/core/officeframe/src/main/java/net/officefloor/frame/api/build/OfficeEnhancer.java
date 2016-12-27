/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.api.build;

import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * <p>
 * Enables enhancing the {@link Office}.
 * <p>
 * This enables:
 * <ol>
 * <li>linking in the {@link ManagedFunction} instances created by the
 * {@link ManagedObjectSource} to other {@link ManagedFunction} instances within
 * the {@link Office}</li>
 * <li>linking the {@link Flow} instances instigated by the
 * {@link ManagedObjectSource} to a {@link ManagedFunction} instances within the
 * {@link Office}</li>
 * </ol>
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeEnhancer {

	/**
	 * Enhances the {@link Office}.
	 * 
	 * @param context
	 *            {@link OfficeEnhancerContext}.
	 */
	void enhanceOffice(OfficeEnhancerContext context);

}