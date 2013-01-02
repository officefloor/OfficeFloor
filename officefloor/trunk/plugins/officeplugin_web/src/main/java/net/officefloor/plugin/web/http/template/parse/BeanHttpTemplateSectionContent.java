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
package net.officefloor.plugin.web.http.template.parse;

/**
 * {@link HttpTemplateSectionContent} that references a bean to use.
 * 
 * @author Daniel Sagenschneider
 */
public interface BeanHttpTemplateSectionContent extends
		HttpTemplateSectionContent {

	/**
	 * Obtains the name of the property to obtain the bean.
	 * 
	 * @return Name of the property to obtain the bean.
	 */
	String getPropertyName();

	/**
	 * Obtains the {@link HttpTemplateSectionContent} instances that comprise
	 * the content for this {@link BeanHttpTemplateSectionContent}.
	 * 
	 * @return {@link HttpTemplateSectionContent} instances that comprise the
	 *         content for this {@link BeanHttpTemplateSectionContent}.
	 */
	HttpTemplateSectionContent[] getContent();

}