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
import net.officefloor.eclipse.extension.teamsource.TeamSourceExtension;
import net.officefloor.eclipse.extension.teamsource.TeamSourceExtensionContext;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * {@link TeamSourceExtension} for the {@link OnePersonTeamSource}.
 *
 * @author Daniel Sagenschneider
 */
public class OnePersonTeamSourceExtension implements TeamSourceExtension<OnePersonTeamSource> {

	/*
	 * ================ TeamSourceExtension ==============================
	 */

	@Override
	public Class<OnePersonTeamSource> getTeamSourceClass() {
		return OnePersonTeamSource.class;
	}

	@Override
	public String getTeamSourceLabel() {
		return "One Person";
	}

	@Override
	public void createControl(Composite page, final TeamSourceExtensionContext context) {

		// Obtain the wait property
		final Property waitProperty = context.getPropertyList()
				.getOrAddProperty(OnePersonTeamSource.MAX_WAIT_TIME_PROPERTY_NAME);

		// Obtain initial value
		String initialValue = waitProperty.getValue();
		initialValue = (initialValue == null ? "100" : initialValue);

		// Add ability to change wait
		page.setLayout(new GridLayout(2, false));
		new Label(page, SWT.NONE).setText("Wait time (ms): ");
		final Text text = new Text(page, SWT.BORDER);
		text.setText(initialValue);
		text.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		text.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				// Change wait time
				String value = text.getText();
				waitProperty.setValue(value);

				// Notify property changed
				context.notifyPropertiesChanged();
			}
		});
	}

}