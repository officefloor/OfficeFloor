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
package net.officefloor.plugin.objects;

import net.officefloor.autowire.AutoWireApplication;
import net.officefloor.model.repository.ConfigurationItem;

/**
 * Context for the {@link AutoWireObjectsLoader}.
 *
 * @author Daniel Sagenschneider
 */
public interface AutoWireObjectsLoaderContext {

	/**
	 * Obtains the {@link ConfigurationItem} containing the configuration of the
	 * objects.
	 * 
	 * @return {@link ConfigurationItem} containing the configuration of the
	 *         objects.
	 */
	ConfigurationItem getConfiguration();

	/**
	 * Obtains the {@link AutoWireApplication} to be configured with the
	 * objects.
	 * 
	 * @return {@link AutoWireApplication} to be configured with the objects.
	 */
	AutoWireApplication getAutoWireApplication();

	/**
	 * <p>
	 * This is available to report invalid configuration but continue to deploy
	 * the remaining object instances.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 * @throws Exception
	 *             If needs to propagate the issue.
	 */
	void addIssue(String issueDescription) throws Exception;

}