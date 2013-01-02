/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.plugin.web.http.location;

/**
 * Tests the {@link HttpApplicationLocationMangedObject} with a deep context
 * path. In other words, multiple directory deep context path.
 * 
 * @author Daniel Sagenschneider
 */
public class DeepContextApplicationPathTest extends
		AbstractHttpApplicationLocationManagedObjectTestCase {

	@Override
	protected HttpApplicationLocationMangedObject createHttpApplicationLocation(
			String domain, int httpPort, int httpsPort) {
		return new HttpApplicationLocationMangedObject(domain, httpPort,
				httpsPort, "/context/path", "node.officefloor.net", 7878, 7979);
	}

}