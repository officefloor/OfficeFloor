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
package net.officefloor.eclipse.wizard.template;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.extension.sectionsource.SectionSourceExtensionContext;
import net.officefloor.eclipse.extension.template.WoofTemplateExtensionSourceExtensionContext;

import org.eclipse.core.resources.IProject;

/**
 * {@link WoofTemplateExtensionSourceExtensionContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofTemplateExtensionSourceExtensionContextImpl implements
		WoofTemplateExtensionSourceExtensionContext {

	/**
	 * {@link PropertyList}.
	 */
	private final PropertyList propertyList;

	/**
	 * {@link SectionSourceExtensionContext}.
	 */
	private final SectionSourceExtensionContext context;

	/**
	 * Initiate.
	 * 
	 * @param propertyList
	 *            {@link PropertyList}.
	 * @param context
	 *            {@link SectionSourceExtensionContext}.
	 */
	public WoofTemplateExtensionSourceExtensionContextImpl(
			PropertyList propertyList, SectionSourceExtensionContext context) {
		this.propertyList = propertyList;
		this.context = context;
	}

	/*
	 * ============= WoofTemplateExtensionSourceExtensionContext =============
	 */

	@Override
	public PropertyList getPropertyList() {
		return this.propertyList;
	}

	@Override
	public void notifyPropertiesChanged() {
		this.context.notifyPropertiesChanged();
	}

	@Override
	public void setErrorMessage(String message) {
		this.context.setErrorMessage(message);
	}

	@Override
	public IProject getProject() {
		return this.context.getProject();
	}

}