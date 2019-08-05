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

import javafx.beans.property.Property;
import net.officefloor.gef.editor.AdaptedParent;
import net.officefloor.gef.ide.editor.AbstractItem;
import net.officefloor.model.Model;

/**
 * Style for the {@link Model} preferences.
 * 
 * @author Daniel Sagenschneider
 */
public class ModelPreferenceStyler<M extends Model> {

	/**
	 * {@link AbstractItem}.
	 */
	private final AbstractItem<?, ?, ?, ?, M, ?> item;

	/**
	 * Prototype {@link Model} for the item.
	 */
	private final M prototype;

	/**
	 * Label for the item.
	 */
	private final String itemLabel;

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
	 * Indicates if {@link AdaptedParent}.
	 */
	private final boolean isParent;

	/**
	 * Preferences to change.
	 */
	private final Map<String, String> preferencesToChange;

	/**
	 * Instantiate.
	 * 
	 * @param item                {@link AbstractItem}.
	 * @param prototype           Prototype {@link Model} for the item.
	 * @param itemLabel           Label for the item.
	 * @param isParent            Indicates if {@link AdaptedParent}.
	 * @param style               {@link Property} to receive changes to the style.
	 *                            Also, provides the initial style.
	 * @param defaultStyle        Default style.
	 * @param preferencesToChange {@link Map} to load with the
	 *                            {@link EditorPreferences} changes.
	 */
	public ModelPreferenceStyler(AbstractItem<?, ?, ?, ?, M, ?> item, M prototype, String itemLabel, boolean isParent,
			Property<String> style, String defaultStyle, Map<String, String> preferencesToChange) {
		this.item = item;
		this.prototype = prototype;
		this.itemLabel = itemLabel;
		this.isParent = isParent;
		this.style = style;
		this.defaultStyle = defaultStyle == null ? "" : defaultStyle;
		this.preferencesToChange = preferencesToChange;
	}

}