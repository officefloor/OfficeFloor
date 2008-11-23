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
package net.officefloor.eclipse.socket;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import net.officefloor.eclipse.extension.classpath.ClasspathProvision;
import net.officefloor.eclipse.extension.classpath.ExtensionClasspathProvider;
import net.officefloor.eclipse.extension.classpath.TypeClasspathProvision;
import net.officefloor.eclipse.extension.workloader.WorkLoaderExtension;
import net.officefloor.eclipse.extension.workloader.WorkLoaderExtensionContext;
import net.officefloor.eclipse.extension.workloader.WorkLoaderProperty;
import net.officefloor.work.WorkLoader;
import net.officefloor.work.http.file.HttpFileWorkLoader;

/**
 * {@link WorkLoaderExtension} for the {@link HttpFileWorkLoader}.
 * 
 * @author Daniel
 */
public class HttpFileWorkLoaderExtension implements WorkLoaderExtension,
		ExtensionClasspathProvider {

	/*
	 * ================ WorkLoaderExtension ====================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.extension.workloader.WorkLoaderExtension#
	 * getWorkLoaderClass()
	 */
	@Override
	public Class<? extends WorkLoader> getWorkLoaderClass() {
		return HttpFileWorkLoader.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.extension.workloader.WorkLoaderExtension#
	 * getDisplayName()
	 */
	@Override
	public String getDisplayName() {
		return "HTTP File";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.extension.workloader.WorkLoaderExtension#
	 * createControl(org.eclipse.swt.widgets.Composite,
	 * net.officefloor.eclipse.extension.workloader.WorkLoaderExtensionContext)
	 */
	@Override
	public List<WorkLoaderProperty> createControl(Composite page,
			WorkLoaderExtensionContext context) {

		// TODO provide ability to associate extension with HTTP content type
		page.setLayout(new GridLayout(1, false));
		new Label(page, SWT.NONE)
				.setText("TODO: table with extension mapped to HTTP content type");

		// No work loader properties
		return new ArrayList<WorkLoaderProperty>(0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.extension.workloader.WorkLoaderExtension#
	 * getSuggestedWorkName(java.util.List)
	 */
	@Override
	public String getSuggestedWorkName(List<WorkLoaderProperty> properties) {
		return "HttpFile";
	}

	/*
	 * =================== ExtensionClasspathProvider =====================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.extension.classpath.ExtensionClasspathProvider
	 * #getClasspathProvisions()
	 */
	@Override
	public ClasspathProvision[] getClasspathProvisions() {
		return new ClasspathProvision[] { new TypeClasspathProvision(
				HttpFileWorkLoader.class) };
	}

}
