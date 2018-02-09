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
package net.officefloor.web.security.build;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.web.security.build.office.HttpOfficeSecurer;
import net.officefloor.web.security.build.section.HttpFlowSecurer;
import net.officefloor.web.spi.security.HttpSecurity;

/**
 * Configures {@link HttpSecurity} around configuration for the
 * {@link OfficeArchitect} and {@link SectionDesigner}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecurer {

	/**
	 * Registers {@link HttpOfficeSecurer}.
	 * 
	 * @param securer
	 *            {@link HttpOfficeSecurer}.
	 * @return {@link HttpSecurer} to configure the {@link HttpOfficeSecurer}.
	 */
	void secure(HttpOfficeSecurer securer);

	/**
	 * Creates the {@link HttpFlowSecurer}.
	 * 
	 * @return {@link HttpFlowSecurer}.
	 */
	HttpFlowSecurer createFlowSecurer();

}