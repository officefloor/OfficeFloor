/*-
 * #%L
 * Web Plug-in
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
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
	 * @param character Character.
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
	 * @param propertyName      Name of the property.
	 * @param isCaseInsensitive Indicates if case insensitive match.
	 */
	public PropertyKey(String propertyName, boolean isCaseInsensitive) {
		this.propertyName = propertyName;
		this.isCaseInsensitive = isCaseInsensitive;
	}

	/**
	 * Obtains the property name.
	 * 
	 * @return Property name.
	 */
	public String getPropertyName() {
		return this.propertyName;
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
