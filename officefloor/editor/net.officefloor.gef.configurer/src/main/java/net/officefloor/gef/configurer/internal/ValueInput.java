package net.officefloor.gef.configurer.internal;

import javafx.scene.Node;
import javafx.scene.Scene;
import net.officefloor.gef.configurer.Builder;

/**
 * Value input.
 * 
 * @author Daniel Sagenschneider
 */
public interface ValueInput {

	/**
	 * Obtains the {@link Node} for the input.
	 * 
	 * @return {@link Node} for the input.
	 */
	Node getNode();

	/**
	 * <p>
	 * Invoked once {@link Node} is connected to the {@link Scene}.
	 * <p>
	 * This allows {@link Scene} based activation of the {@link Node} (e.g.
	 * configuring a style sheet).
	 */
	default void activate() {
	}

	/**
	 * Invoked on reload of {@link Builder}. This allows hooking in to changing view
	 * on reload.
	 */
	default void reload() {
	}

}