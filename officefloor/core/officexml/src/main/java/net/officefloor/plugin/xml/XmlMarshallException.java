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
package net.officefloor.plugin.xml;

import java.io.IOException;

/**
 * Indicates failure to marshall/unmarshall XML.
 * 
 * @author Daniel Sagenschneider
 */
public class XmlMarshallException extends IOException {

	/**
	 * Enforce reason.
	 * 
	 * @param reason
	 *            Reason.
	 */
	public XmlMarshallException(String reason) {
		super(reason);
	}

	/**
	 * Enforce reason and allow cause.
	 * 
	 * @param reason
	 *            Reason.
	 * @param cause
	 *            Cause.
	 */
	public XmlMarshallException(String reason, Throwable cause) {
		super(reason, cause);
	}

}
