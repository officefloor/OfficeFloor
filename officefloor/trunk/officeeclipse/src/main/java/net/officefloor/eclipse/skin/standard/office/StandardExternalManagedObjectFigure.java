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

import net.officefloor.eclipse.skin.office.ExternalManagedObjectFigure;
import net.officefloor.eclipse.skin.office.ExternalManagedObjectFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.model.office.ExternalManagedObjectModel;

/**
 * Standard {@link ExternalManagedObjectFigure}.
 * 
 * @author Daniel
 */
public class StandardExternalManagedObjectFigure extends
		AbstractOfficeFloorFigure implements ExternalManagedObjectFigure {

	/**
	 * {@link ExternalManagedObjectFigureContext}.
	 */
	private final ExternalManagedObjectFigureContext context;

	/**
	 * {@link Figure}.
	 */
	private final net.officefloor.eclipse.skin.standard.figure.ExternalManagedObjectFigure figure;

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link ExternalManagedObjectFigureContext}.
	 */
	public StandardExternalManagedObjectFigure(
			ExternalManagedObjectFigureContext context) {
		this.context = context;
		this.figure = new net.officefloor.eclipse.skin.standard.figure.ExternalManagedObjectFigure(
				this.getManagedObjectText(context.getScope()));
		this.setFigure(this.figure);
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
		this.figure.setManagedObjectName(this.getManagedObjectText(scope));
	}

	/**
	 * Obtains the {@link ExternalManagedObjectModel} text.
	 * 
	 * @param name
	 *            Name of the {@link ExternalManagedObjectModel}.
	 * @param scope
	 *            Scope of the {@link ExternalManagedObjectModel}.
	 * @return {@link ExternalManagedObjectModel} text.
	 */
	private String getManagedObjectText(String scope) {
		return this.context.getExternalManagedObjectName() + " (" + scope
				+ ")";
	}
}
