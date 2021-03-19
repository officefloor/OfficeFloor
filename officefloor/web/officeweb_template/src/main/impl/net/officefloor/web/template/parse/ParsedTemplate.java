/*-
 * #%L
 * Web Template
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

package net.officefloor.web.template.parse;

import net.officefloor.web.template.build.WebTemplate;

/**
 * Parsed {@link WebTemplate} .
 *
 * @author Daniel Sagenschneider
 */
public class ParsedTemplate {

	/**
	 * {@link ParsedTemplateSection} instances.
	 */
	private final ParsedTemplateSection[] sections;

	/**
	 * Initiate.
	 *
	 * @param sections
	 *            {@link ParsedTemplateSection} instances.
	 */
	public ParsedTemplate(ParsedTemplateSection[] sections) {
		this.sections = sections;
	}

	/**
	 * Obtains the {@link ParsedTemplateSection} instances for this
	 * {@link ParsedTemplate}.
	 *
	 * @return {@link ParsedTemplateSection} instances for this
	 *         {@link ParsedTemplate}.
	 */
	public ParsedTemplateSection[] getSections() {
		return this.sections;
	}

}
