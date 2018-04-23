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

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.eclipse.editor.AdaptedModelVisualFactoryContext;
import net.officefloor.eclipse.ide.editor.AbstractChildConfigurableItem;
import net.officefloor.model.section.FunctionNamespaceModel;
import net.officefloor.model.section.FunctionNamespaceModel.FunctionNamespaceEvent;
import net.officefloor.model.section.ManagedFunctionModel;
import net.officefloor.model.section.ManagedFunctionModel.ManagedFunctionEvent;
import net.officefloor.model.section.SectionChanges;
import net.officefloor.model.section.SectionModel;

/**
 * Configuration for the {@link ManagedFunctionModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionItem extends
		AbstractChildConfigurableItem<SectionModel, SectionChanges, FunctionNamespaceModel, FunctionNamespaceEvent, ManagedFunctionModel, ManagedFunctionEvent> {

	@Override
	protected ManagedFunctionModel createPrototype() {
		return new ManagedFunctionModel();
	}

	@Override
	protected List<ManagedFunctionModel> getModels(FunctionNamespaceModel parentModel) {
		return parentModel.getManagedFunctions();
	}

	@Override
	protected Pane createVisual(ManagedFunctionModel model,
			AdaptedModelVisualFactoryContext<ManagedFunctionModel> context) {
		VBox container = new VBox();
		HBox heading = context.addNode(container, new HBox());
		context.label(heading);
		context.addNode(heading, new Button("A")).setOnAction((event) -> {
			context.action((ctx) -> {

				// Obtain the details
				ManagedFunctionModel managedFunction = ctx.getModel();
				String functionName = managedFunction.getManagedFunctionName();

				// Obtain the managed function type
				ManagedFunctionType<?, ?> managedFunctionType = null;

				// Add the function
				ctx.getChangeExecutor().execute(this.getConfigurableContext().getOperations().addFunction(functionName,
						managedFunction, managedFunctionType));
			});
		});
		context.addNode(container, context.childGroup(ManagedFunctionObjectItem.class.getSimpleName(), new VBox()));
		return container;
	}

	@Override
	protected FunctionNamespaceEvent[] parentChangeEvents() {
		return new FunctionNamespaceEvent[] { FunctionNamespaceEvent.ADD_MANAGED_FUNCTION,
				FunctionNamespaceEvent.REMOVE_MANAGED_FUNCTION };
	}

	@Override
	protected String getLabel(ManagedFunctionModel model) {
		return model.getManagedFunctionName();
	}

	@Override
	protected ManagedFunctionEvent[] changeEvents() {
		return new ManagedFunctionEvent[] { ManagedFunctionEvent.CHANGE_MANAGED_FUNCTION_NAME };
	}

	@Override
	protected void loadChildren(List<IdeChildrenGroup> children) {
		children.add(new IdeChildrenGroup(new ManagedFunctionObjectItem()));
	}

}