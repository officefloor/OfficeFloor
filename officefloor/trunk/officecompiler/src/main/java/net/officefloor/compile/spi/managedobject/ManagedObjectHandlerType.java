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
package net.officefloor.compile.spi.managedobject;

import net.officefloor.compile.spi.handler.HandlerType;
import net.officefloor.frame.api.execute.Handler;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * <code>Type definition</code> of a {@link Handler} used by the
 * {@link ManagedObject}.
 * 
 * @author Daniel
 */
public interface ManagedObjectHandlerType<H extends Enum<H>> {

	/**
	 * Obtains the name of the {@link Handler}.
	 * 
	 * @return Name of the {@link Handler}.
	 */
	String getHandlerName();

	/**
	 * Obtains the key identifying this {@link Handler}.
	 * 
	 * @return Key identifying this {@link Handler}.
	 */
	H getKey();

	/**
	 * Obtains the required type of the {@link Handler}.
	 * 
	 * @return Required type of the {@link Handler}. Must return a value if a
	 *         {@link Handler} is not provided by the
	 *         {@link ManagedObjectSource}, otherwise may be <code>null</code>.
	 */
	Class<?> getRequiredHandlerType();

	/**
	 * Obtains the {@link HandlerType} of the {@link Handler} provided by the
	 * {@link ManagedObjectSource}.
	 * 
	 * @return {@link HandlerType} of the {@link Handler} provided by the
	 *         {@link ManagedObjectSource}. Will be <code>null</code> if
	 *         {@link Handler} is not provided by the
	 *         {@link ManagedObjectSource}.
	 */
	HandlerType<?> getProvidedHandlerType();

}