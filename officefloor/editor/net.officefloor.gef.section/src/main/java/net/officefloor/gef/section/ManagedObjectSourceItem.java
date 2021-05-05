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

import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.managedobject.ManagedObjectLoader;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.gef.bridge.EnvironmentBridge;
import net.officefloor.gef.configurer.ChoiceBuilder;
import net.officefloor.gef.configurer.ConfigurationBuilder;
import net.officefloor.gef.configurer.ValueValidator;
import net.officefloor.gef.editor.AdaptedChildVisualFactoryContext;
import net.officefloor.gef.editor.DefaultConnectors;
import net.officefloor.gef.editor.DefaultImages;
import net.officefloor.gef.ide.editor.AbstractConfigurableItem;
import net.officefloor.model.section.SectionChanges;
import net.officefloor.model.section.SectionManagedObjectSourceModel;
import net.officefloor.model.section.SectionManagedObjectSourceModel.SectionManagedObjectSourceEvent;
import net.officefloor.model.section.SectionManagedObjectToSectionManagedObjectSourceModel;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SectionModel.SectionEvent;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;

/**
 * Configuration for the {@link SectionManagedObjectSourceModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectSourceItem extends
		AbstractConfigurableItem<SectionModel, SectionEvent, SectionChanges, SectionManagedObjectSourceModel, SectionManagedObjectSourceEvent, ManagedObjectSourceItem> {

	/**
	 * Loads the {@link ManagedObjectType} for the {@link ManagedObjectItem}.
	 * 
	 * @param item      {@link ManagedObjectItem}.
	 * @param envBridge {@link EnvironmentBridge}.
	 * @return {@link ManagedObjectType}.
	 * @throws Exception If fails to load the {@link ManagedObjectType}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static ManagedObjectType<?> loadManagedObjectType(ManagedObjectSourceItem item, EnvironmentBridge envBridge)
			throws Exception {
		ManagedObjectLoader loader = envBridge.getOfficeFloorCompiler().getManagedObjectLoader();
		Class<? extends ManagedObjectSource> sourceClass = envBridge.loadClass(item.sourceClassName,
				ManagedObjectSource.class);
		return loader.loadManagedObjectType(sourceClass, item.properties);
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
	 * {@link ManagedObjectSource} {@link Class} name.
	 */
	private String sourceClassName;

	/**
	 * {@link PropertyList}.
	 */
	private PropertyList properties = OfficeFloorCompiler.newPropertyList();

	/**
	 * Timeout.
	 */
	private long timeout = 0;

	/**
	 * {@link ManagedObjectType}.
	 */
	private ManagedObjectType<?> managedObjectType;

	/*
	 * ================= AbstractConfigurableItem ====================
	 */

	@Override
	public SectionManagedObjectSourceModel prototype() {
		return new SectionManagedObjectSourceModel("Managed Object Source", null, null, "0");
	}

	@Override
	public IdeExtractor extract() {
		return new IdeExtractor((r) -> r.getSectionManagedObjectSources(),
				SectionEvent.ADD_SECTION_MANAGED_OBJECT_SOURCE, SectionEvent.REMOVE_SECTION_MANAGED_OBJECT_SOURCE);
	}

	@Override
	public void loadToParent(SectionModel parentModel, SectionManagedObjectSourceModel itemModel) {
		parentModel.addSectionManagedObjectSource(itemModel);
	}

	@Override
	public Pane visual(SectionManagedObjectSourceModel model,
			AdaptedChildVisualFactoryContext<SectionManagedObjectSourceModel> context) {
		HBox container = new HBox();
		context.label(container).getStyleClass().add("title");
		context.addNode(container, context.action((ctx) -> {

			// Obtain the item to load type
			SectionManagedObjectSourceModel mos = ctx.getModel();
			ManagedObjectSourceItem mosItem = new ManagedObjectSourceItem().item(mos);
			ManagedObjectType<?> moType = loadManagedObjectType(mosItem,
					this.getConfigurableContext().getEnvironmentBridge());

			// Add the managed object
			ctx.getChangeExecutor().execute(this.getConfigurableContext().getOperations()
					.addSectionManagedObject(mosItem.name, ManagedObjectScope.THREAD, mos, moType));
		}, DefaultImages.ADD));
		context.addNode(container,
				context.connector(DefaultConnectors.DERIVE, SectionManagedObjectToSectionManagedObjectSourceModel.class)
						.getNode());
		return container;
	}

	@Override
	public IdeLabeller label() {
		return new IdeLabeller((m) -> m.getSectionManagedObjectSourceName(),
				SectionManagedObjectSourceEvent.CHANGE_SECTION_MANAGED_OBJECT_SOURCE_NAME);
	}

	@Override
	public String style() {
		return new IdeStyle().rule("-fx-background-color", "radial-gradient(radius 50.0%, green, darkseagreen)")
				.toString();
	}

	@Override
	public ManagedObjectSourceItem item(SectionManagedObjectSourceModel model) {
		ManagedObjectSourceItem item = new ManagedObjectSourceItem();
		if (model != null) {
			item.name = model.getSectionManagedObjectSourceName();
			item.sourceClassName = model.getManagedObjectSourceClassName();
			if (ClassManagedObjectSource.class.getName().equals(item.sourceClassName)) {
				item.choice = CHOICE_CLASS;
			}
			item.properties = this.translateToPropertyList(model.getProperties(), (p) -> p.getName(),
					(p) -> p.getValue());
			item.timeout = Long.parseLong(model.getTimeout());
		}
		return item;
	}

	@Override
	public IdeConfigurer configure() {
		return new IdeConfigurer().addAndRefactor((builder, context) -> {
			builder.title("Managed Object Source");
			builder.text("Name").init((item) -> item.name).validate(ValueValidator.notEmptyString("Must specify name"))
					.setValue((item, value) -> item.name = value);
			ChoiceBuilder<ManagedObjectSourceItem> choices = builder.choices("").init((item) -> item.choice)
					.validate(ValueValidator.notNull("Must select")).setValue((item, value) -> {
						if (value == CHOICE_CLASS) {
							item.sourceClassName = ClassManagedObjectSource.class.getName();
						}
					});

			// Choice: class
			ConfigurationBuilder<ManagedObjectSourceItem> classBuilder = choices.choice("Class");
			classBuilder
					.clazz("Class").init((item) -> item.properties
							.getOrAddProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME).getValue())
					.validate((ctx) -> {
						ManagedObjectSourceItem item = ctx.getModel();
						String className = item.properties
								.getOrAddProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME).getValue();
						ValueValidator.notEmptyString(className, "Must specify class", ctx);
					}).setValue((item, value) -> item.properties
							.getOrAddProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME).setValue(value));

			// Choice: source
			ConfigurationBuilder<ManagedObjectSourceItem> sourceBuilder = choices.choice("Source");
			sourceBuilder.clazz("Source").init((item) -> item.sourceClassName).superType(ManagedObjectSource.class)
					.validate(ValueValidator.notEmptyString("Must specify source"))
					.setValue((item, value) -> item.sourceClassName = value);
			sourceBuilder.properties("Properties").init((item) -> item.properties)
					.setValue((item, value) -> item.properties = value);

			// Validate (ensure loads type)
			builder.validate((ctx) -> {
				EnvironmentBridge envBridge = this.getConfigurableContext().getEnvironmentBridge();

				// Validate the type
				ManagedObjectSourceItem item = ctx.getModel();

				// Attempt to load the function name space type
				item.managedObjectType = loadManagedObjectType(item, envBridge);
			});

		}).add((builder, context) -> {
			builder.apply("Add", (item) -> {
				context.execute(context.getOperations().addSectionManagedObjectSource(item.name, item.sourceClassName,
						item.properties, item.timeout, item.managedObjectType));
			});

		}).refactor((builder, context) -> {
			builder.apply("Refactor", (item) -> {
				// TODO implement refactorManagedObjectSource
				throw new UnsupportedOperationException("TODO implement SectionChanges.refactorManagedObjectSource");
			});

		}).delete((context) -> {
			context.execute(context.getOperations().removeSectionManagedObjectSource(context.getModel()));
		});
	}

}
