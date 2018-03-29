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

import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Function;

import net.officefloor.compile.properties.PropertyList;

/**
 * Builds the configuration.
 * 
 * @author Daniel Sagenschneider
 */
public interface ConfigurationBuilder<M> extends ItemBuilder<M> {

	/**
	 * Builds choices in configuration.
	 * 
	 * @param label
	 *            Label for the choices.
	 * @return {@link ChoiceBuilder}.
	 */
	ChoiceBuilder<M> choices(String label);

	/**
	 * Configures a list of items.
	 * 
	 * @param label
	 *            Label for the items.
	 * @param itemClass
	 *            {@link Class} of the item.
	 * @return {@link ItemBuilder}.
	 */
	<I> ItemBuilder<I> list(String label, Class<I> itemClass);

	/**
	 * Configures {@link PropertyList}.
	 * 
	 * @param label
	 *            Label for the {@link Properties}.
	 * @return {@link PropertiesBuilder}.
	 */
	PropertiesBuilder<M> properties(String label);

	/**
	 * Configures a mapping of name to name.
	 * 
	 * @param label
	 *            Label for the mapping.
	 * @param getMappedItems
	 *            {@link Function} to extract the mapping from the model.
	 */
	void map(String label, Function<M, List<String>> getMappedItems);

	/**
	 * Configures a {@link Consumer} to apply the configured model.
	 * 
	 * @param applier
	 *            {@link Consumer} to apply the configured model.
	 */
	void apply(Consumer<M> applier);

}