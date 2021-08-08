/*-
 * #%L
 * [bundle] Section Editor
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

package net.officefloor.gef.section;

import java.util.List;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import net.officefloor.gef.editor.AdaptedChildVisualFactoryContext;
import net.officefloor.gef.editor.DefaultConnectors;
import net.officefloor.gef.ide.editor.AbstractItem;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.section.ExternalManagedObjectModel;
import net.officefloor.model.section.ExternalManagedObjectModel.ExternalManagedObjectEvent;
import net.officefloor.model.section.SectionChanges;
import net.officefloor.model.section.SectionManagedObjectDependencyModel;
import net.officefloor.model.section.SectionManagedObjectDependencyModel.SectionManagedObjectDependencyEvent;
import net.officefloor.model.section.SectionManagedObjectDependencyToExternalManagedObjectModel;
import net.officefloor.model.section.SectionManagedObjectDependencyToSectionManagedObjectModel;
import net.officefloor.model.section.SectionManagedObjectModel;
import net.officefloor.model.section.SectionManagedObjectModel.SectionManagedObjectEvent;
import net.officefloor.model.section.SectionModel;

/**
 * Configuration for the {@link SectionManagedObjectDependencyModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectDependencyItem extends
		AbstractItem<SectionModel, SectionChanges, SectionManagedObjectModel, SectionManagedObjectEvent, SectionManagedObjectDependencyModel, SectionManagedObjectDependencyEvent> {

	@Override
	public SectionManagedObjectDependencyModel prototype() {
		return new SectionManagedObjectDependencyModel("Dependency", null);
	}

	@Override
	public IdeExtractor extract() {
		return new IdeExtractor((p) -> p.getSectionManagedObjectDependencies(),
				SectionManagedObjectEvent.ADD_SECTION_MANAGED_OBJECT_DEPENDENCY,
				SectionManagedObjectEvent.REMOVE_SECTION_MANAGED_OBJECT_DEPENDENCY);
	}

	@Override
	public void loadToParent(SectionManagedObjectModel parentModel, SectionManagedObjectDependencyModel itemModel) {
		parentModel.addSectionManagedObjectDependency(itemModel);
	}

	@Override
	public Pane visual(SectionManagedObjectDependencyModel model,
			AdaptedChildVisualFactoryContext<SectionManagedObjectDependencyModel> context) {
		HBox container = new HBox();
		context.label(container);
		context.addNode(container,
				context.connector(DefaultConnectors.OBJECT,
						SectionManagedObjectDependencyToSectionManagedObjectModel.class,
						SectionManagedObjectDependencyToExternalManagedObjectModel.class).getNode());
		return container;
	}

	@Override
	public IdeLabeller label() {
		return new IdeLabeller((m) -> m.getSectionManagedObjectDependencyName(),
				SectionManagedObjectDependencyEvent.CHANGE_SECTION_MANAGED_OBJECT_DEPENDENCY_NAME);
	}

	@Override
	protected void connections(List<IdeConnectionTarget<? extends ConnectionModel, ?, ?>> connections) {

		// Managed Object
		connections.add(new IdeConnection<>(SectionManagedObjectDependencyToSectionManagedObjectModel.class)
				.connectOne((s) -> s.getSectionManagedObject(), (c) -> c.getSectionManagedObjectDependency(),
						SectionManagedObjectDependencyEvent.CHANGE_SECTION_MANAGED_OBJECT)
				.to(SectionManagedObjectModel.class)
				.many((t) -> t.getDependentSectionManagedObjects(), (c) -> c.getSectionManagedObject(),
						SectionManagedObjectEvent.ADD_SECTION_MANAGED_OBJECT_DEPENDENCY,
						SectionManagedObjectEvent.REMOVE_SECTION_MANAGED_OBJECT_DEPENDENCY)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(
							ctx.getOperations().linkSectionManagedObjectDependencyToSectionManagedObject(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations()
							.removeSectionManagedObjectDependencyToSectionManagedObject(ctx.getModel()));
				}));

		// External Object
		connections.add(new IdeConnection<>(SectionManagedObjectDependencyToExternalManagedObjectModel.class)
				.connectOne((s) -> s.getExternalManagedObject(), (c) -> c.getSectionManagedObjectDependency(),
						SectionManagedObjectDependencyEvent.CHANGE_EXTERNAL_MANAGED_OBJECT)
				.to(ExternalManagedObjectModel.class)
				.many((t) -> t.getDependentSectionManagedObjects(), (c) -> c.getExternalManagedObject(),
						ExternalManagedObjectEvent.ADD_DEPENDENT_SECTION_MANAGED_OBJECT,
						ExternalManagedObjectEvent.REMOVE_DEPENDENT_SECTION_MANAGED_OBJECT)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(
							ctx.getOperations().linkSectionManagedObjectDependencyToExternalManagedObject(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations()
							.removeSectionManagedObjectDependencyToExternalManagedObject(ctx.getModel()));
				}));
	}

}
