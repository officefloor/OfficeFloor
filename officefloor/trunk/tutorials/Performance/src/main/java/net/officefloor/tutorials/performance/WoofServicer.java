/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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
package net.officefloor.tutorials.performance;

import net.officefloor.plugin.woof.WoofOfficeFloorSource;

/**
 * WoOF {@link Servicer}
 * 
 * @author Daniel Sagenschneider
 */
public class WoofServicer implements Servicer {

	@Override
	public int getPort() {
		return 7878;
	}

	@Override
	public int getMaximumConnectionCount() {
		return 10000;
	}

	@Override
	public void start() throws Exception {
		WoofOfficeFloorSource.start();
	}

	@Override
	public void stop() throws Exception {
		WoofOfficeFloorSource.stop();
	}

}