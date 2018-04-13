/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.eclipse.configurer;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.scene.Node;

/**
 * Configured configuration.
 * 
 * @author Daniel Sagenschneider
 */
public interface Configuration {

	/**
	 * Obtains the {@link Node} containing the configuration.
	 * 
	 * @return {@link Node} containing the configuration.
	 */
	Node getConfigurationNode();

	/**
	 * Obtains the title.
	 * 
	 * @return Title.
	 */
	String getTitle();

	/**
	 * Indicates if the configuration is valid.
	 * 
	 * @return {@link ReadOnlyBooleanProperty} to indicate if the configuration is
	 *         valid.
	 */
	ReadOnlyBooleanProperty validProperty();

	/**
	 * <p>
	 * Obtains the {@link Actioner} for the configuration
	 * <p>
	 * Should this be invoked, it is assumed the {@link Actioner} will be triggered
	 * externally to the configuration.
	 * 
	 * @return {@link Actioner} for the configuration.
	 */
	Actioner getActioner();

}