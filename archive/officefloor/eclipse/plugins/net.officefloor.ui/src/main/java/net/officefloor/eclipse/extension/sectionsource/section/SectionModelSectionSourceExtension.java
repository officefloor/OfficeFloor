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
package net.officefloor.eclipse.extension.sectionsource.section;

import net.officefloor.eclipse.extension.sectionsource.SectionSourceExtension;
import net.officefloor.eclipse.extension.sectionsource.SectionSourceExtensionContext;
import net.officefloor.model.impl.section.SectionModelSectionSource;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * {@link SectionSourceExtension} for the {@link SectionModelSectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class SectionModelSectionSourceExtension implements SectionSourceExtension<SectionModelSectionSource> {

	/*
	 * ==================== SectionSourceExtension ====================
	 */

	@Override
	public Class<SectionModelSectionSource> getSectionSourceClass() {
		return SectionModelSectionSource.class;
	}

	@Override
	public String getSectionSourceLabel() {
		return "Section";
	}

	@Override
	public void createControl(Composite page, SectionSourceExtensionContext context) {
		page.setLayout(new FillLayout());
		new Label(page, SWT.NONE).setText("No properties required for Section");
	}

}