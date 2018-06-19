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

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.extension.teamsource.TeamSourceExtension;
import net.officefloor.eclipse.extension.teamsource.TeamSourceExtensionContext;
import net.officefloor.frame.impl.spi.team.LeaderFollowerTeamSource;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * {@link TeamSourceExtension} for the {@link LeaderFollowerTeamSource}.
 *
 * @author Daniel Sagenschneider
 */
public class LeaderFollowerTeamSourceExtension implements TeamSourceExtension<LeaderFollowerTeamSource> {

	/*
	 * =============== TeamSourceExtension ==============================
	 */

	@Override
	public Class<LeaderFollowerTeamSource> getTeamSourceClass() {
		return LeaderFollowerTeamSource.class;
	}

	@Override
	public String getTeamSourceLabel() {
		return "Leader Follower Team";
	}

	@Override
	public void createControl(Composite page, final TeamSourceExtensionContext context) {

		// Obtain the properties
		PropertyList properties = context.getPropertyList();
		final Property sizeProperty = properties.getOrAddProperty(LeaderFollowerTeamSource.TEAM_SIZE_PROPERTY_NAME);

		// Default initial values
		String sizeValue = sizeProperty.getValue();
		if (sizeValue == null) {
			sizeValue = "10";
			sizeProperty.setValue(sizeValue);
		}

		// Add controls to change properties
		page.setLayout(new GridLayout(2, false));

		// Allow changing the size
		new Label(page, SWT.NONE).setText("Team size: ");
		final Text size = new Text(page, SWT.BORDER);
		size.setText(sizeValue);
		size.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		size.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				// Specify value for size property
				String sizeValue = size.getText();
				sizeProperty.setValue(sizeValue);

				// Notify change in size
				context.notifyPropertiesChanged();
			}
		});
	}

}