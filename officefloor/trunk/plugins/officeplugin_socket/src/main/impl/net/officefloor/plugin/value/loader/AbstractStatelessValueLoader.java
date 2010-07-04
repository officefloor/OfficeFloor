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

/**
 * Abstract {@link StatelessValueLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractStatelessValueLoader implements StatelessValueLoader {

	/**
	 * Loads the value.
	 * 
	 * @param object
	 *            Object to load value on.
	 * @param propertyName
	 *            Property name.
	 * @param remainingName
	 *            Remaining name content.
	 * @param value
	 *            Value to load on Object.
	 * @param state
	 *            State of loading values to the Object graph.
	 * @throws Exception
	 *             If fails to load the value.
	 */
	protected abstract void loadValue(Object object, String propertyName,
			String remainingName, String value, Object[] state)
			throws Exception;

	/*
	 * ================== StatelessValueLoader ==========================
	 */

	@Override
	public void loadValue(Object object, String name, String value,
			Object[] state) throws Exception {

		// Parse out the property name
		int index = -1;
		SEPARATOR_FOUND: for (int i = 0; i < name.length(); i++) {
			char character = name.charAt(i);
			switch (character) {
			case '.':
			case '{':
				index = i;
				break SEPARATOR_FOUND;
			default:
				// not separator so continue to next character
			}
		}

		// Split out the property name from the name
		String propertyName;
		String remainingName;
		if (index < 0) {
			// Entire name
			propertyName = name;
			remainingName = "";
		} else {
			propertyName = name.substring(0, index);
			remainingName = name.substring(index + 1); // ignore separator
		}

		// Load the value
		this.loadValue(object, propertyName, remainingName, value, state);
	}

}