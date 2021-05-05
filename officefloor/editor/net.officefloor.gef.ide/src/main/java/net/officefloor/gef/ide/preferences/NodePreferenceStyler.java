/*-
 * #%L
 * [bundle] OfficeFloor Eclipse IDE
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

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
