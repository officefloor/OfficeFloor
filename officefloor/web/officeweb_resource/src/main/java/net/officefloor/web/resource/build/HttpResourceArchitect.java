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

import net.officefloor.compile.spi.office.OfficeEscalation;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.resource.HttpResource;

/**
 * Builds the {@link HttpResource} for {@link WebArchitect}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpResourceArchitect {

	/**
	 * Adds a {@link HttpFileDescriber}.
	 * 
	 * @param describer
	 *            {@link HttpFileDescriber}.
	 */
	void addHttpFileDescriber(HttpFileDescriber describer);

	/**
	 * Links the {@link OfficeSectionOutput} to the {@link HttpResource}.
	 * 
	 * @param output
	 *            {@link OfficeSectionOutput}.
	 * @param resourcePath
	 *            Path to the {@link HttpResource}.
	 */
	void link(OfficeSectionOutput output, String resourcePath);

	/**
	 * Links the {@link OfficeEscalation} to the {@link HttpResource}.
	 * 
	 * @param escalation
	 *            {@link OfficeEscalation}.
	 * @param resourcePath
	 *            Path to the {@link HttpResource}.
	 */
	void link(OfficeEscalation escalation, String resourcePath);

	/**
	 * Adds external {@link HttpResource} instances that may be served from file
	 * system.
	 * 
	 * @param rootDirectory
	 *            Root directory for the external {@link HttpResource}
	 *            instances.
	 * @return {@link ExternalHttpResourcesBuilder}.
	 */
	ExternalHttpResourcesBuilder addExternalHttpResources(File rootDirectory);

	/**
	 * Informs the {@link WebArchitect} of the necessary {@link HttpResource}
	 * instances. This is to be invoked once all {@link HttpResource} instances
	 * are configured.
	 */
	void informWebArchitect();

}