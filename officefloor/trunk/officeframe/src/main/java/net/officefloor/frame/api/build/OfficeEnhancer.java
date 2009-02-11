/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.frame.api.build;

import net.officefloor.frame.api.execute.Handler;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * <p>
 * Enables enhancing the {@link Office}.
 * <p>
 * This enables:<il>
 * <li>linking in the {@link Task} instances created by the
 * {@link ManagedObjectSource}</li>
 * <li>providing {@link Handler} instances for a {@link ManagedObjectSource}</li>
 * <li>linking in {@link Task} instances for the {@link Handler} instances</li>
 * </il>
 * 
 * @author Daniel
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
