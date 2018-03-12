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
package net.officefloor.eclipse.extension.template;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.extension.WoofExtensionUtil;
import net.officefloor.woof.template.WoofTemplateExtensionSource;

/**
 * Extension for the {@link WoofTemplateExtensionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WoofTemplateExtensionSourceExtension<S extends WoofTemplateExtensionSource> {

	/**
	 * Extension ID.
	 */
	public static final String EXTENSION_ID = WoofExtensionUtil
			.getExtensionId("wooftemplateextensionsources");

	/**
	 * Obtains the class of the {@link WoofTemplateExtensionSource}.
	 * 
	 * @return Class of the {@link WoofTemplateExtensionSource}.
	 */
	Class<S> getWoofTemplateExtensionSourceClass();

	/**
	 * <p>
	 * Obtains the label for the {@link WoofTemplateExtensionSource}.
	 * <p>
	 * This is a descriptive name that can be used other than the fully
	 * qualified name of the {@link WoofTemplateExtensionSource}.
	 * 
	 * @return Label for the {@link WoofTemplateExtensionSource}.
	 */
	String getWoofTemplateExtensionSourceLabel();

	/**
	 * Loads the input page with the necessary {@link Control} instances to
	 * populate the {@link PropertyList}. Also allows notifying of changes to
	 * {@link Property} instances via the
	 * {@link WoofTemplateExtensionSourceExtensionContext}.
	 * 
	 * @param page
	 *            Page to be setup for populating the {@link PropertyList}.
	 * @param context
	 *            {@link WoofTemplateExtensionSourceExtensionContext}.
	 */
	void createControl(Composite page,
			WoofTemplateExtensionSourceExtensionContext context);

}