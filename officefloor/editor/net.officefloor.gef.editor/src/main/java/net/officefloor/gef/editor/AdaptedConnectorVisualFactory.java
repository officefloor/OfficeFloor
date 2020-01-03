package net.officefloor.gef.editor;

import org.eclipse.gef.fx.nodes.GeometryNode;

import javafx.scene.layout.Region;

/**
 * Factory for the creation of the {@link GeometryNode}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdaptedConnectorVisualFactory<N extends Region> {

	/**
	 * Creates the {@link GeometryNode}.
	 * 
	 * @param context
	 *            {@link AdaptedConnectorVisualFactoryContext}.
	 * @return New {@link GeometryNode}.
	 */
	N createGeometryNode(AdaptedConnectorVisualFactoryContext context);

}