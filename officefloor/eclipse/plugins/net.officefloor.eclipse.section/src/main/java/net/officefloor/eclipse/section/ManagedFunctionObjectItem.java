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
package net.officefloor.eclipse.section;

import java.util.List;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import net.officefloor.eclipse.editor.AdaptedModelVisualFactoryContext;
import net.officefloor.eclipse.ide.editor.AbstractChildConfigurableItem;
import net.officefloor.model.section.ManagedFunctionModel;
import net.officefloor.model.section.ManagedFunctionModel.ManagedFunctionEvent;
import net.officefloor.model.section.ManagedFunctionObjectModel;
import net.officefloor.model.section.ManagedFunctionObjectModel.ManagedFunctionObjectEvent;
import net.officefloor.model.section.SectionChanges;
import net.officefloor.model.section.SectionModel;

/**
 * Configuration for the {@link ManagedFunctionObjectModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionObjectItem extends
		AbstractChildConfigurableItem<SectionModel, SectionChanges, ManagedFunctionModel, ManagedFunctionEvent, ManagedFunctionObjectModel, ManagedFunctionObjectEvent> {

	@Override
	protected ManagedFunctionObjectModel createPrototype() {
		return new ManagedFunctionObjectModel();
	}

	@Override
	protected List<ManagedFunctionObjectModel> getModels(ManagedFunctionModel parentModel) {
		return parentModel.getManagedFunctionObjects();
	}

	@Override
	protected Pane createVisual(ManagedFunctionObjectModel model,
			AdaptedModelVisualFactoryContext<ManagedFunctionObjectModel> context) {
		HBox container = new HBox();
		context.addNode(container, context.connector());
		context.label(container);
		return container;
	}

	@Override
	protected ManagedFunctionEvent[] parentChangeEvents() {
		return new ManagedFunctionEvent[] { ManagedFunctionEvent.ADD_MANAGED_FUNCTION_OBJECT,
				ManagedFunctionEvent.REMOVE_MANAGED_FUNCTION_OBJECT };
	}

	@Override
	protected String getLabel(ManagedFunctionObjectModel model) {
		return model.getObjectName();
	}

	@Override
	protected ManagedFunctionObjectEvent[] changeEvents() {
		return new ManagedFunctionObjectEvent[] { ManagedFunctionObjectEvent.CHANGE_OBJECT_NAME };
	}

	@Override
	protected void loadChildren(
			List<AbstractChildConfigurableItem<SectionModel, SectionChanges, ManagedFunctionModel, ManagedFunctionEvent, ManagedFunctionObjectModel, ManagedFunctionObjectEvent>.IdeChildrenGroup> childGroups) {
	}

}