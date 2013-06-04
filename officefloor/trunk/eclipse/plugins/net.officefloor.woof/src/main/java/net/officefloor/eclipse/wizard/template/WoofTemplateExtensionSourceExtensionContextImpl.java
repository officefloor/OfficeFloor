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

import org.eclipse.core.resources.IProject;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.extension.template.WoofTemplateExtensionSourceExtensionContext;

/**
 * {@link WoofTemplateExtensionSourceExtensionContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofTemplateExtensionSourceExtensionContextImpl implements
		WoofTemplateExtensionSourceExtensionContext {

	/*
	 * ============= WoofTemplateExtensionSourceExtensionContext =============
	 */

	@Override
	public PropertyList getPropertyList() {
		// TODO implement
		// WoofTemplateExtensionSourceExtensionContext.getPropertyList
		throw new UnsupportedOperationException(
				"TODO implement WoofTemplateExtensionSourceExtensionContext.getPropertyList");
	}

	@Override
	public void notifyPropertiesChanged() {
		// TODO implement
		// WoofTemplateExtensionSourceExtensionContext.notifyPropertiesChanged
		throw new UnsupportedOperationException(
				"TODO implement WoofTemplateExtensionSourceExtensionContext.notifyPropertiesChanged");
	}

	@Override
	public void setErrorMessage(String message) {
		// TODO implement
		// WoofTemplateExtensionSourceExtensionContext.setErrorMessage
		throw new UnsupportedOperationException(
				"TODO implement WoofTemplateExtensionSourceExtensionContext.setErrorMessage");
	}

	@Override
	public IProject getProject() {
		// TODO implement WoofTemplateExtensionSourceExtensionContext.getProject
		throw new UnsupportedOperationException(
				"TODO implement WoofTemplateExtensionSourceExtensionContext.getProject");
	}

}