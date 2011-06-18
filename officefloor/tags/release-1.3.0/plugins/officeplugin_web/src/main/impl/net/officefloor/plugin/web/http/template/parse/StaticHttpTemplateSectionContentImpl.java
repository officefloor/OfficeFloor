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

package net.officefloor.plugin.web.http.template.parse;

import net.officefloor.plugin.web.http.template.parse.StaticHttpTemplateSectionContent;

/**
 * {@link StaticHttpTemplateSectionContent} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class StaticHttpTemplateSectionContentImpl implements
		StaticHttpTemplateSectionContent {

	/**
	 * Static content.
	 */
	private final String staticContent;

	/**
	 * Initiate.
	 *
	 * @param staticContent
	 *            Static content.
	 */
	public StaticHttpTemplateSectionContentImpl(String staticContent) {
		this.staticContent = staticContent;
	}

	/*
	 * ===================== StaticHttpTemplateSectionContent ==================
	 */

	@Override
	public String getStaticContent() {
		return this.staticContent;
	}

}