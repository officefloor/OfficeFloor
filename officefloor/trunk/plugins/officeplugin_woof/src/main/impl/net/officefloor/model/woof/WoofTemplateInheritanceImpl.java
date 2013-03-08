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
package net.officefloor.model.woof;

import java.util.Set;

/**
 * {@link WoofTemplateInheritance} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofTemplateInheritanceImpl implements WoofTemplateInheritance {

	/**
	 * Inheritance hierarchy.
	 */
	private final WoofTemplateModel[] hierarchy;

	/**
	 * Inheritance template path property value.
	 */
	private final String inheritancePropertyValue;

	/**
	 * Inherited output names.
	 */
	private final Set<String> inheritedOutputNames;

	/**
	 * Initiate.
	 * 
	 * @param hierarchy
	 *            Inheritance hierarchy.
	 * @param inheritancePropertyValue
	 *            Inheritance template path property value.
	 * @param inheritedOutputNames
	 *            Inherited output names.
	 */
	public WoofTemplateInheritanceImpl(WoofTemplateModel[] hierarchy,
			String inheritancePropertyValue, Set<String> inheritedOutputNames) {
		this.hierarchy = hierarchy;
		this.inheritancePropertyValue = inheritancePropertyValue;
		this.inheritedOutputNames = inheritedOutputNames;
	}

	/*
	 * ==================== WoofTemplateInheritance =======================
	 */

	@Override
	public WoofTemplateModel[] getInheritanceHierarchy() {
		return this.hierarchy;
	}

	@Override
	public String getInheritedTemplatePathsPropertyValue() {
		return this.inheritancePropertyValue;
	}

	@Override
	public Set<String> getInheritedWoofTemplateOutputNames() {
		return this.inheritedOutputNames;
	}

}