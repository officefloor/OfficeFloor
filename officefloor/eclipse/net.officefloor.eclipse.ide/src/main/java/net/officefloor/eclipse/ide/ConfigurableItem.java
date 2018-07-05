/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.eclipse.ide;

/**
 * Configurable Item.
 * 
 * @author Daniel Sagenschneider
 */
public interface ConfigurableItem<I> {

	/**
	 * Obtains the item to be configured.
	 * 
	 * @return Item to be configured.
	 */
	// I getItem();

	/**
	 * Loads the refactor configuration for the item.
	 * 
	 * @param builder
	 *            {@link ConfigurationBuilder} to receive the configuration.
	 */
	// void loadRefactorConfiguration(ConfigurationBuilder<I> builder);

	/**
	 * Indicates whether matches the input search text.
	 * 
	 * @param searchText
	 *            Search text to match against.
	 * @return <code>true</code> if item matches the search text.
	 */
	// boolean isMatchSearch(String searchText);

}