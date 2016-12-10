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
package net.officefloor.eclipse.extension.worksource;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.eclipse.extension.ExtensionUtil;
import net.officefloor.eclipse.extension.classpath.ExtensionClasspathProvider;
import net.officefloor.eclipse.extension.open.ExtensionOpener;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Interface for extension to provide enriched {@link WorkSource} usage.
 * 
 * @author Daniel Sagenschneider
 * 
 * @see ExtensionClasspathProvider
 * @see ExtensionOpener
 */
public interface WorkSourceExtension<W extends Work, S extends WorkSource<W>> {

	/**
	 * Extension ID.
	 */
	public static final String EXTENSION_ID = ExtensionUtil
			.getExtensionId("worksources");

	/**
	 * Obtains the class of the {@link WorkSource} being enriched in its usage.
	 * 
	 * @return Class of the {@link WorkSource} being enriched in its usage.
	 */
	Class<S> getWorkSourceClass();

	/**
	 * <p>
	 * Obtains the label for the {@link WorkSource}.
	 * <p>
	 * This is a descriptive name that can be used other than the fully
	 * qualified name of the {@link WorkSource}.
	 * 
	 * @return Label for the {@link WorkSource}.
	 */
	String getWorkSourceLabel();

	/**
	 * Loads the input page with the necessary {@link Control} instances to
	 * populate the {@link PropertyList}. Also allows notifying of changes to
	 * {@link Property} instances via the {@link WorkSourceExtensionContext}.
	 * 
	 * @param page
	 *            Page to be setup for populating the {@link PropertyList}.
	 * @param context
	 *            {@link WorkSourceExtensionContext}.
	 */
	void createControl(Composite page, WorkSourceExtensionContext context);

	/**
	 * Obtains the suggested name of the {@link Work}.
	 * 
	 * @param properties
	 *            {@link PropertyList}.
	 * @return Suggested {@link Work} name or <code>null</code> if no
	 *         suggestion.
	 */
	String getSuggestedWorkName(PropertyList properties);

	/**
	 * Obtains documentation about a {@link Task} of the {@link Work}.
	 * 
	 * @param context
	 *            {@link TaskDocumentationContext}.
	 * @return Documentation about the {@link Task}.
	 * @throws Throwable
	 *             If fails to obtain documentation about the {@link Task}.
	 */
	String getTaskDocumentation(TaskDocumentationContext context)
			throws Throwable;

}