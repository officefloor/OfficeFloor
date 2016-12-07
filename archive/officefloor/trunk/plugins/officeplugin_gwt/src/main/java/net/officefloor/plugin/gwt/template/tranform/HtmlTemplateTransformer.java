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
package net.officefloor.plugin.gwt.template.tranform;

/**
 * Transforms the HTML template.
 * 
 * @author Daniel Sagenschneider
 */
public interface HtmlTemplateTransformer {

	/**
	 * Transforms the Raw HTML.
	 * 
	 * @param rawHtml
	 *            Raw HTML.
	 * @param transformation
	 *            {@link HtmlTemplateTransformation}.
	 * @return Transformed HTML.
	 */
	String transform(String rawHtml, HtmlTemplateTransformation transformation);

}