package net.officefloor.gef.editor;

import javafx.scene.Node;
import net.officefloor.model.Model;

/**
 * Creates a visual {@link Node} for the parent {@link Model}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdaptedChildVisualFactory<M extends Model> {

	/**
	 * Creates the visual {@link Node}.
	 * 
	 * @param model   {@link AdaptedModel}.
	 * @param context {@link AdaptedChildVisualFactoryContext}.
	 * @return Visual {@link Node}.
	 */
	Node createVisual(M model, AdaptedChildVisualFactoryContext<M> context);

}