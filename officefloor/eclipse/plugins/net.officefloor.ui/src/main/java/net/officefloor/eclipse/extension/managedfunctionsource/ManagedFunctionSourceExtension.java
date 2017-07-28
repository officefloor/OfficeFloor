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
package net.officefloor.eclipse.extension.managedfunctionsource;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.eclipse.extension.ExtensionUtil;
import net.officefloor.eclipse.extension.open.ExtensionOpener;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.model.section.FunctionNamespaceModel;

/**
 * Interface for extension to provide enriched {@link ManagedFunctionSource}
 * usage.
 * 
 * @author Daniel Sagenschneider
 * 
 * @see ExtensionOpener
 */
public interface ManagedFunctionSourceExtension<S extends ManagedFunctionSource> {

	/**
	 * Extension ID.
	 */
	public static final String EXTENSION_ID = ExtensionUtil.getExtensionId("managedfunctionsources");

	/**
	 * Obtains the class of the {@link ManagedFunctionSource} being enriched in
	 * its usage.
	 * 
	 * @return Class of the {@link ManagedFunctionSource} being enriched in its
	 *         usage.
	 */
	Class<S> getManagedFunctionSourceClass();

	/**
	 * <p>
	 * Obtains the label for the {@link ManagedFunctionSource}.
	 * <p>
	 * This is a descriptive name that can be used other than the fully
	 * qualified name of the {@link ManagedFunctionSource}.
	 * 
	 * @return Label for the {@link ManagedFunctionSource}.
	 */
	String getManagedFunctionSourceLabel();

	/**
	 * Loads the input page with the necessary {@link Control} instances to
	 * populate the {@link PropertyList}. Also allows notifying of changes to
	 * {@link Property} instances via the
	 * {@link ManagedFunctionSourceExtensionContext}.
	 * 
	 * @param page
	 *            Page to be setup for populating the {@link PropertyList}.
	 * @param context
	 *            {@link ManagedFunctionSourceExtensionContext}.
	 */
	void createControl(Composite page, ManagedFunctionSourceExtensionContext context);

	/**
	 * Obtains the suggested name of the {@link FunctionNamespaceModel}.
	 * 
	 * @param properties
	 *            {@link PropertyList}.
	 * @return Suggested {@link FunctionNamespaceModel} name or
	 *         <code>null</code> if no suggestion.
	 */
	String getSuggestedFunctionNamespaceName(PropertyList properties);

	/**
	 * Obtains documentation about a {@link ManagedFunction}.
	 * 
	 * @param context
	 *            {@link FunctionDocumentationContext}.
	 * @return Documentation about the {@link ManagedFunction}.
	 * @throws Throwable
	 *             If fails to obtain documentation about the
	 *             {@link ManagedFunction}.
	 */
	String getFunctionDocumentation(FunctionDocumentationContext context) throws Throwable;

}