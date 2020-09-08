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

package net.officefloor.frame.test;

import org.easymock.Capture;
import org.easymock.EasyMock;

/**
 * Captures a parameter value.
 * 
 * @author Daniel Sagenschneider
 */
public class ParameterCapture<T> {

	/**
	 * {@link Capture}.
	 */
	private final Capture<T> capture = Capture.newInstance();

	/**
	 * Captures the value.
	 * 
	 * @return Parameter input to capture the value.
	 */
	public T capture() {
		return EasyMock.capture(this.capture);
	}

	/**
	 * Obtains the value.
	 * 
	 * @return Value.
	 */
	public T getValue() {
		return this.capture.getValue();
	}

}
