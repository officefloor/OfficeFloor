/*-
 * #%L
 * Web configuration
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

package net.officefloor.woof.template;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.configuration.ConfigurationContext;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.template.build.WebTemplate;

/**
 * Context for the {@link WoofTemplateExtensionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WoofTemplateExtensionSourceContext extends SourceContext, ConfigurationContext {

	/**
	 * Obtains the application path to the {@link WebTemplate}.
	 * 
	 * @return Application path to the {@link WebTemplate}.
	 */
	String getApplicationPath();

	/**
	 * Obtains the {@link WebTemplate} being extended.
	 * 
	 * @return {@link WebTemplate} being extended.
	 */
	WebTemplate getTemplate();

	/**
	 * Obtains the {@link WebArchitect} that the {@link WebTemplate} has been
	 * added.
	 * 
	 * @return {@link WebArchitect}.
	 */
	WebArchitect getWebArchitect();

	/**
	 * Obtains the {@link OfficeArchitect} that the {@link WebTemplate} has been
	 * added.
	 * 
	 * @return {@link OfficeArchitect}.
	 */
	OfficeArchitect getOfficeArchitect();

}
