package net.officefloor.gef.ide.preferences;

import javafx.beans.property.Property;
import javafx.collections.ObservableMap;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import net.officefloor.gef.bridge.EnvironmentBridge;

/**
 * Styles the {@link Node}.
 * 
 * @author Daniel Sagenschneider
 */
public class NodePreferenceStyler extends AbstractPreferenceStyler {

	/**
	 * Identifier of preference.
	 */
	private final String preferenceId;

	/**
	 * {@link Node} for visual structure.
	 */
	private final Node visual;

	/**
	 * Default style.
	 */
	private final String defaultStyle;

	/**
	 * Style updater.
	 */
	private final Property<String> styleUpdater;

	/**
	 * Instantiate.
	 * 
	 * @param preferenceId        Identifier of preference.
	 * @param visual              {@link Node} for visual structure.
	 * @param defaultStyle        Default style.
	 * @param styleUpdater        Style updater.
	 * @param preferencesToChange Preferences to change.
	 * @param envBridge           {@link EnvironmentBridge}.
	 * @param backgroundColour    Background {@link Color}.
	 */
	public NodePreferenceStyler(String preferenceId, Node visual, String defaultStyle, Property<String> styleUpdater,
			ObservableMap<String, PreferenceValue> preferencesToChange, EnvironmentBridge envBridge,
			Color backgroundColour) {
		super(preferencesToChange, envBridge, backgroundColour);
		this.preferenceId = preferenceId;
		this.visual = visual;
		this.defaultStyle = defaultStyle;
		this.styleUpdater = styleUpdater;
	}

	/*
	 * ===================== AbstractPreferenceStyler ========================
	 */

	@Override
	protected PreferenceConfiguration init() {
		return new PreferenceConfiguration(this.preferenceId, this.visual, this.defaultStyle, this.styleUpdater, null,
				null);
	}

}