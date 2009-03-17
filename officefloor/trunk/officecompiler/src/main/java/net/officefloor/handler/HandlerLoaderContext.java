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
package net.officefloor.handler;

import net.officefloor.model.handler.HandlerModel;
import net.officefloor.repository.ConfigurationContext;

/**
 * Context for the {@link HandlerLoader}.
 * 
 * @author Daniel
 */
public interface HandlerLoaderContext {

	/**
	 * Obtains the configuration string.
	 * 
	 * @return Configuration string.
	 */
	String getConfiguration();

	/**
	 * Obtains the {@link ConfigurationContext}.
	 * 
	 * @return {@link ConfigurationContext}.
	 */
	ConfigurationContext getConfigurationContext();

	/**
	 * Obtains the {@link ClassLoader} for loading the {@link HandlerModel}.
	 * 
	 * @return {@link ClassLoader} for loading the {@link HandlerModel}.
	 */
	ClassLoader getClassLoader();

}