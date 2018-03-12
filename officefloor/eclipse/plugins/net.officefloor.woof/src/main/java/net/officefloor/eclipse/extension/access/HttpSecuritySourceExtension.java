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
package net.officefloor.eclipse.extension.access;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.extension.ExtensionUtil;
import net.officefloor.eclipse.extension.open.ExtensionOpener;
import net.officefloor.web.spi.security.HttpSecuritySource;

/**
 * Interface for extension to provide enriched {@link HttpSecuritySource} usage.
 * 
 * @author Daniel Sagenschneider
 * 
 * @see ExtensionOpener
 */
@SuppressWarnings("rawtypes")
public interface HttpSecuritySourceExtension<S extends HttpSecuritySource> {

	/**
	 * Extension ID.
	 */
	public static final String EXTENSION_ID = ExtensionUtil.getExtensionId("httpsecuritysources");

	/**
	 * Obtains the class of the {@link HttpSecuritySource} being enriched in its
	 * usage.
	 * 
	 * @return Class of the {@link HttpSecuritySource} being enriched in its
	 *         usage.
	 */
	Class<S> getHttpSecuritySourceClass();

	/**
	 * <p>
	 * Obtains the label for the {@link HttpSecuritySource}.
	 * <p>
	 * This is a descriptive name that can be used other than the fully
	 * qualified name of the {@link HttpSecuritySource}.
	 * 
	 * @return Label for the {@link HttpSecuritySource}.
	 */
	String getHttpSecuritySourceLabel();

	/**
	 * Loads the input page with the necessary {@link Control} instances to
	 * populate the {@link PropertyList}. Also allows notifying of changes to
	 * {@link Property} instances via the
	 * {@link HttpSecuritySourceExtensionContext}.
	 * 
	 * @param page
	 *            Page to be setup for populating the {@link PropertyList}.
	 * @param context
	 *            {@link HttpSecuritySourceExtensionContext}.
	 */
	void createControl(Composite page, HttpSecuritySourceExtensionContext context);

}