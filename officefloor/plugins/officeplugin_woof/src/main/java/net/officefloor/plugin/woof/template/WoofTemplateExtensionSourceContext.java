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
package net.officefloor.plugin.woof.template;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.configuration.ConfigurationContext;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.plugin.web.http.application.HttpTemplateSection;
import net.officefloor.plugin.web.http.application.WebArchitect;

/**
 * Context for the {@link WoofTemplateExtensionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WoofTemplateExtensionSourceContext extends SourceContext, ConfigurationContext {

	/**
	 * Obtains the URL path to the {@link HttpTemplateSection}.
	 * 
	 * @return URL path to the {@link HttpTemplateSection}.
	 */
	String getTemplatePath();

	/**
	 * Obtains the {@link HttpTemplateSection} being extended.
	 * 
	 * @return {@link HttpTemplateSection} being extended.
	 */
	HttpTemplateSection getTemplate();

	/**
	 * Obtains the {@link WebArchitect} that the {@link HttpTemplateSection} has
	 * been added.
	 * 
	 * @return {@link WebArchitect}.
	 */
	WebArchitect getWebApplication();

	/**
	 * Obtains the {@link OfficeArchitect} that the {@link HttpTemplateSection}
	 * has been added.
	 * 
	 * @return {@link OfficeArchitect}.
	 */
	OfficeArchitect getOfficeArchitect();

}