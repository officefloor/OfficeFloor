/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

package net.officefloor.eclipse.extension.officesource.office;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import net.officefloor.eclipse.extension.officesource.OfficeSourceExtension;
import net.officefloor.eclipse.extension.officesource.OfficeSourceExtensionContext;
import net.officefloor.model.impl.office.OfficeModelOfficeSource;
import net.officefloor.model.office.OfficeModel;

/**
 * {@link OfficeSourceExtension} for the {@link OfficeModel}.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeModelOfficeSourceExtension implements
		OfficeSourceExtension<OfficeModelOfficeSource> {

	@Override
	public Class<OfficeModelOfficeSource> getOfficeSourceClass() {
		return OfficeModelOfficeSource.class;
	}

	@Override
	public String getOfficeSourceLabel() {
		return "Office";
	}

	@Override
	public void createControl(Composite page,
			OfficeSourceExtensionContext context) {
		page.setLayout(new GridLayout());
		new Label(page, SWT.NONE).setText("No properties required for "
				+ OfficeModelOfficeSource.class.getSimpleName());
	}

}