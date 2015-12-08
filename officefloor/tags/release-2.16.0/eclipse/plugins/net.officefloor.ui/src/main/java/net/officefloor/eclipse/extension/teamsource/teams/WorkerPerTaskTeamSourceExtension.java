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

import net.officefloor.eclipse.extension.teamsource.TeamSourceExtension;
import net.officefloor.eclipse.extension.teamsource.TeamSourceExtensionContext;
import net.officefloor.frame.impl.spi.team.WorkerPerTaskTeamSource;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * {@link TeamSourceExtension} for the {@link WorkerPerTaskTeamSource}.
 *
 * @author Daniel Sagenschneider
 */
public class WorkerPerTaskTeamSourceExtension implements
		TeamSourceExtension<WorkerPerTaskTeamSource> {

	/*
	 * =============== TeamSourceExtension ==============================
	 */

	@Override
	public Class<WorkerPerTaskTeamSource> getTeamSourceClass() {
		return WorkerPerTaskTeamSource.class;
	}

	@Override
	public String getTeamSourceLabel() {
		return "Worker per Task Team";
	}

	@Override
	public void createControl(Composite page,
			final TeamSourceExtensionContext context) {

		// Flag no properties required
		page.setLayout(new GridLayout(1, false));
		new Label(page, SWT.NONE).setText("No properties required");
	}

}