/*-
 * #%L
 * [bundle] Section Editor
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

package net.officefloor.gef.section;

import java.util.List;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.gef.editor.AdaptedChildVisualFactoryContext;
import net.officefloor.gef.editor.DefaultConnectors;
import net.officefloor.gef.editor.DefaultImages;
import net.officefloor.gef.ide.editor.AbstractItem;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.section.FunctionModel;
import net.officefloor.model.section.FunctionModel.FunctionEvent;
import net.officefloor.model.section.FunctionNamespaceModel;
import net.officefloor.model.section.FunctionNamespaceModel.FunctionNamespaceEvent;
import net.officefloor.model.section.ManagedFunctionModel;
import net.officefloor.model.section.ManagedFunctionModel.ManagedFunctionEvent;
import net.officefloor.model.section.ManagedFunctionToFunctionModel;
import net.officefloor.model.section.SectionChanges;
import net.officefloor.model.section.SectionModel;

/**
 * Configuration for the {@link ManagedFunctionModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionItem extends
		AbstractItem<SectionModel, SectionChanges, FunctionNamespaceModel, FunctionNamespaceEvent, ManagedFunctionModel, ManagedFunctionEvent> {

	@Override
	public ManagedFunctionModel prototype() {
		return new ManagedFunctionModel("Managed Function");
	}

	@Override
	public IdeExtractor extract() {
		return new IdeExtractor((parent) -> parent.getManagedFunctions(), FunctionNamespaceEvent.ADD_MANAGED_FUNCTION,
				FunctionNamespaceEvent.REMOVE_MANAGED_FUNCTION);
	}

	@Override
	public void loadToParent(FunctionNamespaceModel parentModel, ManagedFunctionModel itemModel) {
		parentModel.addManagedFunction(itemModel);
	}

	@Override
	public Pane visual(ManagedFunctionModel model, AdaptedChildVisualFactoryContext<ManagedFunctionModel> context) {
		VBox container = new VBox();
		HBox heading = context.addNode(container, new HBox());
		context.label(heading);
		context.addNode(heading, context.action((ctx) -> {

			// Obtain the details
			ManagedFunctionModel managedFunction = ctx.getModel();
			String functionName = managedFunction.getManagedFunctionName();

			// Obtain the parent to load type
			FunctionNamespaceModel functionNamespace = (FunctionNamespaceModel) ctx.getAdaptedModel().getParent()
					.getModel();
			FunctionNamespaceItem functionNamespaceItem = new FunctionNamespaceItem().item(functionNamespace);
			FunctionNamespaceType functionNamespaceType = FunctionNamespaceItem.loadFunctionNamespaceType(
					functionNamespaceItem, this.getConfigurableContext().getEnvironmentBridge());

			// Obtain the particular managed function type
			ManagedFunctionType<?, ?> managedFunctionType = null;
			for (ManagedFunctionType<?, ?> type : functionNamespaceType.getManagedFunctionTypes()) {
				if (functionName.equals(type.getFunctionName())) {
					managedFunctionType = type;
				}
			}
			if (managedFunctionType == null) {
				throw new RuntimeException("Configuration out of sync");
			}

			// Add the function
			ctx.getChangeExecutor().execute(this.getConfigurableContext().getOperations().addFunction(functionName,
					managedFunction, managedFunctionType));
		}, DefaultImages.ADD));
		context.addNode(heading,
				context.connector(DefaultConnectors.DERIVE, ManagedFunctionToFunctionModel.class).getNode());
		context.addNode(container, context.childGroup(ManagedFunctionObjectItem.class.getSimpleName(), new VBox()));
		return container;
	}

	@Override
	public IdeLabeller label() {
		return new IdeLabeller((model) -> model.getManagedFunctionName(),
				ManagedFunctionEvent.CHANGE_MANAGED_FUNCTION_NAME);
	}

	@Override
	protected void children(List<IdeChildrenGroup> children) {
		children.add(new IdeChildrenGroup(new ManagedFunctionObjectItem()));
	}

	@Override
	protected void connections(List<IdeConnectionTarget<? extends ConnectionModel, ?, ?>> connections) {
		connections.add(new IdeConnection<>(ManagedFunctionToFunctionModel.class)
				.connectMany((source) -> source.getFunctions(), (conn) -> conn.getManagedFunction(),
						ManagedFunctionEvent.ADD_FUNCTION, ManagedFunctionEvent.REMOVE_FUNCTION)
				.to(FunctionModel.class).one((target) -> target.getManagedFunction(), (conn) -> conn.getFunction(),
						FunctionEvent.CHANGE_MANAGED_FUNCTION));
	}

}
