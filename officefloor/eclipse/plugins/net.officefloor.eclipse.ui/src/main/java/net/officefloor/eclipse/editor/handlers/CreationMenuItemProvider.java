/*******************************************************************************
 * Copyright (c) 2015, 2017 itemis AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthias Wienand (itemis AG) - initial API and implementation
 *
 *******************************************************************************/
package net.officefloor.eclipse.editor.handlers;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Provider;

import javafx.scene.Node;
import javafx.scene.control.Label;

public class CreationMenuItemProvider implements Provider<List<CreationMenuOnClickHandler.ICreationMenuItem>> {

	static class GeometricShapeItem implements CreationMenuOnClickHandler.ICreationMenuItem {

		@Override
		public Object createContent() {
			return "TODO palette";
		}

		@Override
		public Node createVisual() {
			return new Label("TODO palette create");
		}

	}

	@Override
	public List<CreationMenuOnClickHandler.ICreationMenuItem> get() {
		List<CreationMenuOnClickHandler.ICreationMenuItem> items = new ArrayList<>();
//		for (Object shape : AbstractEditorMain.createPaletteViewerContents()) {
//			items.add(new GeometricShapeItem());
//		}
		return items;
	}
}
