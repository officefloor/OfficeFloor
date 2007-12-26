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
package net.officefloor.eclipse.common.editparts;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.model.ConnectionModel;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.eclipse.gef.editpolicies.ConnectionEndpointEditPolicy;

/**
 * Abstract Office Floor
 * {@link org.eclipse.gef.editparts.AbstractConnectionEditPart}.
 * 
 * @author Daniel
 */
public class OfficeFloorConnectionEditPart<M extends ConnectionModel> extends
		AbstractConnectionEditPart implements RemovableEditPart {

	/**
	 * Registry of {@link FigureFactory} by the model types.
	 */
	private static final Map<Class<?>, FigureFactory<?>> factories = new HashMap<Class<?>, FigureFactory<?>>();

	/**
	 * Registers the {@link FigureFactory} for the model type.
	 * 
	 * @param modelType
	 *            Type of model.
	 * @param figureFactory
	 *            {@link FigureFactory}.
	 */
	public synchronized static void registerFigureFactory(Class<?> modelType,
			FigureFactory<?> figureFactory) {
		factories.put(modelType, figureFactory);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractConnectionEditPart#createFigure()
	 */
	@SuppressWarnings("unchecked")
	protected IFigure createFigure() {

		IFigure figure;

		// Obtain figure factory
		FigureFactory figureFactory = factories.get(this.getCastedModel()
				.getClass());
		if (figureFactory != null) {
			// Create the figure
			figure = figureFactory.createFigure(this.getCastedModel());
		} else {
			// Create the default connection figure
			PolylineConnection connFigure = (PolylineConnection) super
					.createFigure();
			connFigure.setTargetDecoration(new PolygonDecoration());
			figure = connFigure;
		}

		// Indicate if able to remove connection
		if (!this.getCastedModel().isRemovable()) {
			figure.setForegroundColor(ColorConstants.gray);
		}

		// Return the figure
		return figure;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
	 */
	protected void createEditPolicies() {
		this.installEditPolicy(EditPolicy.CONNECTION_ENDPOINTS_ROLE,
				new ConnectionEndpointEditPolicy());
	}

	/**
	 * Obtains the specific Model.
	 * 
	 * @return Specific Model.
	 */
	@SuppressWarnings( { "unchecked" })
	public M getCastedModel() {
		return (M) this.getModel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.RemovableEditPart#delete()
	 */
	public void delete() {
		if (this.getCastedModel().isRemovable()) {
			this.getCastedModel().remove();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.RemovableEditPart#undelete()
	 */
	public void undelete() {
		this.getCastedModel().connect();
	}
}
