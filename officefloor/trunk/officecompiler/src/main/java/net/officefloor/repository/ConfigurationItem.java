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
package net.officefloor.repository;

import java.io.InputStream;

/**
 * Item of configuration for the Office Floor.
 * 
 * @author Daniel
 */
public interface ConfigurationItem {

	/**
	 * Obtains the Identifier for this configuration item. This value is used in
	 * the equality of configuration items.
	 * 
	 * @return Identifier for this configuration item.
	 */
	String getId();

	/**
	 * Obtains the configuration that this represents.
	 * 
	 * @return Configuration.
	 * @throws Exception
	 *             If fails to obtain the configuration.
	 */
	InputStream getConfiguration() throws Exception;

	/**
	 * Specifies the configuration that this represents.
	 * 
	 * @param configuration
	 *            Configuration.
	 * @throws Exception
	 *             If fails to set the configuration.
	 */
	void setConfiguration(InputStream configuration) throws Exception;

	/**
	 * Obtains the context for this configuration.
	 * 
	 * @return Context of this configuration.
	 */
	ConfigurationContext getContext();
}
