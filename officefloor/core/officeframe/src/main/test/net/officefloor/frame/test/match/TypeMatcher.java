/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.test.match;

import org.junit.Assert;

/**
 * {@link ArgumentsMatcher} that checks type of objects only.
 * 
 * @author Daniel Sagenschneider
 */
public class TypeMatcher implements ArgumentsMatcher {

	/**
	 * Types of the arguments expected.
	 */
	protected final Class<?>[] matchTypes;

	/**
	 * Initiate with a multiple argument types.
	 * 
	 * @param types Types corresponding the parameters.
	 */
	public TypeMatcher(Class<?>... types) {
		// Initiate state
		this.matchTypes = types;
	}

	/*
	 * ====================== ArgumentsMatcher =========================
	 */

	@Override
	public boolean matches(Object[] actual) {

		// Ensure get actual matches
		if (actual == null) {
			Assert.fail("No actual values");
		}

		// Determine if incorrect number of parameters
		if (actual.length != this.matchTypes.length) {
			Assert.fail("Invalid number of parameters configured into " + this.getClass().getSimpleName());
		}

		// Ensure parameters match
		int index = 0;
		for (Class<?> matchType : this.matchTypes) {
			// Ensure null if expected
			if (matchType == null) {
				if (actual[index] != null) {
					return false;
				}
			} else {
				// Ensure same type
				if (!matchType.isInstance(actual[index])) {
					return false;
				}
			}
			index++;
		}

		// Matches
		return true;
	}

}
