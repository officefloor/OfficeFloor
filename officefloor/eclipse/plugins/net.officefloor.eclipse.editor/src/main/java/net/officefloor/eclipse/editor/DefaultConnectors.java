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
package net.officefloor.eclipse.editor;

import org.eclipse.gef.fx.nodes.GeometryNode;
import org.eclipse.gef.geometry.planar.Ellipse;
import org.eclipse.gef.geometry.planar.Point;
import org.eclipse.gef.geometry.planar.Polygon;

import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

/**
 * Default {@link GeometryNode} instances for {@link AdaptedConnector}
 * instances.
 * 
 * @author Daniel Sagenschneider
 */
public class DefaultConnectors {

	/**
	 * {@link AdaptedConnectorVisualFactory} for flow connector.
	 */
	public static AdaptedConnectorVisualFactory<Region> FLOW = (context) -> {
		final double X_LEFT = 0;
		final double X_STEM = 5;
		final double X_TIP = 9;
		final double Y_TOP = 0;
		final double Y_BOTTOM = 10;
		final double Y_STEM_INSET = 2;
		final double Y_STEM_TOP = Y_TOP + Y_STEM_INSET;
		final double Y_STEM_BOTTOM = Y_BOTTOM - Y_STEM_INSET;
		final double Y_TIP = (Y_BOTTOM - Y_TOP) / 2;
		GeometryNode<Polygon> node = new GeometryNode<>(new Polygon(new Point(X_LEFT, Y_STEM_TOP),
				new Point(X_STEM, Y_STEM_TOP), new Point(X_STEM, Y_TOP), new Point(X_TIP, Y_TIP),
				new Point(X_STEM, Y_BOTTOM), new Point(X_STEM, Y_STEM_BOTTOM), new Point(X_LEFT, Y_STEM_BOTTOM)));
		node.setFill(Color.BLACK);
		return node;
	};

	/**
	 * {@link AdaptedConnectorVisualFactory} for object connector.
	 */
	public static AdaptedConnectorVisualFactory<Region> OBJECT = (context) -> {
		GeometryNode<Ellipse> node = new GeometryNode<Ellipse>(new Ellipse(0, 0, 12, 6));
		node.setFill(Color.BLACK);
		return node;
	};

	/**
	 * TODO provide specific connector.
	 * 
	 * {@link AdaptedConnectorVisualFactory} for team connector.
	 */
	public static AdaptedConnectorVisualFactory<Region> TEAM = FLOW;

	/**
	 * TODO provide specific connector.
	 * 
	 * {@link AdaptedConnectorVisualFactory} for derivative connector.
	 */
	public static AdaptedConnectorVisualFactory<Region> DERIVE = FLOW;

	/**
	 * All access via static methods.
	 */
	private DefaultConnectors() {
	}

}