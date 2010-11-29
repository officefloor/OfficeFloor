/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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
package net.officefloor.plugin.value.loader;

import java.util.HashMap;
import java.util.Map;

/**
 * Root {@link StatelessValueLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class RootStatelessValueLoader extends AbstractStatelessValueLoader {

	/**
	 * {@link StatelessValueLoader} instances by property names.
	 */
	private Map<String, StatelessValueLoader> valueLoaders = new HashMap<String, StatelessValueLoader>();

	/**
	 * {@link NameTranslator}.
	 */
	private final NameTranslator translator;

	/**
	 * Initiate.
	 * 
	 * @param valueLoaders
	 *            {@link StatelessValueLoader} instances by property names.
	 * @param translator
	 *            {@link NameTranslator}.
	 */
	public RootStatelessValueLoader(
			Map<String, StatelessValueLoader> valueLoaders,
			NameTranslator translator) {
		this.valueLoaders = valueLoaders;
		this.translator = translator;
	}

	/*
	 * ==================== ValueLoader ==========================
	 */

	@Override
	protected void loadValue(Object object, String propertyName,
			String remainingName, String value, Object[] state)
			throws Exception {

		// Transform the property name for comparison
		propertyName = this.translator.translate(propertyName);

		// Obtain the value loader for the property name
		StatelessValueLoader valueLoader = this.valueLoaders.get(propertyName);
		if (valueLoader == null) {
			return; // no value loader for property
		}

		// Load the value
		valueLoader.loadValue(object, remainingName, value, state);
	}

}