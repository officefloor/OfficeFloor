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

/**
 * Parameter {@link ArgumentsMatcher}
 * 
 * @author Daniel Sagenschneider
 */
public class ParameterMatcher implements ArgumentsMatcher {

	/**
	 * {@link AbstractMatch} instances for matching the parameters.
	 */
	private final AbstractMatch[] matches;

	/**
	 * Initiate.
	 * 
	 * @param matches {@link AbstractMatch} instances for matching the parameters.
	 */
	public ParameterMatcher(AbstractMatch... matches) {
		this.matches = matches;
	}

	/*
	 * ================= ArgumentsMatcher =========================
	 */

	@Override
	public boolean matches(Object[] actual) {

		// Ensure correct number of actual parameters
		if (this.matches.length != actual.length) {
			return false;
		}

		// Ensure parameters match
		for (int i = 0; i < this.matches.length; i++) {
			if (!this.matches[i].isMatch(actual[i])) {
				return false;
			}
		}

		// Matches if at this point
		return true;
	}

}