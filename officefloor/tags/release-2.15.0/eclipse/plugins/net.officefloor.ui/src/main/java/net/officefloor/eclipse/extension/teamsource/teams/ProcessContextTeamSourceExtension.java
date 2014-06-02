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
package net.officefloor.eclipse.extension.teamsource.teams;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import net.officefloor.eclipse.extension.teamsource.TeamSourceExtension;
import net.officefloor.eclipse.extension.teamsource.TeamSourceExtensionContext;
import net.officefloor.frame.impl.spi.team.ProcessContextTeamSource;

/**
 * {@link TeamSourceExtension} for the {@link ProcessContextTeamSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessContextTeamSourceExtension implements
		TeamSourceExtension<ProcessContextTeamSource> {

	/*
	 * ================= TeamSourceExtension ============================
	 */

	@Override
	public Class<ProcessContextTeamSource> getTeamSourceClass() {
		return ProcessContextTeamSource.class;
	}

	@Override
	public String getTeamSourceLabel() {
		return "Process Context Team";
	}

	@Override
	public void createControl(Composite page, TeamSourceExtensionContext context) {
		page.setLayout(new GridLayout());
		new Label(page, SWT.NONE).setText("No properties required for "
				+ ProcessContextTeamSource.class.getSimpleName());
	}

}