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
package net.officefloor.eclipse.extension.worksource;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.eclipse.extension.ExtensionUtil;
import net.officefloor.eclipse.extension.classpath.ExtensionClasspathProvider;
import net.officefloor.frame.api.execute.Work;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Interface for extension to provide enriched {@link WorkSource} usage.
 * 
 * @author Daniel
 * 
 * @see ExtensionClasspathProvider
 */
public interface WorkSourceExtension<W extends Work, S extends WorkSource<W>> {

	/**
	 * Extension ID.
	 */
	public static final String EXTENSION_ID = ExtensionUtil
			.getExtensionId("worksource");

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

}