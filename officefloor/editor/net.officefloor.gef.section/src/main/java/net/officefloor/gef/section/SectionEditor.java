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

import javafx.scene.control.Label;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.gef.bridge.EnvironmentBridge;
import net.officefloor.gef.ide.editor.AbstractAdaptedIdeEditor;
import net.officefloor.gef.ide.editor.AbstractConfigurableItem;
import net.officefloor.gef.ide.editor.AbstractItem.ConfigurableContext;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.impl.section.SectionChangesImpl;
import net.officefloor.model.impl.section.SectionRepositoryImpl;
import net.officefloor.model.section.SectionChanges;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SectionModel.SectionEvent;
import net.officefloor.model.section.SectionRepository;

/**
 * {@link SectionModel} editor.
 * 
 * @author Daniel Sagenschneider
 */
public class SectionEditor extends AbstractAdaptedIdeEditor<SectionModel, SectionEvent, SectionChanges> {

	/**
	 * {@link SectionRepository}.
	 */
	private static final SectionRepository SECTION_REPOSITORY = new SectionRepositoryImpl(new ModelRepositoryImpl());

	/**
	 * Default instantiate.
	 * 
	 * @param envBridge {@link EnvironmentBridge}.
	 */
	public SectionEditor(EnvironmentBridge envBridge) {
		super(SectionModel.class, (model) -> new SectionChangesImpl(model), envBridge);
	}

	/*
	 * =============== AbstractIdeEditor ======================
	 */

	@Override
	protected void init(ConfigurableContext<SectionModel, SectionChanges> context) {
		context.getRootBuilder().overlay(10, 400, (overlay) -> {
			overlay.getOverlayParent().getChildren()
					.add(new Label("WARNING: The " + this.getClass().getSimpleName() + " is only to prove concepts.\n"
							+ "It should NEVER be used for application developement.\n\n"
							+ "The purpose of this editor is to prove the raw OfficeFloor model.\n"
							+ "Much of the functionality is not complete for this editor (or likely very buggy).\n"
							+ "For application development please use WoOF."));
		});
	}

	@Override
	public String paletteStyle() {
		return ".palette { -fx-background-color: cornsilk }";
	}

	@Override
	public String paletteIndicatorStyle() {
		return ".palette-indicator { -fx-background-color: bisque }";
	}

	@Override
	public String editorStyle() {
		return ".editor { -fx-background-color: burlywood } .connection Path { -fx-stroke: royalblue }";
	}

	@Override
	public String fileName() {
		return "new.section";
	}

	@Override
	public SectionModel newFileRoot() {
		return new SectionModel();
	}

	@Override
	public SectionModel prototype() {
		return new SectionModel();
	}

	@Override
	protected void loadParents(
			List<AbstractConfigurableItem<SectionModel, SectionEvent, SectionChanges, ?, ?, ?>> parents) {
		parents.add(new FunctionNamespaceItem());
		parents.add(new FunctionItem());
		parents.add(new SubSectionItem());
		parents.add(new ExternalFlowItem());
		parents.add(new ManagedObjectSourceItem());
		parents.add(new ManagedObjectItem());
		parents.add(new ExternalManagedObjectItem());
	}

	@Override
	protected SectionModel loadRootModel(ConfigurationItem configurationItem) throws Exception {
		SectionModel section = new SectionModel();
		SECTION_REPOSITORY.retrieveSection(section, configurationItem);
		return section;
	}

	@Override
	public void saveRootModel(SectionModel model, WritableConfigurationItem configurationItem) throws Exception {
		SECTION_REPOSITORY.storeSection(model, configurationItem);
	}

}
