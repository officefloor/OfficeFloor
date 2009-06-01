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
package net.officefloor.eclipse.extension.teamsource.teams;

import net.officefloor.compile.properties.Property;
import net.officefloor.eclipse.extension.teamsource.TeamSourceExtension;
import net.officefloor.eclipse.extension.teamsource.TeamSourceExtensionContext;
import net.officefloor.frame.impl.spi.team.WorkerPerTaskTeamSource;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

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

		// Obtain the property
		final Property nameProperty = context.getPropertyList()
				.getOrAddProperty(
						WorkerPerTaskTeamSource.TEAM_NAME_PROPERTY_NAME);

		// Obtain the initial value
		String nameValue = nameProperty.getValue();
		nameValue = (nameValue == null ? "" : nameValue);

		// Add controls to change properties
		page.setLayout(new GridLayout(2, false));

		// Allow changing the name
		new Label(page, SWT.NONE).setText("Team name: ");
		final Text name = new Text(page, SWT.BORDER);
		name.setText(nameValue);
		name.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		name.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				// Specify value for name property
				String nameValue = name.getText();
				nameProperty.setValue(nameValue);

				// Notify change in name
				context.notifyPropertiesChanged();
			}
		});
	}

}