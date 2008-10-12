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
package net.officefloor.eclipse.skin.standard.office;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;

import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.eclipse.skin.office.ExternalManagedObjectFigure;
import net.officefloor.eclipse.skin.standard.OfficeFloorFigureImpl;
import net.officefloor.model.office.ExternalManagedObjectModel;

/**
 * {@link OfficeFloorFigure} for the {@link ExternalManagedObjectModel}.
 * 
 * @author Daniel
 */
public class ExternalManagedObjectFigureImpl extends OfficeFloorFigureImpl
		implements ExternalManagedObjectFigure {

	/**
	 * Scope.
	 */
	private final Label scope;

	/**
	 * Initiate.
	 * 
	 * @param figure
	 *            {@link Figure}.
	 * @param scope
	 *            Scope {@link Label}.
	 */
	public ExternalManagedObjectFigureImpl(IFigure figure, Label scope) {
		super(figure);
		this.scope = scope;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.skin.office.ExternalManagedObjectFigure#setScope
	 * (java.lang.String)
	 */
	@Override
	public void setScope(String scope) {
		this.scope.setText(scope);
	}

}
