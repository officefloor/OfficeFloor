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

package net.officefloor.plugin.web.http.application;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.plugin.autowire.AutoWireSection;
import net.officefloor.plugin.autowire.AutoWireSectionImpl;
import net.officefloor.plugin.web.http.template.parse.HttpTemplate;

/**
 * Allows wiring the flows of the {@link HttpTemplate}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpTemplateAutoWireSectionImpl extends AutoWireSectionImpl
		implements HttpTemplateAutoWireSection {

	/**
	 * Logic class for the template.
	 */
	private final Class<?> templateLogicClass;

	/**
	 * URI to the template. May be <code>null</code> if not publicly exposed
	 * template.
	 */
	private final String templateUri;

	/**
	 * Initiate.
	 * 
	 * @param compiler
	 *            {@link OfficeFloorCompiler}.
	 * @param section
	 *            {@link AutoWireSection}.
	 * @param templateLogicClass
	 *            Logic class for the template.
	 * @param templateUri
	 *            URI to the template. May be <code>null</code> if not publicly
	 *            exposed template.
	 */
	public HttpTemplateAutoWireSectionImpl(OfficeFloorCompiler compiler,
			AutoWireSection section, Class<?> templateLogicClass,
			String templateUri) {
		super(compiler, section);
		this.templateLogicClass = templateLogicClass;
		this.templateUri = templateUri;
	}

	/*
	 * ====================== HttpTemplateAutoWireSection =====================
	 */

	@Override
	public String getTemplatePath() {
		return this.getSectionLocation();
	}

	@Override
	public Class<?> getTemplateLogicClass() {
		return this.templateLogicClass;
	}

	@Override
	public String getTemplateUri() {
		return this.templateUri;
	}

}