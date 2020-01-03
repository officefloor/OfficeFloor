package net.officefloor.gef.editor;

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import net.officefloor.model.Model;

/**
 * Context for the {@link AdaptedChildVisualFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdaptedChildVisualFactoryContext<M extends Model>
		extends AdaptedModelVisualFactoryContext<M>, AdaptedConnectorVisualFactoryContext {

	/**
	 * Convenience method to add the {@link AdaptedModel} {@link Label} to the
	 * {@link Pane}.
	 * 
	 * @param parent {@link Pane}.
	 * @return Added {@link Label}.
	 */
	Label label(Pane parent);

	/**
	 * Specifies the {@link Pane} for the child group.
	 *
	 * @param                <P> Parent {@link Pane} type.
	 * @param childGroupName Name of the child group.
	 * @param parent         {@link Pane} to add the child group visuals.
	 * @return Input {@link Pane}.
	 */
	<P extends Pane> P childGroup(String childGroupName, P parent);

	/**
	 * <p>
	 * Indicates if palette prototype.
	 * <p>
	 * This allows for visual to not show actions or connectors that would be
	 * confusing (and error) if used from the palette.
	 * 
	 * @return <code>true</code> if the palette prototype.
	 */
	boolean isPalettePrototype();

}