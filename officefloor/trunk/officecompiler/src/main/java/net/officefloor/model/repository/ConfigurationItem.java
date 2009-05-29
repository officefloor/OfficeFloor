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
package net.officefloor.model.repository;

import java.io.InputStream;

/**
 * Item of configuration within a {@link ConfigurationContext}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ConfigurationItem {

	/**
	 * <p>
	 * Obtains the relative location for this {@link ConfigurationItem} within
	 * its {@link ConfigurationContext}.
	 * <p>
	 * This value is used in the equality of {@link ConfigurationItem} instances
	 * within the {@link ConfigurationContext}.
	 * 
	 * @return Relative location for this {@link ConfigurationItem} within its
	 *         {@link ConfigurationContext}.
	 */
	String getLocation();

	/**
	 * Obtains the configuration that this represents.
	 * 
	 * @return Configuration.
	 * @throws Exception
	 *             If fails to obtain the configuration.
	 */
	InputStream getConfiguration() throws Exception;

	/**
	 * Specifies the configuration that this is to represent.
	 * 
	 * @param configuration
	 *            Configuration.
	 * @throws Exception
	 *             If fails to set the configuration.
	 */
	void setConfiguration(InputStream configuration) throws Exception;

	/**
	 * Obtains the {@link ConfigurationContext} for this
	 * {@link ConfigurationItem}.
	 * 
	 * @return {@link ConfigurationContext} for this {@link ConfigurationItem}.
	 */
	ConfigurationContext getContext();

}