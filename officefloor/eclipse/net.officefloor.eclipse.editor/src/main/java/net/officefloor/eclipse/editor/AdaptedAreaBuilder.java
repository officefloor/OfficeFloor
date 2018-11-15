/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.eclipse.editor;

import java.net.URL;

import javafx.beans.property.Property;
import javafx.scene.Parent;
import net.officefloor.model.Model;

/**
 * Builds an {@link AdaptedChild}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdaptedAreaBuilder<R extends Model, O, M extends Model, E extends Enum<E>> {

	/**
	 * Obtains the configuration path.
	 * 
	 * @return Configuration path.
	 */
	String getConfigurationPath();

	/**
	 * Obtains the {@link Model} {@link Class}.
	 * 
	 * @return {@link Model} {@link Class}.
	 */
	Class<M> getModelClass();

	/**
	 * <p>
	 * Obtains the {@link Property} to the style sheet rules for the
	 * {@link AdaptedChild}.
	 * <p>
	 * Note: this is <strong>NOT</strong> the style sheet {@link URL}. This is the
	 * style sheet rules (content of style sheet) and the {@link AdaptedChild} will
	 * handle making available to {@link Parent} as a {@link URL}.
	 * 
	 * @return {@link Property} to the style sheet rules.
	 */
	Property<String> style();

	/**
	 * Configures an {@link ModelAction} for creating the area {@link Model}.
	 * 
	 * @param action        {@link ModelAction}.
	 * @param visualFactory {@link AdaptedActionVisualFactory}.
	 */
	void create(ModelAction<R, O, M> action, AdaptedActionVisualFactory visualFactory);

	/**
	 * Configures an {@link ModelAction} for the area {@link Model}.
	 * 
	 * @param action        {@link ModelAction}.
	 * @param visualFactory {@link AdaptedActionVisualFactory}.
	 */
	void action(ModelAction<R, O, M> action, AdaptedActionVisualFactory visualFactory);

}