/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
package net.officefloor.example.servletfilterwebapplication;

import net.officefloor.plugin.autowire.AutoWireSection;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.servlet.OfficeFloorServletFilter;
import net.officefloor.plugin.web.http.server.HttpTemplateAutoWireSection;

/**
 * Example {@link OfficeFloorServletFilter}
 * 
 * @author Daniel Sagenschneider
 */
public class ExampleFilter extends OfficeFloorServletFilter {

	@Override
	protected void configure() {

		// Configure a template
		HttpTemplateAutoWireSection template = this.addHttpTemplate(
				"templates/Template.ofp", ExampleTemplateLogic.class,
				"template");

		// Configure rendering of link response by JSP
		this.linkToServletResource(template, "LINK", "/Template.jsp");

		// Configure class
		AutoWireSection section = this.addSection("class",
				ClassSectionSource.class, ExampleClass.class.getName());
		this.linkUri("class", section, "example");
	}

}