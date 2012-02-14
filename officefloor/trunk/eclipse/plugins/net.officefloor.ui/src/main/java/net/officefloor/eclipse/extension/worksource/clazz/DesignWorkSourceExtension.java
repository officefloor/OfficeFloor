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

package net.officefloor.eclipse.extension.worksource.clazz;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.extension.worksource.TaskDocumentationContext;
import net.officefloor.eclipse.extension.worksource.WorkSourceExtension;
import net.officefloor.eclipse.extension.worksource.WorkSourceExtensionContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.plugin.work.design.DesignWorkSource;

/**
 * {@link WorkSourceExtension} for the {@link DesignWorkSource}.
 *
 * @author Daniel Sagenschneider
 */
public class DesignWorkSourceExtension implements
		WorkSourceExtension<Work, DesignWorkSource> {

	/*
	 * ================== WorkSourceExtension =================================
	 */

	@Override
	public Class<DesignWorkSource> getWorkSourceClass() {
		return DesignWorkSource.class;
	}

	@Override
	public String getWorkSourceLabel() {
		return "Design class";
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

	@Override
	public String getTaskDocumentation(TaskDocumentationContext context)
			throws Throwable {
		// No documentation
		return null;
	}

}