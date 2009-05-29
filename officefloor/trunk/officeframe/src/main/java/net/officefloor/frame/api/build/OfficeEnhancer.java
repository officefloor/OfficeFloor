/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.frame.api.build;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * <p>
 * Enables enhancing the {@link Office}.
 * <p>
 * This enables:
 * <ol>
 * <li>linking in the {@link Task} instances created by the
 * {@link ManagedObjectSource} to other {@link Task} instances within the
 * {@link Office}</li>
 * <li>linking the {@link Flow} instances instigated by the
 * {@link ManagedObjectSource} to a {@link Task} within the {@link Office}</li>
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