/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.plugin.work.http.html.template.parse;

/**
 * {@link TemplateSectionContent} that references other content to be used.
 * 
 * @author Daniel
 */
public class ReferenceTemplateSectionContent implements TemplateSectionContent {

	/**
	 * Key that references content to be used.
	 */
	private final String key;

	/**
	 * Initiate.
	 * 
	 * @param key
	 *            Key that references content to be used.
	 */
	public ReferenceTemplateSectionContent(String key) {
		this.key = key;
	}

	/**
	 * Obtains the key.
	 * 
	 * @return Key.
	 */
	public String getKey() {
		return this.key;
	}

}
