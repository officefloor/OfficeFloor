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
import net.officefloor.eclipse.extension.worksource.WorkSourceExtension;
import net.officefloor.eclipse.extension.worksource.WorkSourceExtensionContext;
import net.officefloor.plugin.work.http.html.form.HttpHtmlFormToBeanTask;
import net.officefloor.plugin.work.http.html.form.HttpHtmlFormToBeanWorkSource;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * {@link WorkSourceExtension} for he {@link HttpHtmlFormToBeanWorkSource}.
 *
 * @author Daniel Sagenschneider
 */
public class HttpHtmlFormToBeanWorkSourceExtension
		implements
		WorkSourceExtension<HttpHtmlFormToBeanTask, HttpHtmlFormToBeanWorkSource> {

	/*
	 * ================== WorkSourceExtension =================================
	 */

	@Override
	public Class<HttpHtmlFormToBeanWorkSource> getWorkSourceClass() {
		return HttpHtmlFormToBeanWorkSource.class;
	}

	@Override
	public String getWorkSourceLabel() {
		return "HTML Form to Bean";
	}

	@Override
	public void createControl(Composite page, WorkSourceExtensionContext context) {
		// TODO implement controls
		page.setLayout(new GridLayout());
		new Label(page, SWT.NONE).setText("TODO implement "
				+ this.getClass().getName());
	}

	@Override
	public String getSuggestedWorkName(PropertyList properties) {
		return null;
	}

}