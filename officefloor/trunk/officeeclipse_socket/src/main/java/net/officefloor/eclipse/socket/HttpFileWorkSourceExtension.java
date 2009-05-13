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

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.extension.classpath.ClasspathProvision;
import net.officefloor.eclipse.extension.classpath.ExtensionClasspathProvider;
import net.officefloor.eclipse.extension.classpath.TypeClasspathProvision;
import net.officefloor.eclipse.extension.worksource.WorkSourceExtension;
import net.officefloor.eclipse.extension.worksource.WorkSourceExtensionContext;
import net.officefloor.plugin.work.http.file.HttpFileTask;
import net.officefloor.plugin.work.http.file.HttpFileWorkSource;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * {@link WorkSourceExtension} for the {@link HttpFileWorkSource}.
 * 
 * @author Daniel
 */
public class HttpFileWorkSourceExtension implements
		WorkSourceExtension<HttpFileTask, HttpFileWorkSource>,
		ExtensionClasspathProvider {

	/*
	 * ================ WorkLoaderExtension ====================
	 */

	@Override
	public Class<HttpFileWorkSource> getWorkSourceClass() {
		return HttpFileWorkSource.class;
	}

	@Override
	public String getWorkSourceLabel() {
		return "HTTP File";
	}

	@Override
	public void createControl(Composite page, WorkSourceExtensionContext context) {

		// TODO provide ability to associate extension with HTTP content type
		page.setLayout(new GridLayout(1, false));
		new Label(page, SWT.NONE)
				.setText("TODO: table with extension mapped to HTTP content type");
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
				HttpFileWorkSource.class) };
	}

}