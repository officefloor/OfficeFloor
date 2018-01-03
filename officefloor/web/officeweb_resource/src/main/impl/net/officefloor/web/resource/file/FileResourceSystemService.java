/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.web.resource.file;

import java.io.IOException;

import net.officefloor.web.resource.spi.ResourceSystem;
import net.officefloor.web.resource.spi.ResourceSystemContext;
import net.officefloor.web.resource.spi.ResourceSystemService;

/**
 * {@link ResourceSystemService} backed by files.
 * 
 * @author Daniel Sagenschneider
 */
public class FileResourceSystemService implements ResourceSystemService {

	/**
	 * Protocol name.
	 */
	public static final String PROTOCOL_NAME = "file";

	/*
	 * ==================== ResourceSystemService =====================
	 */

	@Override
	public String getProtocolName() {
		return PROTOCOL_NAME;
	}

	@Override
	public ResourceSystem createResourceSystem(ResourceSystemContext context) throws IOException {
		return new FileResourceSystem(context);
	}

}