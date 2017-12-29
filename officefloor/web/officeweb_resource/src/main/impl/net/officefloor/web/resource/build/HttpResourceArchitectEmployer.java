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
package net.officefloor.web.resource.build;

import java.io.File;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeEscalation;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.web.build.WebArchitect;

/**
 * Employs a {@link HttpResourceArchitect}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpResourceArchitectEmployer implements HttpResourceArchitect {

	/**
	 * Employs the {@link HttpResourceArchitect}.
	 * 
	 * @param webArchitect
	 *            {@link WebArchitect}.
	 * @param officeArchitect
	 *            {@link OfficeArchitect}.
	 * @param officeSourceContext
	 *            {@link OfficeSourceContext}.
	 * @return {@link HttpResourceArchitect}.
	 */
	public static HttpResourceArchitect employHttpResourceArchitect(WebArchitect webArchitect,
			OfficeArchitect officeArchitect, OfficeSourceContext officeSourceContext) {
		return new HttpResourceArchitectEmployer(webArchitect, officeArchitect, officeSourceContext);
	}

	/**
	 * {@link WebArchitect}.
	 */
	private final WebArchitect webArchitect;

	/**
	 * {@link OfficeArchitect}.
	 */
	private final OfficeArchitect officeArchitect;

	/**
	 * {@link OfficeSourceContext}.
	 */
	private final OfficeSourceContext officeSourceContext;

	/**
	 * Instantiate.
	 * 
	 * @param webArchitect
	 *            {@link WebArchitect}.
	 * @param officeArchitect
	 *            {@link OfficeArchitect}.
	 * @param officeSourceContext
	 *            {@link OfficeSourceContext}.
	 */
	private HttpResourceArchitectEmployer(WebArchitect webArchitect, OfficeArchitect officeArchitect,
			OfficeSourceContext officeSourceContext) {
		this.webArchitect = webArchitect;
		this.officeArchitect = officeArchitect;
		this.officeSourceContext = officeSourceContext;
	}

	/*
	 * =================== HttpResourceArchitect ========================
	 */

	@Override
	public void addHttpFileDescriber(HttpFileDescriber describer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void link(OfficeSectionOutput output, String resourcePath) {
		// TODO Auto-generated method stub

	}

	@Override
	public void link(OfficeEscalation escalation, String resourcePath) {
		// TODO Auto-generated method stub

	}

	@Override
	public ExternalHttpResourcesBuilder addExternalHttpResources(File rootDirectory) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void informWebArchitect() {
		// TODO Auto-generated method stub

	}

}