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
package net.officefloor.model.repository;

import java.io.InputStream;

/**
 * Context of the {@link net.officefloor.model.repository.persistence.ConfigurationItem}
 * instances.
 * 
 * @author Daniel
 */
public interface ConfigurationContext {

	/**
	 * Obtains the Identifier for this configuration context. This value is used
	 * in the equality of configuration contexts.
	 * 
	 * @return Identifier for this configuration context.
	 */
	String getId();

	/**
	 * Obtains the class path for the Context.
	 * 
	 * @return Class path for the Context.
	 */
	String[] getClasspath();

	/**
	 * Obtains the {@link ConfigurationItem} identified by its Id.
	 * 
	 * @param id
	 *            Id of the {@link ConfigurationItem} to obtain.
	 * @return {@link ConfigurationItem}.
	 * @throws Exception
	 *             If the configuration does not exist.
	 */
	ConfigurationItem getConfigurationItem(String id)
			throws Exception;

	/**
	 * Creates a new {@link ConfigurationItem} identified by the input Id.
	 * 
	 * @param id
	 *            Id of the {@link ConfigurationItem} to create.
	 * @param configuration
	 *            Configuration for the {@link ConfigurationItem}.
	 * @return The created {@link ConfigurationItem}.
	 * @throws Exception
	 *             If fails to create the {@link ConfigurationItem}.s
	 */
	ConfigurationItem createConfigurationItem(String id,
			InputStream configuration) throws Exception;
}
