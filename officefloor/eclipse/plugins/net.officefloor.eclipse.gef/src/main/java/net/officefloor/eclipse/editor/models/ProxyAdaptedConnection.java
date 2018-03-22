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
package net.officefloor.eclipse.editor.models;

import java.util.List;

import org.eclipse.gef.mvc.fx.parts.IBendableContentPart.BendPoint;

import net.officefloor.eclipse.editor.AdaptedChild;
import net.officefloor.eclipse.editor.AdaptedConnection;
import net.officefloor.model.ConnectionModel;

/**
 * Proxy {@link AdaptedConnection} to enable creating connections.
 * 
 * @author Daniel Sagenschneider
 */
public class ProxyAdaptedConnection implements AdaptedConnection<ConnectionModel> {

	/**
	 * Source {@link AdaptedConnector}.
	 */
	private final AdaptedConnector<?> sourceConnector;

	/**
	 * {@link BendPoint} instances.
	 */
	private List<BendPoint> bendPoints = null;

	/**
	 * Instantiate.
	 * 
	 * @param sourceConnector
	 *            Source {@link AdaptedConnector}.
	 */
	public ProxyAdaptedConnection(AdaptedConnector<?> sourceConnector) {
		this.sourceConnector = sourceConnector;
	}

	/**
	 * Obtains the source {@link AdaptedConnector}.
	 * 
	 * @return Source {@link AdaptedConnector}.
	 */
	public AdaptedConnector<?> getSourceAdaptedConnector() {
		return this.sourceConnector;
	}

	/**
	 * Specifies the {@link BendPoint} instances.
	 * 
	 * @param bendPoints
	 *            {@link BendPoint} instances.
	 */
	public void setBendPoints(List<BendPoint> bendPoints) {
		this.bendPoints = bendPoints;
	}

	/**
	 * Obtains the target {@link BendPoint}.
	 * 
	 * @return Target {@link BendPoint}.
	 */
	public BendPoint getTargetBendPoint() {
		if (this.bendPoints.size() >= 2) {
			return this.bendPoints.get(this.bendPoints.size() - 1);
		}
		return null;
	}

	/*
	 * ================ AdaptedConnection ====================
	 */

	@Override
	public ConnectionModel getModel() {
		return null;
	}

	@Override
	public AdaptedChild<?> getSource() {
		throw new IllegalStateException("Should not obtain source for " + this.getClass().getSimpleName());
	}

	@Override
	public AdaptedChild<?> getTarget() {
		throw new IllegalStateException("Should not obtain target for " + this.getClass().getSimpleName());
	}

	@Override
	public void remove() {
		throw new IllegalStateException("Should not remove " + this.getClass().getSimpleName());
	}

}