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
package net.officefloor.web.value.load;

/**
 * Key for matching property names.
 * 
 * @author Daniel Sagenschneider
 */
public class PropertyKey {

	/**
	 * Converts character to lower case.
	 * 
	 * @param character
	 *            Character.
	 * @return Lower case character.
	 */
	private static char toLower(char character) {
		return Character.toLowerCase(character);
	}

	/**
	 * Name of the property.
	 */
	private final String propertyName;

	/**
	 * Indicates if case insensitive match.
	 */
	private final boolean isCaseInsensitive;

	/**
	 * Initiate.
	 * 
	 * @param propertyName
	 *            Name of the property.
	 * @param isCaseInsensitive
	 *            Indicates if case insensitive match.
	 */
	public PropertyKey(String propertyName, boolean isCaseInsensitive) {
		this.propertyName = propertyName;
		this.isCaseInsensitive = isCaseInsensitive;
	}

	/*
	 * ================== PropertyKey ======================
	 */

	@Override
	public int hashCode() {

		// Always hash case insensitive and allow equal to match
		int hash = 0;
		for (int i = 0; i < this.propertyName.length(); i++) {
			hash += toLower(this.propertyName.charAt(i));
			hash *= 13;
		}
		return hash;
	}

	@Override
	public boolean equals(Object obj) {

		// Determine if same
		if (this == obj) {
			return true;
		}

		// Ensure appropriate type
		if (!(obj instanceof PropertyKey)) {
			return false;
		}
		PropertyKey that = (PropertyKey) obj;

		// Ensure same length
		if (this.propertyName.length() != that.propertyName.length()) {
			return false;
		}

		// Handle based on whether case sensitive
		if (this.isCaseInsensitive) {
			// Ensure characters are the same insensitively
			boolean isKey = false;
			for (int i = 0; i < this.propertyName.length(); i++) {
				char thisChar = this.propertyName.charAt(i);
				char thatChar = that.propertyName.charAt(i);

				// Only key characters are case sensitive
				if (!isKey) {
					thisChar = toLower(thisChar);
					thatChar = toLower(thatChar);
				}

				// Must match
				if (thisChar != thatChar) {
					return false; // not match
				}

				// Determine if key (or not)
				if ((!isKey) && (thisChar == '{')) {
					// Next character is key
					isKey = true;
				} else if ((isKey) && (thisChar == '}')) {
					// Next character no longer a key
					isKey = false;
				}
			}

		} else {
			// Ensure characters are the same exactly
			for (int i = 0; i < this.propertyName.length(); i++) {
				char thisChar = this.propertyName.charAt(i);
				char thatChar = that.propertyName.charAt(i);

				// Must match
				if (thisChar != thatChar) {
					return false; // not match
				}
			}
		}

		// As here must match
		return true;
	}

}