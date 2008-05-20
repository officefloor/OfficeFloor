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
package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.extension.ExtensionInterfaceFactory;

/**
 * Meta-data of the Extension Interface.
 * 
 * @author Daniel
 */
public interface ExtensionInterfaceMetaData<I extends Object> {

	/**
	 * Obtains the index to identify the {@link ManagedObject} within the
	 * {@link Work} to create the extension interface from.
	 * 
	 * @return Index to identify the {@link ManagedObject} to create the
	 *         extension interface from.
	 */
	int getManagedObjectIndex();

	/**
	 * Obtains the factory to create the Extension Interface for the
	 * {@link ManagedObject}.
	 * 
	 * @return Factory to create the Extension Interface for the
	 *         {@link ManagedObject}.
	 */
	ExtensionInterfaceFactory<I> getExtensionInterfaceFactory();

}
