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
package net.officefloor.eclipse.office.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.officefloor.compile.WorkEntry;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorNodeEditPart;
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.model.office.ExternalManagedObjectModel;
import net.officefloor.model.office.ExternalManagedObjectModel.ExternalManagedObjectEvent;

import org.eclipse.draw2d.ActionEvent;
import org.eclipse.draw2d.ActionListener;
import org.eclipse.draw2d.Clickable;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;

/**
 * {@link org.eclipse.gef.EditPart} for the
 * {@link net.officefloor.model.office.ExternalManagedObjectModel}.
 * 
 * @author Daniel
 */
public class ExternalManagedObjectEditPart extends
		AbstractOfficeFloorNodeEditPart<ExternalManagedObjectModel> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorNodeEditPart#populateConnectionSourceModels(java.util.List)
	 */
	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		// Not a source
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorNodeEditPart#populateConnectionTargetModels(java.util.List)
	 */
	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		models.addAll(this.getCastedModel().getAdministrators());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#populatePropertyChangeHandlers(java.util.List)
	 */
	@Override
	protected void populatePropertyChangeHandlers(
			List<PropertyChangeHandler<?>> handlers) {
		handlers.add(new PropertyChangeHandler<ExternalManagedObjectEvent>(
				ExternalManagedObjectEvent.values()) {
			@Override
			protected void handlePropertyChange(
					ExternalManagedObjectEvent property, PropertyChangeEvent evt) {
				switch (property) {
				case ADD_ADMINISTRATOR:
				case REMOVE_ADMINISTRATOR:
					ExternalManagedObjectEditPart.this
							.refreshTargetConnections();
					break;
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
	 */
	@Override
	protected IFigure createFigure() {
		Figure figure = new Figure();
		figure.setBackgroundColor(ColorConstants.lightGray);
		figure.setOpaque(true);
		figure.setLayoutManager(new FlowLayout(true));

		// Name of external managed object
		Label name = new Label(this.getCastedModel().getName());
		figure.add(name);

		// Scope of external managed object
		String scopeName = this.getCastedModel().getScope();
		if (scopeName == null) {
			scopeName = "not specified";
		}
		final Label scope = new Label(scopeName);
		scope.setBackgroundColor(ColorConstants.lightBlue);
		scope.setOpaque(true);
		Clickable clickableScope = new Clickable(scope);
		clickableScope.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				// Obtain the next scope
				String nextScope = ExternalManagedObjectEditPart.this
						.getNextExternalManagedObjectScope(scope.getText());

				// Change scope on model
				ExternalManagedObjectEditPart.this.getCastedModel().setScope(
						nextScope);

				// Change scope on figure
				scope.setText(nextScope);
			}
		});
		figure.add(clickableScope);

		// Return figure
		return figure;
	}

	/**
	 * Obtains the next scope for the {@link ExternalManagedObjectModel}.
	 * 
	 * @param currentScope
	 *            Current scope (may be <code>null</code>).
	 * @return Next scope.
	 */
	protected String getNextExternalManagedObjectScope(String currentScope) {

		// Find the index of the current scope
		int currentScopeIndex = -1;
		for (int i = 0; i < WorkEntry.MANAGED_OBJECT_SCOPES.length; i++) {
			if (WorkEntry.MANAGED_OBJECT_SCOPES[i].equals(currentScope)) {
				currentScopeIndex = i;
			}
		}

		// Move to the next scope (and cycle back to first if necessary)
		int nextScopeIndex = (currentScopeIndex + 1)
				% WorkEntry.MANAGED_OBJECT_SCOPES.length;

		// Return the next scope
		return WorkEntry.MANAGED_OBJECT_SCOPES[nextScopeIndex];
	}

}
