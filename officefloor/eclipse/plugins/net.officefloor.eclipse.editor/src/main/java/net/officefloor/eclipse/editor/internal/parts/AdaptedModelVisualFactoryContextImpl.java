/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.eclipse.editor.internal.parts;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import net.officefloor.eclipse.editor.AdaptedActionVisualFactory;
import net.officefloor.eclipse.editor.AdaptedConnectorVisualFactory;
import net.officefloor.eclipse.editor.AdaptedModelVisualFactoryContext;
import net.officefloor.eclipse.editor.ModelAction;
import net.officefloor.model.Model;

/**
 * {@link AdaptedModelVisualFactoryContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class AdaptedModelVisualFactoryContextImpl<M extends Model> implements AdaptedModelVisualFactoryContext<M> {

	@Override
	public Label label(Pane parent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <N extends Node> N addNode(Pane parent, N node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <P extends Pane> P childGroup(String childGroupName, P parent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <N extends Region> Connector connector(AdaptedConnectorVisualFactory<N> visualFactory,
			Class... connectionModelClasses) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node createImageWithHover(Class<?> resourceClass, String imageFilePath, String hoverImageFilePath) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <R extends Model, O> void action(ModelAction<R, O, M> action) {
		// TODO Auto-generated method stub

	}

	@Override
	public <R extends Model, O> Node action(ModelAction<R, O, M> action, AdaptedActionVisualFactory visualFactory) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isPalettePrototype() {
		// TODO Auto-generated method stub
		return false;
	}

}