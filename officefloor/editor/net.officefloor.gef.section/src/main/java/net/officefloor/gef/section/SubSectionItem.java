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
import javafx.scene.layout.VBox;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionLoader;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.gef.bridge.EnvironmentBridge;
import net.officefloor.gef.configurer.ChoiceBuilder;
import net.officefloor.gef.configurer.ConfigurationBuilder;
import net.officefloor.gef.configurer.ValueValidator;
import net.officefloor.gef.editor.AdaptedChildVisualFactoryContext;
import net.officefloor.gef.ide.editor.AbstractConfigurableItem;
import net.officefloor.model.section.SectionChanges;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SectionModel.SectionEvent;
import net.officefloor.model.section.SubSectionModel;
import net.officefloor.model.section.SubSectionModel.SubSectionEvent;
import net.officefloor.plugin.section.clazz.ClassSectionSource;

/**
 * Configuration for the {@link SubSectionModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class SubSectionItem extends
		AbstractConfigurableItem<SectionModel, SectionEvent, SectionChanges, SubSectionModel, SubSectionEvent, SubSectionItem> {

	/**
	 * Loads the {@link SectionType} for the {@link SubSectionItem}.
	 * 
	 * @param item      {@link SubSectionItem}.
	 * @param envBridge {@link EnvironmentBridge}.
	 * @return {@link SectionType}.
	 * @throws Exception If fails to load the {@link SectionType}.
	 */
	public static SectionType loadSectionType(SubSectionItem item, EnvironmentBridge envBridge) throws Exception {
		SectionLoader loader = envBridge.getOfficeFloorCompiler().getSectionLoader();
		Class<? extends SectionSource> sourceClass = envBridge.loadClass(item.sourceClassName, SectionSource.class);
		return loader.loadSectionType(sourceClass, item.location, item.properties);
	}

	/**
	 * Choice {@link Class}.
	 */
	private static final int CHOICE_CLASS = 0;

	/**
	 * Name.
	 */
	private String name;

	/**
	 * Choice.
	 */
	private Integer choice = null;

	/**
	 * {@link SectionSource} {@link Class} name.
	 */
	private String sourceClassName;

	/**
	 * Location.
	 */
	private String location;

	/**
	 * {@link PropertyList}.
	 */
	private PropertyList properties = OfficeFloorCompiler.newPropertyList();

	/**
	 * {@link SectionType}.
	 */
	private SectionType sectionType;

	/*
	 * =================== AbstractConfigurableItem ====================
	 */

	@Override
	public SubSectionModel prototype() {
		return new SubSectionModel("Sub Section", null, null);
	}

	@Override
	public IdeExtractor extract() {
		return new IdeExtractor((root) -> root.getSubSections(), SectionEvent.ADD_SUB_SECTION,
				SectionEvent.REMOVE_SUB_SECTION);
	}

	@Override
	public void loadToParent(SectionModel parentModel, SubSectionModel itemModel) {
		parentModel.addSubSection(itemModel);
	}

	@Override
	public Pane visual(SubSectionModel model, AdaptedChildVisualFactoryContext<SubSectionModel> context) {
		VBox container = new VBox();
		context.label(container);
		HBox children = context.addNode(container, new HBox());
		VBox inputs = context.addNode(children, new VBox());
		context.childGroup(SubSectionInputItem.class.getSimpleName(), inputs);
		VBox outputs = context.addNode(children, new VBox());
		context.childGroup(SubSectionOutputItem.class.getSimpleName(), outputs);
		return container;
	}

	@Override
	public IdeLabeller label() {
		return new IdeLabeller((model) -> model.getSubSectionName(), SubSectionEvent.CHANGE_SUB_SECTION_NAME);
	}

	@Override
	public String style() {
		IdeStyle background = new IdeStyle().rule("-fx-background-color",
				"radial-gradient(radius 50.0%, darkorchid, thistle)");
		IdeStyle text = new IdeStyle(".${model} .title").rule("-fx-text-fill", "white");
		return background.toString() + text.toString();
	}

	@Override
	public SubSectionItem item(SubSectionModel model) {
		SubSectionItem item = new SubSectionItem();
		if (model != null) {
			item.name = model.getSubSectionName();
			item.sourceClassName = model.getSectionSourceClassName();
			item.location = model.getSectionLocation();
			item.properties = this.translateToPropertyList(model.getProperties(), (p) -> p.getName(),
					(p) -> p.getValue());
			if (ClassSectionSource.class.getName().equals(item.sourceClassName)) {
				item.choice = CHOICE_CLASS;
			}
		}
		return item;
	}

	@Override
	protected void children(List<IdeChildrenGroup> childGroups) {
		childGroups.add(new IdeChildrenGroup(new SubSectionInputItem()));
		childGroups.add(new IdeChildrenGroup(new SubSectionOutputItem()));
	}

	@Override
	public IdeConfigurer configure() {
		return new IdeConfigurer().addAndRefactor((builder, context) -> {
			builder.title("Sub Section");
			builder.text("Name").init((item) -> item.name).validate(ValueValidator.notEmptyString("Must provide name"))
					.setValue((item, value) -> item.name = value);
			ChoiceBuilder<SubSectionItem> choices = builder.choices("").init((item) -> item.choice)
					.validate(ValueValidator.notNull("Must select")).setValue((item, value) -> {
						if (value == CHOICE_CLASS) {
							item.sourceClassName = ClassSectionSource.class.getName();
						}
					});

			// Choice: class
			ConfigurationBuilder<SubSectionItem> classBuilder = choices.choice("Class");
			classBuilder.clazz("Class").init((item) -> item.location)
					.validate(ValueValidator.notEmptyString("Must specify class"))
					.setValue((item, value) -> item.location = value);

			// Choice: source
			ConfigurationBuilder<SubSectionItem> sourceBuilder = choices.choice("Source");
			sourceBuilder.clazz("Source").init((item) -> item.sourceClassName).superType(SectionSource.class)
					.validate(ValueValidator.notEmptyString("Must specify source"))
					.setValue((item, value) -> item.sourceClassName = value);
			sourceBuilder.text("Location").init((item) -> item.location)
					.setValue((item, value) -> item.location = value);
			sourceBuilder.properties("Properties").init((item) -> item.properties)
					.setValue((item, value) -> item.properties = value);

			// Validate (ensure loads type)
			builder.validate((ctx) -> {
				EnvironmentBridge envBridge = this.getConfigurableContext().getEnvironmentBridge();

				// Validate the type
				SubSectionItem item = ctx.getModel();

				// Attempt to load the type
				item.sectionType = loadSectionType(item, envBridge);
			});

		}).add((builder, context) -> {
			builder.apply("Add", (item) -> {
				context.execute(context.getOperations().addSubSection(item.name, item.sourceClassName, item.location,
						item.properties, item.sectionType));
			});

		}).refactor((builder, context) -> {
			builder.apply("Refactor", (item) -> {
				// TODO implement refactoring sub section
				throw new UnsupportedOperationException("TODO implement refactoring SubSection");
			});

		}).delete((context) -> {
			context.execute(context.getOperations().removeSubSection(context.getModel()));
		});
	}

}
