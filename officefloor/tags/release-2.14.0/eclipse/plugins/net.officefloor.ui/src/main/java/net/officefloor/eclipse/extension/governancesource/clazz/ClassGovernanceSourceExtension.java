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
package net.officefloor.eclipse.extension.governancesource.clazz;

import net.officefloor.eclipse.extension.classpath.ClasspathProvision;
import net.officefloor.eclipse.extension.classpath.ExtensionClasspathProvider;
import net.officefloor.eclipse.extension.classpath.TypeClasspathProvision;
import net.officefloor.eclipse.extension.governancesource.GovernanceSourceExtension;
import net.officefloor.eclipse.extension.governancesource.GovernanceSourceExtensionContext;
import net.officefloor.eclipse.extension.open.ExtensionOpener;
import net.officefloor.eclipse.extension.open.ExtensionOpenerContext;
import net.officefloor.eclipse.extension.util.SourceExtensionUtil;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.plugin.governance.clazz.ClassGovernanceSource;

import org.eclipse.swt.widgets.Composite;

/**
 * {@link GovernanceSourceExtension} for {@link ClassGovernanceSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassGovernanceSourceExtension implements
		GovernanceSourceExtension<Object, Indexed, ClassGovernanceSource>,
		ExtensionClasspathProvider, ExtensionOpener {

	/*
	 * ================ GovernanceSourceExtension =========================
	 */

	@Override
	public Class<ClassGovernanceSource> getGovernanceSourceClass() {
		return ClassGovernanceSource.class;
	}

	@Override
	public String getGovernanceSourceLabel() {
		return "Class";
	}

	@Override
	public void createControl(Composite page,
			final GovernanceSourceExtensionContext context) {

		// Provide property for class name
		SourceExtensionUtil.loadPropertyLayout(page);
		SourceExtensionUtil.createPropertyClass("Class",
				ClassGovernanceSource.CLASS_NAME_PROPERTY_NAME, page, context,
				null);
	}

	/*
	 * ======================= ExtensionClasspathProvider ======================
	 */

	@Override
	public ClasspathProvision[] getClasspathProvisions() {
		return new ClasspathProvision[] { new TypeClasspathProvision(
				ClassGovernanceSource.class) };
	}

	/*
	 * ========================= ExtensionOpener ==============================
	 */

	@Override
	public void openSource(ExtensionOpenerContext context) throws Exception {

		// Obtain the name of the class
		String className = context.getPropertyList().getPropertyValue(
				ClassGovernanceSource.CLASS_NAME_PROPERTY_NAME, null);

		// Ensure have class name
		if (EclipseUtil.isBlank(className)) {
			throw new Exception("No class name provided");
		}

		// Translate class name to resource name
		String resourceName = className.replace('.', '/') + ".class";

		// Open the class file
		context.openClasspathResource(resourceName);
	}

}