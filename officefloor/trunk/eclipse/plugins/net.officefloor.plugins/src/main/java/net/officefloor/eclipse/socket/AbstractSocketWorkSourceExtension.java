/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.eclipse.socket;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.eclipse.extension.classpath.ClasspathProvision;
import net.officefloor.eclipse.extension.classpath.ExtensionClasspathProvider;
import net.officefloor.eclipse.extension.classpath.TypeClasspathProvision;
import net.officefloor.eclipse.extension.worksource.WorkSourceExtension;
import net.officefloor.frame.api.execute.Work;

/**
 * Provides abstract functionality for a source extension.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractSocketWorkSourceExtension<W extends Work, S extends WorkSource<W>>
		implements WorkSourceExtension<W, S>, ExtensionClasspathProvider {

	/**
	 * {@link WorkSource} class.
	 */
	private final Class<S> workSourceClass;

	/**
	 * Label for the {@link WorkSource}.
	 */
	private final String workSourceLabel;

	/**
	 * Initiate.
	 * 
	 * @param workSourceClass
	 *            {@link WorkSource} class.
	 * @param workSourceLabel
	 *            Label for the {@link WorkSource}.
	 */
	public AbstractSocketWorkSourceExtension(Class<S> workSourceClass,
			String workSourceLabel) {
		this.workSourceClass = workSourceClass;
		this.workSourceLabel = workSourceLabel;
	}

	/*
	 * ===================== ExtensionClasspathProvider ======================
	 */

	@Override
	public ClasspathProvision[] getClasspathProvisions() {
		return new ClasspathProvision[] { new TypeClasspathProvision(
				this.workSourceClass) };
	}

	/*
	 * ===================== WorkSourceExtension =============================
	 */

	@Override
	public Class<S> getWorkSourceClass() {
		return this.workSourceClass;
	}

	@Override
	public String getWorkSourceLabel() {
		return this.workSourceLabel;
	}

	@Override
	public String getSuggestedWorkName(PropertyList properties) {
		// Obtain the suggested work name from label
		String suggestedWorkName = this.getWorkSourceLabel();
		suggestedWorkName.replace(" ", ""); // remove spacing
		return suggestedWorkName;
	}

}