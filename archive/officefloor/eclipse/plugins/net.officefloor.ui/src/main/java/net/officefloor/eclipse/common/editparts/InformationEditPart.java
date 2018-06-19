/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.eclipse.common.editparts;

import java.beans.PropertyChangeEvent;

import org.eclipse.draw2d.Label;
import org.eclipse.gef.EditPart;

import net.officefloor.eclipse.common.models.InformationModel;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.frame.api.build.None;

/**
 * {@link EditPart} for the {@link InformationModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class InformationEditPart extends AbstractOfficeFloorEditPart<InformationModel, None, OfficeFloorFigure> {

	/**
	 * ===================== EditPart =========================
	 */

	@Override
	protected Class<None> getPropertyChangeEventType() {
		return None.class;
	}

	@Override
	protected void handlePropertyChange(None property, PropertyChangeEvent evt) {
	}

	@Override
	protected OfficeFloorFigure createOfficeFloorFigure() {
		return new InformationFigure(this.getCastedModel());
	}

	/**
	 * {@link OfficeFloorFigure} for the {@link InformationModel}.
	 */
	private static class InformationFigure extends AbstractOfficeFloorFigure {

		/**
		 * Configure.
		 * 
		 * @param information
		 *            {@link InformationModel}.
		 */
		private InformationFigure(InformationModel information) {
			this.setFigure(new Label(information.getText()));
		}
	}

}
