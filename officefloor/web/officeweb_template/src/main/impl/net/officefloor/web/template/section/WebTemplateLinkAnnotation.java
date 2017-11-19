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
package net.officefloor.web.template.section;

import net.officefloor.web.build.HttpInput;
import net.officefloor.web.template.build.WebTemplate;

/**
 * {@link HttpInput} for the {@link WebTemplate}.
 * 
 * @author Daniel Sagenschneider
 */
public class WebTemplateLinkAnnotation {

	/**
	 * Indicates if the link is secure.
	 */
	private final boolean isLinkSecure;

	/**
	 * Name of link.
	 */
	private final String linkName;

	/**
	 * Instantiate.
	 * 
	 * @param isLinkSecure
	 *            Indicates if the link is secure.
	 * @param linkName
	 *            Name of link.
	 */
	public WebTemplateLinkAnnotation(boolean isLinkSecure, String linkName) {
		this.isLinkSecure = isLinkSecure;
		this.linkName = linkName;
	}

	/**
	 * Indicates if the link is secure.
	 * 
	 * @return <code>true</code> if link is secure.
	 */
	public boolean isLinkSecure() {
		return this.isLinkSecure;
	}

	/**
	 * Obtains the link name.
	 * 
	 * @return Link name.
	 */
	public String getLinkName() {
		return this.linkName;
	}

}