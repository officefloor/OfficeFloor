/*-
 * #%L
 * Web on OfficeFloor
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

package net.officefloor.woof;

import net.officefloor.OfficeFloorMain;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.server.http.HttpServerLocation;


/**
 * <p>
 * Provides <code>main</code> method for running Web on OfficeFloor (WoOF)
 * application.
 * <p>
 * It also provides useful methods for testing the WoOF server.
 * 
 * @author Daniel Sagenschneider
 */
public class WoOF extends OfficeFloorMain {

	/**
	 * Opens the WoOF application on the ports.
	 * 
	 * @param httpPort
	 *            HTTP port.
	 * @param httpsPort
	 *            HTTPS port.
	 * @return {@link OfficeFloor} to the WoOF application.
	 */
	public static OfficeFloor open(int httpPort, int httpsPort) {
		return OfficeFloorMain.open(HttpServerLocation.PROPERTY_HTTP_PORT, String.valueOf(httpPort),
				HttpServerLocation.PROPERTY_HTTP_PORT, String.valueOf(httpsPort));
	}

}
