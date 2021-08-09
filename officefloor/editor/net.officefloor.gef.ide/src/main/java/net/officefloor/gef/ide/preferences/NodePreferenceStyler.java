/*-
 * #%L
 * [bundle] OfficeFloor Eclipse IDE
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
