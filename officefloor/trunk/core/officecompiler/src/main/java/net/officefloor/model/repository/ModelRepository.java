/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.model.repository;

import net.officefloor.model.Model;
import net.officefloor.plugin.xml.unmarshall.designate.DesignateXmlUnmarshaller;

/**
 * Repository to the {@link Model} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface ModelRepository {

	/**
	 * Creates the {@link ConfigurationItem} to hold the {@link Model}.
	 * 
	 * @param location
	 *            Relative location within the {@link ConfigurationContext} to
	 *            create the {@link ConfigurationItem}.
	 * @param model
	 *            {@link Model} to be stored in the created
	 *            {@link ConfigurationItem}.
	 * @param context
	 *            {@link ConfigurationContext}.
	 * @return {@link ConfigurationItem} created for the {@link Model}.
	 * @throws Exception
	 *             If fails to create the {@link ConfigurationItem}.
	 */
	ConfigurationItem create(String location, Object model,
			ConfigurationContext context) throws Exception;

	/**
	 * Stores the {@link Model} within the {@link ConfigurationItem}.
	 * 
	 * @param model
	 *            {@link Model} to be stored.
	 * @param configuration
	 *            {@link ConfigurationItem} to contain the {@link Model}.
	 * @throws Exception
	 *             If fails to store the {@link Model}.
	 */
	void store(Object model, ConfigurationItem configuration) throws Exception;

	/**
	 * Configures the {@link Model} from the {@link ConfigurationItem}.
	 * 
	 * @param model
	 *            {@link Model} to be configured.
	 * @param configuration
	 *            {@link ConfigurationItem} containing configuration of the
	 *            {@link Model}.
	 * @return Configured {@link Model}.
	 * @throws Exception
	 *             If fails to configure the {@link Model}.
	 */
	<O> O retrieve(O model, ConfigurationItem configuration) throws Exception;

	/**
	 * Registers meta-data for a {@link Model} to be retrieved.
	 * 
	 * @param modelType
	 *            {@link Class} of the {@link Model}.
	 * @throws Exception
	 *             If fails to configure the {@link DesignateXmlUnmarshaller}.
	 */
	void registerModel(Class<?> modelType) throws Exception;

	/**
	 * <p>
	 * Retrieves the {@link Model} from the {@link ConfigurationItem}.
	 * <p>
	 * Only {@link Model} instances successfully registered by
	 * {@link #registerModel(Class)} may be retrieved by this method.
	 * 
	 * @param configuration
	 *            {@link ConfigurationItem} containing configuration of the
	 *            {@link Model}.
	 * @return {@link Model} for the {@link ConfigurationItem}.
	 * @throws Exception
	 *             If fails to retrieve the {@link Model}.
	 */
	Object retrieve(ConfigurationItem configuration) throws Exception;

}