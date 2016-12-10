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
 * Context for the {@link HtmlTemplateTransformationContext}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HtmlTemplateTransformationContext {

	/**
	 * Obtains the type of tag.
	 * 
	 * @return {@link HtmlTagType}.
	 */
	HtmlTagType getTagType();

	/**
	 * Obtains the tag name.
	 * 
	 * @return Name of the tag.
	 */
	String getTagName();

	/**
	 * Obtains the attribute value for the tag.
	 * 
	 * @param name
	 *            Name of attribute.
	 * @return Value for attribute or <code>null</code> if no attribute by name.
	 */
	String getAttributeValue(String name);

	/**
	 * Prepends content before the tag.
	 * 
	 * @param content
	 *            Content.
	 */
	void prependContent(String content);

	/**
	 * Inputs content within the tag:
	 * <ol>
	 * <li>{@link HtmlTagType#OPEN} appends content</li>
	 * <li>{@link HtmlTagType#OPEN} prepends content</li>
	 * <li>{@link HtmlTagType#OPEN_CLOSE} splits tag and adds content between</li>
	 * </ol>
	 * 
	 * @param content
	 *            Content.
	 */
	void inputContent(String content);

	/**
	 * Appends content after the tag.
	 * 
	 * @param content
	 *            Content.
	 */
	void appendContent(String content);

}