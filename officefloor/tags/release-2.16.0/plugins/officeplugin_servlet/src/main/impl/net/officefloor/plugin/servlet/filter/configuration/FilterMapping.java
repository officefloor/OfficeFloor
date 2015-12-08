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
package net.officefloor.plugin.servlet.filter.configuration;

import javax.servlet.Filter;
import javax.servlet.Servlet;

import net.officefloor.plugin.servlet.mapping.MappingType;

/**
 * Mapping for a {@link Filter}.
 * 
 * @author Daniel Sagenschneider
 */
public class FilterMapping {

	/**
	 * Name of the {@link Filter}.
	 */
	private String filterName;

	/**
	 * URL pattern.
	 */
	private String urlPattern;

	/**
	 * {@link Servlet} name.
	 */
	private String servletName;

	/**
	 * {@link MappingType} instances.
	 */
	private MappingType[] mappingTypes;

	/**
	 * Initiate.
	 * 
	 * @param filterName
	 *            Name of the {@link Filter}.
	 * @param urlPattern
	 *            URL pattern.
	 * @param servletName
	 *            {@link Servlet} name.
	 * @param mappingTypes
	 *            {@link MappingType} instances.
	 */
	public FilterMapping(String filterName, String urlPattern,
			String servletName, MappingType... mappingTypes) {
		this.filterName = filterName;
		this.urlPattern = urlPattern;
		this.servletName = servletName;
		this.mappingTypes = mappingTypes;
	}

	/**
	 * Obtains the {@link Filter} name.
	 * 
	 * @return {@link Filter} name.
	 */
	public String getFilterName() {
		return this.filterName;
	}

	/**
	 * Specifies the {@link Filter} name.
	 * 
	 * @param filterName
	 *            {@link Filter} name.
	 */
	public void setFilterName(String filterName) {
		this.filterName = filterName;
	}

	/**
	 * Obtains the URL pattern.
	 * 
	 * @return URL pattern.
	 */
	public String getUrlPattern() {
		return this.urlPattern;
	}

	/**
	 * Specifies the URL pattern.
	 * 
	 * @param urlPattern
	 *            URL pattern.
	 */
	public void setUrlPattern(String urlPattern) {
		this.urlPattern = urlPattern;
	}

	/**
	 * Obtains the {@link Servlet} name.
	 * 
	 * @return {@link Servlet} name.
	 */
	public String getServletName() {
		return this.servletName;
	}

	/**
	 * Specifies the {@link Servlet} name.
	 * 
	 * @param servletName
	 *            {@link Servlet} name.
	 */
	public void setServletName(String servletName) {
		this.servletName = servletName;
	}

	/**
	 * Obtains the {@link MappingType} instances.
	 * 
	 * @return {@link MappingType} instances.
	 */
	public MappingType[] getMappingTypes() {
		return this.mappingTypes;
	}

	/**
	 * Specifies the {@link MappingType} instances.
	 * 
	 * @param mappingTypes
	 *            {@link MappingType} instances.
	 */
	public void setMappingTypes(MappingType[] mappingTypes) {
		this.mappingTypes = mappingTypes;
	}

}