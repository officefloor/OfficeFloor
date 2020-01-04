/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.compile.properties;

import java.util.Comparator;
import java.util.Properties;

/**
 * Listing of {@link Property} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface PropertyList extends Iterable<Property> {

	/**
	 * Appends a {@link Property} to this {@link PropertyList}.
	 * 
	 * @param name
	 *            Name of the {@link Property}.
	 * @param label
	 *            Label of the {@link Property}. Should this be blank it will be
	 *            defaulted to the name.
	 * @return {@link Property} added.
	 */
	Property addProperty(String name, String label);

	/**
	 * Appends a {@link Property} to this {@link PropertyList}.
	 * 
	 * @param name
	 *            Name of the {@link Property} which is also used as the label.
	 * @return {@link Property} added.
	 */
	Property addProperty(String name);

	/**
	 * Removes the {@link Property} from this {@link PropertyList}.
	 * 
	 * @param property
	 *            {@link Property} to be removed.
	 */
	void removeProperty(Property property);

	/**
	 * Obtains the names of the {@link Property} instances in the order they
	 * were added.
	 * 
	 * @return Names of the {@link Property} instances.
	 */
	String[] getPropertyNames();

	/**
	 * Obtains the first {@link Property} by the input name.
	 * 
	 * @param name
	 *            Name of the {@link Property} to return.
	 * @return First {@link Property} by the input name, or <code>null</code> if
	 *         no {@link Property} by the name.
	 */
	Property getProperty(String name);

	/**
	 * Convenience method that attempts to get the {@link Property} and if not
	 * found adds the {@link Property}.
	 * 
	 * @param name
	 *            Name of the {@link Property} to return.
	 * @return First {@link Property} by the input name or a newly added
	 *         {@link Property} if no {@link Property} found by the name.
	 */
	Property getOrAddProperty(String name);

	/**
	 * Convenience method to obtain the {@link Property} value.
	 * 
	 * @param name
	 *            Name of the {@link Property} to obtain its value.
	 * @param defaultValue
	 *            Default value should the {@link Property} not exist or have
	 *            blank value.
	 * @return Value for the {@link Property} (or <code>defaultValue</code> if
	 *         not available).
	 */
	String getPropertyValue(String name, String defaultValue);

	/**
	 * Obtains the {@link Properties} populated with the {@link Property}
	 * values.
	 * 
	 * @return Populated {@link Properties}.
	 */
	Properties getProperties();

	/**
	 * Clears the {@link PropertyList}.
	 */
	void clear();

	/**
	 * Enable sorting the {@link Property} instances within this
	 * {@link PropertyList}.
	 * 
	 * @param comparator
	 *            {@link Comparator} to provide comparisons for sorting.
	 */
	void sort(Comparator<? super Property> comparator);

	/**
	 * <p>
	 * Normalises the {@link Property} instances.
	 * <p>
	 * This will remove:
	 * <ol>
	 * <li>any {@link Property} with a blank name</li>
	 * <li>any {@link Property} with a blank value</li>
	 * <li>duplicate {@link Property} instances by the same name (keeps the
	 * first {@link Property})</li>
	 * </ol>
	 */
	void normalise();

	/**
	 * Loads the {@link Property} values of this {@link PropertyList} to
	 * {@link PropertyConfigurable}.
	 * 
	 * @param configurable
	 *            {@link PropertyConfigurable} to be configured with the
	 *            {@link Property} values.
	 */
	void configureProperties(PropertyConfigurable configurable);

}
