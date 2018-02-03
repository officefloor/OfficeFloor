/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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

import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.web.security.build.office.HttpOfficeSecurer;
import net.officefloor.web.spi.security.HttpSecurity;

/**
 * HTTP security builder.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecurityBuilder extends PropertyConfigurable {

	/**
	 * Adds the a <code>Content-Type</code> supported by this
	 * {@link HttpSecurity}.
	 * 
	 * @param contentType
	 *            <code>Content-Type</code> supported by this
	 *            {@link HttpSecurity}.
	 */
	void addContentType(String contentType);

	/**
	 * <p>
	 * Obtains the {@link OfficeSectionInput} to authenticate with application
	 * credentials.
	 * <p>
	 * The application credentials are to be a parameter to this
	 * {@link OfficeSectionInput}.
	 * 
	 * @return {@link OfficeSectionInput} to undertake authentication with the
	 *         application credentials..
	 */
	OfficeSectionInput getAuthenticateInput();

	/**
	 * Obtains the {@link OfficeSectionOutput} from the {@link HttpSecurity}.
	 * 
	 * @param outputName
	 *            {@link OfficeSectionOutput} name.
	 * @return {@link OfficeSectionOutput} for the name.
	 */
	OfficeSectionOutput getOutput(String outputName);

	/**
	 * Registers {@link HttpOfficeSecurer} for this {@link HttpSecurity}.
	 * 
	 * @param securer
	 *            {@link HttpOfficeSecurer}.
	 * @return {@link HttpSecurerBuilder} to configure the {@link HttpOfficeSecurer}.
	 */
	HttpSecurerBuilder secure(HttpOfficeSecurer securer);

}