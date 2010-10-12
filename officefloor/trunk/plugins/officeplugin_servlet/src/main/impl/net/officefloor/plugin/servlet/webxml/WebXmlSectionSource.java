/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.plugin.servlet.webxml;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;

import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.plugin.servlet.webxml.model.WebAppModel;
import net.officefloor.plugin.servlet.webxml.model.WebXmlLoader;

/**
 * {@link SectionSource} to load {@link HttpServlet} functionality from a
 * <code>web.xml</code> file (as per {@link Servlet} specification).
 * 
 * @author Daniel Sagenschneider
 */
public class WebXmlSectionSource extends AbstractSectionSource {

	/*
	 * ===================== SectionSource ============================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification required, using location for web.xml
	}

	@Override
	public void sourceSection(SectionDesigner sectionBuilder,
			SectionSourceContext context) throws Exception {

		// Load the web.xml configuration
		String webXmlLocation = context.getSectionLocation();
		WebAppModel webApp = new WebXmlLoader().loadConfiguration(
				webXmlLocation, context);

		// TODO utilise web-app model to populate section
	}

}