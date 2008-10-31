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
package net.officefloor.eclipse.extension.workloader;

import net.officefloor.eclipse.extension.ExtensionUtil;
import net.officefloor.work.WorkLoader;

/**
 * Interface for extension to provide enriched {@link WorkLoader} usage.
 * 
 * @author Daniel
 */
public interface WorkLoaderExtension {

	/**
	 * Extension ID.
	 */
	public static final String EXTENSION_ID = ExtensionUtil
			.getExtensionId("workloader");

	/**
	 * Obtains the class of the {@link WorkLoader} being enriched in its usage.
	 * 
	 * @return Class of the {@link WorkLoader} being enriched in its usage.
	 */
	Class<? extends WorkLoader> getWorkLoaderClass();

	/**
	 * Obtains the display name. This is a descriptive name that can be used
	 * other than the fully qualified name of the {@link WorkLoader}.
	 * 
	 * @return Display name.
	 */
	String getDisplayName();
}
