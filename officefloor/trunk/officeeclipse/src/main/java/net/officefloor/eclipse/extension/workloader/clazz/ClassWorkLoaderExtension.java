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
package net.officefloor.eclipse.extension.workloader.clazz;

import net.officefloor.eclipse.extension.workloader.WorkLoaderExtension;
import net.officefloor.work.WorkLoader;
import net.officefloor.work.clazz.ClassWorkLoader;

/**
 * {@link WorkLoaderExtension} for the {@link ClassWorkLoader}.
 * 
 * @author Daniel
 */
public class ClassWorkLoaderExtension implements WorkLoaderExtension {

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.extension.workloader.WorkLoaderExtension#
	 * getWorkLoaderClass()
	 */
	@Override
	public Class<? extends WorkLoader> getWorkLoaderClass() {
		return ClassWorkLoader.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.extension.workloader.WorkLoaderExtension#
	 * getDisplayName()
	 */
	@Override
	public String getDisplayName() {
		return "Class";
	}

}
