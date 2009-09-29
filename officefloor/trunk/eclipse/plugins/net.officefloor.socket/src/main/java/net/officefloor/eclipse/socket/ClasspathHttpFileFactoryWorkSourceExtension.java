/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
import net.officefloor.eclipse.extension.classpath.ClasspathProvision;
import net.officefloor.eclipse.extension.classpath.ExtensionClasspathProvider;
import net.officefloor.eclipse.extension.classpath.TypeClasspathProvision;
import net.officefloor.eclipse.extension.util.SourceExtensionUtil;
import net.officefloor.eclipse.extension.worksource.WorkSourceExtension;
import net.officefloor.eclipse.extension.worksource.WorkSourceExtensionContext;
import net.officefloor.plugin.socket.server.http.file.source.ClasspathHttpFileFactoryWorkSource;
import net.officefloor.plugin.socket.server.http.file.source.HttpFileFactoryTask;

import org.eclipse.swt.widgets.Composite;

/**
 * {@link WorkSourceExtension} for the
 * {@link ClasspathHttpFileFactoryWorkSource}.
 *
 * @author Daniel Sagenschneider
 */
public class ClasspathHttpFileFactoryWorkSourceExtension
		implements
		WorkSourceExtension<HttpFileFactoryTask, ClasspathHttpFileFactoryWorkSource>,
		ExtensionClasspathProvider {

	/*
	 * ================ WorkLoaderExtension ====================
	 */

	@Override
	public Class<ClasspathHttpFileFactoryWorkSource> getWorkSourceClass() {
		return ClasspathHttpFileFactoryWorkSource.class;
	}

	@Override
	public String getWorkSourceLabel() {
		return "HTTP File";
	}

	@Override
	public void createControl(Composite page,
			final WorkSourceExtensionContext context) {

		// Provide properties
		SourceExtensionUtil.loadPropertyLayout(page);
		SourceExtensionUtil.createPropertyText("Package prefix",
				ClasspathHttpFileFactoryWorkSource.PROPERTY_CLASSPATH_PREFIX,
				"html", page, context);
		SourceExtensionUtil.createPropertyText("Directory index file name",
				ClasspathHttpFileFactoryWorkSource.PROPERTY_DEFAULT_FILE_NAME,
				"index.html", page, context);
	}

	@Override
	public String getSuggestedWorkName(PropertyList properties) {
		return "HttpFile";
	}

	/*
	 * =================== ExtensionClasspathProvider =====================
	 */

	@Override
	public ClasspathProvision[] getClasspathProvisions() {
		return new ClasspathProvision[] { new TypeClasspathProvision(
				ClasspathHttpFileFactoryWorkSource.class) };
	}

}