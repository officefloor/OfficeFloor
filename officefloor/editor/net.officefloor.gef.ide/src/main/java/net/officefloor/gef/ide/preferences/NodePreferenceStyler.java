/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.gef.ide.preferences;

import java.util.Map;
import java.util.function.Consumer;

import javafx.beans.property.Property;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/**
 * Styles the {@link Node}.
 * 
 * @author Daniel Sagenschneider
 */
public class NodePreferenceStyler {

	/**
	 * Title.
	 */
	private final String title;

	/**
	 * Message.
	 */
	private final String message;

	/**
	 * {@link Node} to extract the structure.
	 */
	private final Node node;

	/**
	 * Identifier within the {@link EditorPreferences} for the style.
	 */
	private final String preferenceStyleId;

	/**
	 * {@link Property} to receive changes to the style. Also, provides the initial
	 * style.
	 */
	private final Property<String> style;

	/**
	 * Default style.
	 */
	private final String defaultStyle;

	/**
	 * Preferences to change.
	 */
	private final Map<String, String> preferencesToChange;

	/**
	 * {@link Scene}.
	 */
	private final Scene scene;

	/**
	 * {@link Pane}.
	 */
	private final Pane pane;

	/**
	 * Instantiate.
	 * 
	 * @param title               Title.
	 * @param message             Message.
	 * @param node                {@link Node} to extract the structure.
	 * @param preferenceStyleId   Identifier within the {@link EditorPreferences}
	 *                            for the style.
	 * @param style               {@link Property} to receive changes to the style.
	 *                            Also, provides the initial style.
	 * @param defaultStyle        Default style.
	 * @param preferencesToChange Preferences to change.
	 * @param scene               {@link Scene}.
	 */
	public NodePreferenceStyler(String title, String message, Node node, String preferenceStyleId,
			Property<String> style, String defaultStyle, Map<String, String> preferencesToChange, Scene scene,
			Consumer<Pane> closeHandler) {
		this.title = title;
		this.message = message;
		this.node = node;
		this.preferenceStyleId = preferenceStyleId;
		this.style = style;
		this.defaultStyle = defaultStyle == null ? "" : defaultStyle;
		this.preferencesToChange = preferencesToChange;
		this.scene = scene;

		// Create title
		Label titleLabel = new Label(title);
		Label messageLabel = new Label(message);

		// Create the container
		VBox container = new VBox(titleLabel, messageLabel);

		// Specify the pane
		this.pane = null;
	}

}