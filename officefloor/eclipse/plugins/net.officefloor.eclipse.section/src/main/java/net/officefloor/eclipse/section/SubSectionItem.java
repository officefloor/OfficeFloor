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

import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionLoader;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.eclipse.configurer.ChoiceBuilder;
import net.officefloor.eclipse.configurer.ConfigurationBuilder;
import net.officefloor.eclipse.configurer.ValueValidator;
import net.officefloor.eclipse.editor.AdaptedModelVisualFactoryContext;
import net.officefloor.eclipse.ide.editor.AbstractConfigurableItem;
import net.officefloor.eclipse.osgi.OfficeFloorOsgiBridge;
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
	 * Test configuration.
	 */
	public static void main(String[] args) {
		SectionEditor.launchConfigurer(new SubSectionItem(), null);
	}

	/**
	 * Loads the {@link SectionType} for the {@link SubSectionItem}.
	 * 
	 * @param item
	 *            {@link SubSectionItem}.
	 * @param osgiBridge
	 *            {@link OfficeFloorOsgiBridge}.
	 * @return {@link SectionType}.
	 * @throws Exception
	 *             If fails to load the {@link SectionType}.
	 */
	public static SectionType loadSectionType(SubSectionItem item, OfficeFloorOsgiBridge osgiBridge) throws Exception {
		SectionLoader loader = osgiBridge.getOfficeFloorCompiler().getSectionLoader();
		Class<? extends SectionSource> sourceClass = osgiBridge.loadClass(item.sourceClassName, SectionSource.class);
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
	protected SubSectionModel prototype() {
		return new SubSectionModel("Sub Section", null, null);
	}

	@Override
	protected IdeExtractor extract() {
		return new IdeExtractor((root) -> root.getSubSections(), SectionEvent.ADD_SUB_SECTION,
				SectionEvent.REMOVE_SUB_SECTION);
	}

	@Override
	protected Pane visual(SubSectionModel model, AdaptedModelVisualFactoryContext<SubSectionModel> context) {
		VBox container = new VBox();
		context.label(container);
		return container;
	}

	@Override
	protected IdeLabeller label() {
		return new IdeLabeller((model) -> model.getSubSectionName(), SubSectionEvent.CHANGE_SUB_SECTION_NAME);
	}

	@Override
	protected SubSectionItem item(SubSectionModel model) {
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
				OfficeFloorOsgiBridge osgiBridge = this.getConfigurableContext().getOsgiBridge();

				// Validate the type
				SubSectionItem item = ctx.getModel();

				// Attempt to load the type
				item.sectionType = loadSectionType(item, osgiBridge);
			});

		}).add((builder, context) -> {
			builder.apply("Add", (item) -> {
				context.execute(context.getOperations().addSubSection(item.name, item.sourceClassName, item.location,
						item.properties, item.sectionType));
			});
		});
	}

}
