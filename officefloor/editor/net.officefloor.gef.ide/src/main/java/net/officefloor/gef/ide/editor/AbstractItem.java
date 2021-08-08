/*-
 * #%L
 * [bundle] OfficeFloor Eclipse IDE
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

package net.officefloor.gef.ide.editor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javafx.scene.Node;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.gef.bridge.EnvironmentBridge;
import net.officefloor.gef.editor.AdaptedChildBuilder;
import net.officefloor.gef.editor.AdaptedChildVisualFactoryContext;
import net.officefloor.gef.editor.AdaptedConnectionBuilder;
import net.officefloor.gef.editor.AdaptedConnectionManagementBuilder;
import net.officefloor.gef.editor.AdaptedConnectionManagementBuilder.ConnectionFactory;
import net.officefloor.gef.editor.AdaptedConnectionManagementBuilder.ConnectionRemover;
import net.officefloor.gef.editor.AdaptedRootBuilder;
import net.officefloor.gef.editor.ChangeExecutor;
import net.officefloor.gef.editor.ChildrenGroup;
import net.officefloor.gef.editor.ChildrenGroupBuilder;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;

/**
 * Abstract child {@link ConfigurationItem}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractItem<R extends Model, O, P extends Model, PE extends Enum<PE>, M extends Model, E extends Enum<E>> {

	/**
	 * {@link IdeChildrenGroup} instances for this {@link AbstractItem}.
	 */
	private List<IdeChildrenGroup> childrenGroups;

	/**
	 * {@link AdaptedChildBuilder}.
	 */
	protected AdaptedChildBuilder<R, O, M, E> builder;

	/**
	 * {@link ConfigurableContext}.
	 */
	private ConfigurableContext<R, O> context;

	/**
	 * Context for the configurable parent.
	 */
	public static interface ConfigurableContext<R extends Model, O> {

		/**
		 * Obtains the {@link AdaptedRootBuilder}.
		 * 
		 * @return {@link AdaptedRootBuilder}.
		 */
		AdaptedRootBuilder<R, O> getRootBuilder();

		/**
		 * Obtains the {@link EnvironmentBridge}.
		 * 
		 * @return {@link EnvironmentBridge}.
		 * @throws Exception If fails to obtain the {@link EnvironmentBridge}.
		 */
		EnvironmentBridge getEnvironmentBridge() throws Exception;

		/**
		 * Obtains a preference value.
		 * 
		 * @param preferenceId Identifier for the preference value.
		 * @return Preference value. May be <code>null</code> if no preference
		 *         configured for identifier.
		 */
		String getPreference(String preferenceId);

		/**
		 * Adds a {@link PreferenceListener}.
		 * 
		 * @param preferenceId       Identifier of the preference value to listen for
		 *                           changes.
		 * @param preferenceListener {@link PreferenceListener}.
		 */
		void addPreferenceListener(String preferenceId, PreferenceListener preferenceListener);

		/**
		 * Obtains the operations.
		 * 
		 * @return Operations.
		 */
		O getOperations();

		/**
		 * Obtains the {@link ChangeExecutor}.
		 * 
		 * @return {@link ChangeExecutor}.
		 */
		ChangeExecutor getChangeExecutor();
	}

	/**
	 * Listener to change of a preference.
	 */
	public static interface PreferenceListener {

		/**
		 * Notified of preference value change.
		 * 
		 * @param newPreferenceValue New value for the preference. May be
		 *                           <code>null</code>.
		 */
		void preferenceValueChanged(String newPreferenceValue);
	}

	/**
	 * Initialise with {@link ConfigurableContext}.
	 * 
	 * @param context {@link ConfigurableContext}.
	 */
	public final void init(ConfigurableContext<R, O> context) {
		this.context = context;
	}

	/**
	 * Obtains the {@link ConfigurableContext}.
	 * 
	 * @return {@link ConfigurableContext}.
	 */
	public final ConfigurableContext<R, O> getConfigurableContext() {
		return this.context;
	}

	/**
	 * Convenience method to translate list of property items to a
	 * {@link PropertyList}.
	 *
	 * @param <PI>       Property item type.
	 * @param properties Property items.
	 * @param getName    {@link Function} to extract the name from the property
	 *                   item.
	 * @param getValue   {@link Function} to extract the value from the property
	 *                   item.
	 * @return {@link PropertyList}.
	 */
	protected final <PI> PropertyList translateToPropertyList(List<PI> properties, Function<PI, String> getName,
			Function<PI, String> getValue) {
		PropertyList propertyList = OfficeFloorCompiler.newPropertyList();
		if (properties != null) {
			for (PI property : properties) {
				String name = getName.apply(property);
				String value = getValue.apply(property);
				propertyList.addProperty(name).setValue(value);
			}
		}
		return propertyList;
	}

	/**
	 * Convenience method to translate list of items to a comma separated list.
	 * 
	 * @param <CSVI>   Comma separated item type.
	 * @param items    Items.
	 * @param getValue Obtains the value for the comma separate list from the item.
	 * @return Comma separate text for the items.
	 */
	protected final <CSVI> String translateToCommaSeparateList(List<CSVI> items, Function<CSVI, String> getValue) {
		StringBuilder text = new StringBuilder();
		boolean isFirst = true;
		for (CSVI item : items) {
			if (!isFirst) {
				text.append(", ");
			}
			isFirst = false;
			text.append(getValue.apply(item));
		}
		return text.toString();
	}

	/**
	 * Convenience method to translate comma separated text into a list.
	 *
	 * @param <CSVI>  Comma separated item type.
	 * @param text    Comma separated text.
	 * @param getItem Creates the item from the comma separated value.
	 * @return List of items.
	 */
	protected final <CSVI> List<CSVI> translateFromCommaSeparatedList(String text, Function<String, CSVI> getItem) {
		String[] parts = (text == null ? "" : text).split(",");
		List<CSVI> items = new LinkedList<>();
		for (String part : parts) {
			part = part.trim();
			if (part.length() > 0) {
				items.add(getItem.apply(part));
			}
		}
		return items;
	}

	/**
	 * Translate the list of items to name mapping.
	 * 
	 * @param <I>     Item types.
	 * @param items   Items.
	 * @param getName {@link Function} to extract the name from the item.
	 * @return Name mapping.
	 */
	protected final <I> Map<String, String> translateToNameMappings(I[] items, Function<I, String> getName) {
		Map<String, String> mapping = new HashMap<>();
		for (I item : items) {
			String name = getName.apply(item);
			mapping.put(name, name);
		}
		return mapping;
	}

	/**
	 * Creates the prototype for the item.
	 * 
	 * @return Prototype.
	 */
	public abstract M prototype();

	/**
	 * Extracts the {@link Model} instances.
	 */
	public class IdeExtractor {

		/**
		 * {@link Function} to extract the {@link Model} instances from the parent
		 * {@link Model}.
		 */
		private final Function<P, List<M>> extractor;

		/**
		 * Extract change events.
		 */
		private final PE[] extractChangeEvents;

		/**
		 * Instantiate.
		 * 
		 * @param extractor           {@link Function} to extract the {@link Model}
		 *                            instances from the parent {@link Model}.
		 * @param extractChangeEvents Extract change events.
		 */
		@SafeVarargs
		public IdeExtractor(Function<P, List<M>> extractor, PE... extractChangeEvents) {
			this.extractor = extractor;
			this.extractChangeEvents = extractChangeEvents;
		}

		/**
		 * Extracts the {@link Model} instances from the parent {@link Model}.
		 * 
		 * @param parentModel Parent {@link Model}.
		 * @return Extract {@link Model} instances.
		 */
		List<M> extract(P parentModel) {
			return this.extractor.apply(parentModel);
		}

		/**
		 * Obtains the extract change events.
		 * 
		 * @return Extract change events.
		 */
		PE[] getExtractChangeEvents() {
			return this.extractChangeEvents;
		}
	}

	/**
	 * Obtains the {@link IdeExtractor} to extract {@link Model} instances from the
	 * parent {@link Model}.
	 * 
	 * @return {@link IdeExtractor}.
	 */
	public abstract IdeExtractor extract();

	/**
	 * Loads the {@link Model} to the parent {@link Model}. This allows for
	 * constructing a prototype model for editing preferences of the
	 * {@link AbstractAdaptedIdeEditor}.
	 * 
	 * @param parentModel Parent {@link Model}.
	 * @param itemModel   Item {@link Model}.
	 */
	public abstract void loadToParent(P parentModel, M itemModel);

	/**
	 * Creates the visual for the {@link Model}.
	 * 
	 * @param model   {@link Model}.
	 * @param context {@link AdaptedChildVisualFactoryContext}.
	 * @return {@link Node} for the visual.
	 */
	public abstract Node visual(M model, AdaptedChildVisualFactoryContext<M> context);

	/**
	 * Labels the configuration item.
	 */
	public class IdeLabeller {

		/**
		 * {@link Function} to extract the label from the {@link Model}.
		 */
		private final Function<M, String> labeller;

		/**
		 * Label change events.
		 */
		private final E[] labelChangeEvents;

		/**
		 * Instantiate.
		 * 
		 * @param labeller          {@link Function} to extract the label from the
		 *                          {@link Model}.
		 * @param labelChangeEvents Label change events.
		 */
		@SafeVarargs
		public IdeLabeller(Function<M, String> labeller, E... labelChangeEvents) {
			this.labeller = labeller;
			this.labelChangeEvents = labelChangeEvents;
		}

		/**
		 * Obtains the label from the {@link Model}.
		 * 
		 * @param model {@link Model}.
		 * @return Label for the {@link Model}.
		 */
		public String getLabel(M model) {
			return this.labeller.apply(model);
		}

		/**
		 * Obtains the change events for the label.
		 * 
		 * @return Change events for the label.
		 */
		E[] getLabelChangeEvents() {
			return this.labelChangeEvents;
		}
	}

	/**
	 * Obtains the {@link IdeLabeller} for the {@link Model}.
	 * 
	 * @return {@link IdeLabeller}.
	 */
	public abstract IdeLabeller label();

	/**
	 * Convenience {@link Class} to build style.
	 */
	public class IdeStyle {

		/**
		 * Style contents.
		 */
		private final StringBuilder style = new StringBuilder();

		/**
		 * Instantiate with selector.
		 * 
		 * @param selector Selector for applying the style.
		 */
		public IdeStyle(String selector) {
			this.style.append(selector + " {" + System.lineSeparator());
		}

		/**
		 * Instantiate for the {@link Model}.
		 */
		public IdeStyle() {
			this(".${model}");
		}

		/**
		 * Adds a rule for this style.
		 * 
		 * @param key   Key of the rule.
		 * @param value Value of the rule.
		 * @return <code>this</code>.
		 */
		public IdeStyle rule(String key, String value) {
			this.style.append("    " + key + ": " + value + ";" + System.lineSeparator());
			return this;
		}

		/*
		 * ============ Object ==================
		 */

		public String toString() {
			return this.style.toString() + "}" + System.lineSeparator();
		}
	}

	/**
	 * Obtains the style for the display of the {@link Model}.
	 * 
	 * @return Style for the display of the {@link Model}.
	 */
	public String style() {

		// Load the styles
		List<IdeStyle> styles = new LinkedList<>();
		this.loadStyles(styles);

		// Determine if have styling
		if (styles.size() == 0) {
			return null;
		}

		// Load the styling
		StringBuilder styling = new StringBuilder();
		for (IdeStyle style : styles) {
			styling.append(style.toString());
		}

		// Return the styling
		return styling.toString();
	}

	/**
	 * Default implementation of {@link #style()} will invoke this to load styles.
	 * 
	 * @param styles {@link List} to be loaded with the {@link IdeStyle} instances.
	 */
	protected void loadStyles(List<IdeStyle> styles) {
	}

	/**
	 * Further adapt the {@link AdaptedChildBuilder}.
	 * 
	 * @param builder {@link AdaptedChildBuilder}.
	 */
	protected void furtherAdapt(AdaptedChildBuilder<R, O, M, E> builder) {
		// Default implementation of nothing further
	}

	/**
	 * IDE {@link ChildrenGroup}.
	 */
	public class IdeChildrenGroup implements Function<M, List<? extends Model>> {

		/**
		 * Name of the {@link ChildrenGroup}.
		 */
		private final String name;

		/**
		 * {@link AbstractItem} instances for the {@link ChildrenGroup}.
		 */
		private final AbstractItem<R, O, M, E, ?, ?>[] children;

		/**
		 * Instantiate for a single {@link AbstractItem}.
		 * 
		 * @param child {@link AbstractItem}.
		 */
		@SuppressWarnings("unchecked")
		public IdeChildrenGroup(AbstractItem<R, O, M, E, ?, ?> child) {
			this.name = child.getClass().getSimpleName();
			this.children = new AbstractItem[] { child };
		}

		/**
		 * Instantiate for multiple {@link AbstractItem} instances.
		 * 
		 * @param groupName Name of the group.
		 * @param children  {@link AbstractItem} instances.
		 */
		@SafeVarargs
		public IdeChildrenGroup(String groupName, AbstractItem<R, O, M, E, ?, ?>... children) {
			this.name = groupName;
			this.children = children;
		}

		/**
		 * Obtains the name of the {@link ChildrenGroup}.
		 * 
		 * @return Name of the {@link ChildrenGroup}.
		 */
		public String getChildrenGroupName() {
			return this.name;
		}

		/*
		 * ======== Function (for models) ==================
		 */

		@Override
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public List<? extends Model> apply(M parent) {
			if (this.children.length == 1) {
				// Just the one child, so return from child
				return this.children[0].extract().extract(parent);
			} else {
				// Include all children together
				List<? extends Model> children = new LinkedList<>();
				for (AbstractItem child : this.children) {
					children.addAll(child.extract().extract(parent));
				}
				return children;
			}
		}

		/**
		 * Obtain the change events.
		 * 
		 * @return Change events.
		 */
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public Enum<?>[] changeEvents() {
			if (this.children.length == 1) {
				// Just the one child, so return from child
				return this.children[0].extract().getExtractChangeEvents();
			} else {
				// Include all children together
				List children = new LinkedList<>();
				for (AbstractItem child : this.children) {
					children.addAll(Arrays.asList(child.extract().getExtractChangeEvents()));
				}
				return (Enum<?>[]) children.toArray(new Enum[children.size()]);
			}
		}

		/**
		 * Obtains the {@link AbstractItem} instances for the {@link ChildrenGroup}.
		 * 
		 * @return {@link AbstractItem} instances for the {@link ChildrenGroup}.
		 */
		public AbstractItem<R, O, M, E, ?, ?>[] getChildren() {
			return this.children;
		}
	}

	/**
	 * Obtains the {@link IdeChildrenGroup} instances.
	 * 
	 * @return {@link IdeChildrenGroup} instances.
	 */
	@SuppressWarnings("unchecked")
	public final IdeChildrenGroup[] getChildrenGroups() {

		// Lazy load the children groups
		if (this.childrenGroups == null) {

			// Load the children groups
			this.childrenGroups = new LinkedList<>();
			this.children(this.childrenGroups);
		}

		// Obtain the children groups
		return this.childrenGroups.toArray(new AbstractItem.IdeChildrenGroup[this.childrenGroups.size()]);
	}

	/**
	 * Loads the {@link IdeChildrenGroup} instances.
	 * 
	 * @param childGroups {@link IdeChildrenGroup} instances.
	 */
	protected void children(List<IdeChildrenGroup> childGroups) {
		// No children by default
	}

	/**
	 * IDE {@link AdaptedConnectionBuilder}.
	 */
	public class IdeConnection<C extends ConnectionModel> {

		/**
		 * {@link Class} of the {@link ConnectionModel}.
		 */
		private final Class<C> connectionClass;

		/**
		 * Source to many {@link ConnectionModel} instances.
		 */
		private Function<M, List<C>> sourceToMany = null;

		/**
		 * Source to one {@link ConnectionModel} instances.
		 */
		private Function<M, C> sourceToOne = null;

		/**
		 * {@link ConnectionModel} to source instance.
		 */
		private Function<C, M> connToSource;

		/**
		 * Source change events.
		 */
		private E[] sourceChangeEvents;

		/**
		 * Target.
		 */
		private IdeConnectionTarget<C, ? extends Model, ? extends Enum<?>> target;

		/**
		 * Instantiate.
		 * 
		 * @param connectionClass {@link Class} of the {@link ConnectionModel}.
		 */
		public IdeConnection(Class<C> connectionClass) {
			this.connectionClass = connectionClass;
		}

		/**
		 * Connect to many targets.
		 * 
		 * @param sourceToMany       Obtains the multiple {@link ConnectionModel}
		 *                           instances from the source.
		 * @param connToSource       Obtains the source from a particular
		 *                           {@link ConnectionModel}.
		 * @param sourceChangeEvents Source change events.
		 * @return <code>this</code>.
		 */
		@SafeVarargs
		public final IdeConnection<C> connectMany(Function<M, List<C>> sourceToMany, Function<C, M> connToSource,
				E... sourceChangeEvents) {
			this.sourceToMany = sourceToMany;
			this.connToSource = connToSource;
			this.sourceChangeEvents = sourceChangeEvents;
			return this;
		}

		/**
		 * Connect to one target.
		 * 
		 * @param sourceToOne        Obtains the single {@link ConnectionModel} from the
		 *                           source.
		 * @param connToSource       Obtains the source from the
		 *                           {@link ConnectionModel}.
		 * @param sourceChangeEvents Source change events.
		 * @return <code>this</code>.
		 */
		@SafeVarargs
		public final IdeConnection<C> connectOne(Function<M, C> sourceToOne, Function<C, M> connToSource,
				E... sourceChangeEvents) {
			this.sourceToOne = sourceToOne;
			this.connToSource = connToSource;
			this.sourceChangeEvents = sourceChangeEvents;
			return this;
		}

		/**
		 * Connects the target.
		 *
		 * @param <T>         Target {@link Model} type.
		 * @param <TE>        Target {@link Model} event type.
		 * @param targetClass Target {@link Class}.
		 * @return {@link IdeConnectionTarget}.
		 */
		@SuppressWarnings("unchecked")
		public <T extends Model, TE extends Enum<TE>> IdeConnectionTarget<C, T, TE> to(Class<T> targetClass) {
			this.target = new IdeConnectionTarget<>(this, targetClass);
			return (IdeConnectionTarget<C, T, TE>) this.target;
		}

	}

	/**
	 * IDE target {@link AdaptedConnectionBuilder}.
	 */
	public class IdeConnectionTarget<C extends ConnectionModel, T extends Model, TE extends Enum<TE>> {

		/**
		 * {@link IdeConnection}.
		 */
		private final IdeConnection<C> ideConnection;

		/**
		 * Target {@link Class}.
		 */
		private Class<T> targetClass;

		/**
		 * Target to many {@link ConnectionModel} instances.
		 */
		private Function<T, List<C>> targetToMany = null;

		/**
		 * Target to one {@link ConnectionModel} instances.
		 */
		private Function<T, C> targetToOne = null;

		/**
		 * {@link ConnectionModel} to source instance.
		 */
		private Function<C, T> connToTarget;

		/**
		 * Target change events.
		 */
		private TE[] targetChangeEvents;

		/**
		 * {@link ConnectionFactory} to create the {@link ConnectionModel}.
		 */
		private ConnectionFactory<R, O, M, C, T> createConnection = null;

		/**
		 * {@link ConnectionRemover} to remove the {@link ConnectionModel}.
		 */
		private ConnectionRemover<R, O, C> deleteConnection = null;

		/**
		 * Instantiate from only the {@link IdeConnection}.
		 * 
		 * @param ideConnection {@link IdeConnection}.
		 * @param targetClass   Target {@link Class}.
		 */
		private IdeConnectionTarget(IdeConnection<C> ideConnection, Class<T> targetClass) {
			this.targetClass = targetClass;
			this.ideConnection = ideConnection;
		}

		/**
		 * Connect to many sources.
		 * 
		 * @param targetToMany       Obtains the multiple {@link ConnectionModel}
		 *                           instances from the target.
		 * @param connToTarget       Obtains the target from a particular
		 *                           {@link ConnectionModel}.
		 * @param targetChangeEvents Target change events.
		 * @return <code>this</code>.
		 */
		@SafeVarargs
		@SuppressWarnings("unchecked")
		public final IdeConnectionTarget<C, T, TE> many(Function<T, List<C>> targetToMany, Function<C, T> connToTarget,
				Enum<?>... targetChangeEvents) {
			this.targetToMany = targetToMany;
			this.connToTarget = connToTarget;
			this.targetChangeEvents = (TE[]) targetChangeEvents;
			return this;
		}

		/**
		 * Connect to one target.
		 * 
		 * @param targetToOne        Obtains the single {@link ConnectionModel} from the
		 *                           target.
		 * @param connToTarget       Obtains the target from the
		 *                           {@link ConnectionModel}.
		 * @param targetChangeEvents Target change events.
		 * @return <code>this</code>.
		 */
		@SafeVarargs
		@SuppressWarnings("unchecked")
		public final IdeConnectionTarget<C, T, TE> one(Function<T, C> targetToOne, Function<C, T> connToTarget,
				Enum<?>... targetChangeEvents) {
			this.targetToOne = targetToOne;
			this.connToTarget = connToTarget;
			this.targetChangeEvents = (TE[]) targetChangeEvents;
			return this;
		}

		/**
		 * Configures creating the {@link ConnectionModel}.
		 * 
		 * @param createConnetion {@link ConnectionFactory} to create the
		 *                        {@link ConnectionModel}.
		 * @return <code>this</code>.
		 */
		public final IdeConnectionTarget<C, T, TE> create(ConnectionFactory<R, O, M, C, T> createConnetion) {
			this.createConnection = createConnetion;
			return this;
		}

		/**
		 * Configures deleting the {@link ConnectionModel}.
		 * 
		 * @param deleteConnection {@link ConnectionRemover} to delete the
		 *                         {@link ConnectionModel}.
		 * @return <code>this</code>.
		 */
		public final IdeConnectionTarget<C, T, TE> delete(ConnectionRemover<R, O, C> deleteConnection) {
			this.deleteConnection = deleteConnection;
			return this;
		}

		/**
		 * Loads the connection to the {@link AdaptedChildBuilder}.
		 * 
		 * @param builder {@link AdaptedChildBuilder} to be configured with the
		 *                {@link ConnectionModel}.
		 */
		void loadConnection(AdaptedChildBuilder<R, O, M, E> builder) {

			// Initiate the connection
			AdaptedConnectionBuilder<R, O, M, C, E> connection;
			if (this.ideConnection.sourceToMany != null) {
				connection = builder.connectMany(this.ideConnection.connectionClass, this.ideConnection.sourceToMany,
						this.ideConnection.connToSource, this.ideConnection.sourceChangeEvents);
			} else if (this.ideConnection.sourceToOne != null) {
				connection = builder.connectOne(this.ideConnection.connectionClass, this.ideConnection.sourceToOne,
						this.ideConnection.connToSource, this.ideConnection.sourceChangeEvents);
			} else {
				throw new IllegalStateException("Must specify connection details for connection "
						+ this.ideConnection.connectionClass.getName());
			}

			// Complete the connection
			AdaptedConnectionManagementBuilder<R, O, M, C, T> management;
			if (this.targetToMany != null) {
				management = connection.toMany(this.targetClass, this.targetToMany, this.connToTarget,
						this.targetChangeEvents);
			} else if (this.targetToOne != null) {
				management = connection.toOne(this.targetClass, this.targetToOne, this.connToTarget,
						this.targetChangeEvents);
			} else {
				throw new IllegalStateException("Must specify connection target details for connection "
						+ this.ideConnection.connectionClass.getName());
			}

			// Load the create / delete for connection
			if (this.createConnection != null) {
				management.create(this.createConnection);
			}
			if (this.deleteConnection != null) {
				management.delete(this.deleteConnection);
			}
		}
	}

	/**
	 * Obtains the {@link IdeConnection} instances.
	 * 
	 * @return {@link IdeConnection} instances.
	 */
	@SuppressWarnings("unchecked")
	public final IdeConnectionTarget<? extends ConnectionModel, ?, ?>[] getConnections() {
		List<IdeConnectionTarget<? extends ConnectionModel, ?, ?>> connections = new LinkedList<>();
		this.connections(connections);
		return connections.toArray(new AbstractItem.IdeConnectionTarget[connections.size()]);
	}

	/**
	 * Loads the {@link IdeConnectionTarget} instances (created from
	 * {@link IdeConnection} instances).
	 * 
	 * @param connections {@link IdeConnection} instances.
	 */
	protected void connections(List<IdeConnectionTarget<? extends ConnectionModel, ?, ?>> connections) {
		// No connections by default
	}

	/**
	 * Creates the {@link AdaptedChildBuilder}.
	 * 
	 * @param childrenGroup {@link ChildrenGroupBuilder}.
	 * @return Child {@link AdaptedChildBuilder}.
	 */
	public final AdaptedChildBuilder<R, O, M, E> createChild(ChildrenGroupBuilder<R, O> childrenGroup) {

		// Add the child
		this.builder = childrenGroup.addChild(this.prototype(), (model, ctx) -> this.visual(model, ctx));

		// Determine if configured with label
		IdeLabeller labeller = this.label();
		if (labeller != null) {
			this.builder.label((model) -> labeller.getLabel(model), labeller.getLabelChangeEvents());
		}

		// Further adapt the child
		this.furtherAdapt(this.builder);

		// Return the child
		return this.builder;
	}

	/**
	 * Obtains the {@link AdaptedChildBuilder} for the {@link AbstractItem}.
	 * 
	 * @return {@link AdaptedChildBuilder} for the {@link AbstractItem}.
	 */
	public final AdaptedChildBuilder<R, O, M, E> getBuilder() {
		return this.builder;
	}

	/**
	 * Obtains the preference identifier for styling this {@link AbstractItem}.
	 * 
	 * @return Preference identifier for styling this {@link AbstractItem}.
	 */
	public final String getPreferenceStyleId() {
		return this.getBuilder().getConfigurationPath() + ".style";
	}

}
