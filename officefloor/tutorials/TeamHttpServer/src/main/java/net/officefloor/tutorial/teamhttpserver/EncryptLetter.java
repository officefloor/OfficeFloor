/*-
 * #%L
 * Team HTTP Server Tutorial
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

package net.officefloor.tutorial.teamhttpserver;

import java.io.Serializable;

import net.officefloor.web.HttpParameters;

/**
 * Request to encode the letter.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: example
@HttpParameters
public class EncryptLetter implements Serializable {
	private static final long serialVersionUID = 1L;

	private char letter;

	public void setLetter(String letter) {
		this.letter = (letter.length() == 0 ? ' ' : letter.charAt(0));
	}

	public char getLetter() {
		return this.letter;
	}

}
// END SNIPPET: example
