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
package net.officefloor.eclipse.ide.editor;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import javafx.scene.layout.Pane;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.eclipse.configurer.AbstractConfigurerRunnable;
import net.officefloor.eclipse.editor.AdaptedChildBuilder;
import net.officefloor.eclipse.editor.AdaptedModelVisualFactoryContext;
import net.officefloor.eclipse.editor.AdaptedRootBuilder;
import net.officefloor.eclipse.editor.ChangeExecutor;
import net.officefloor.eclipse.editor.ChildrenGroup;
import net.officefloor.eclipse.editor.ChildrenGroupBuilder;
import net.officefloor.eclipse.osgi.OfficeFloorOsgiBridge;
import net.officefloor.model.Model;

/**
 * Abstract child {@link ConfigurationItem}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractItem<R extends Model, O, P extends Model, PE extends Enum<PE>, M extends Model, E extends Enum<E>>
		extends AbstractConfigurerRunnable {

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
		 * Obtains the {@link OfficeFloorOsgiBridge}.
		 * 
		 * @return {@link OfficeFloorOsgiBridge}.
		 * @throws Exception
		 *             If fails to obtain the {@link OfficeFloorOsgiBridge}.
		 */
		OfficeFloorOsgiBridge getOsgiBridge() throws Exception;

		/**
		 * Obtains the parent {@link Shell}.
		 * 
		 * @return Parent {@link Shell}.
		 */
		Shell getParentShell();

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
	 * Initialise with {@link ConfigurableContext}.
	 * 
	 * @param context
	 *            {@link ConfigurableContext}.
	 */
	public void init(ConfigurableContext<R, O> context) {
		this.context = context;
	}

	/**
	 * Obtains the {@link ConfigurableContext}.
	 * 
	 * @return {@link ConfigurableContext}.
	 */
	public ConfigurableContext<R, O> getConfigurableContext() {
		return this.context;
	}

	/**
	 * Convenience method to translate list of property items to a
	 * {@link PropertyList}.
	 * 
	 * @param properties
	 *            Property items.
	 * @param getName
	 *            {@link Function} to extract the name from the property item.
	 * @param getValue
	 *            {@link Function} to extract the value from the property item.
	 * @return {@link PropertyList}.
	 */
	protected <PI> PropertyList translateToPropertyList(List<PI> properties, Function<PI, String> getName,
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
	 * Creates the prototype for the item.
	 * 
	 * @return Prototype.
	 */
	protected abstract M prototype();

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
		 * @param extractor
		 *            {@link Function} to extract the {@link Model} instances from the
		 *            parent {@link Model}.
		 * @param extractChangeEvents
		 *            Extract change events.
		 */
		@SafeVarargs
		public IdeExtractor(Function<P, List<M>> extractor, PE... extractChangeEvents) {
			this.extractor = extractor;
			this.extractChangeEvents = extractChangeEvents;
		}

		/**
		 * Extracts the {@link Model} instances from the parent {@link Model}.
		 * 
		 * @param parentModel
		 *            Parent {@link Model}.
		 * @return Extract {@link Model} instances.
		 */
		public List<M> extract(P parentModel) {
			return this.extractor.apply(parentModel);
		}

		/**
		 * Obtains the extract change events.
		 * 
		 * @return Extract change events.
		 */
		public PE[] getExtractChangeEvents() {
			return this.extractChangeEvents;
		}
	}

	/**
	 * Obtains the {@link IdeExtractor} to extract {@link Model} instances from the
	 * parent {@link Model}.
	 * 
	 * @return {@link IdeExtractor}.
	 */
	protected abstract IdeExtractor extract();

	/**
	 * Creates the visual for the {@link Model}.
	 * 
	 * @param model
	 *            {@link Model}.
	 * @param context
	 *            {@link AdaptedModelVisualFactoryContext}.
	 * @return {@link Pane} for the visual.
	 */
	protected abstract Pane visual(M model, AdaptedModelVisualFactoryContext<M> context);

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
		 * @param labeller
		 *            {@link Function} to extract the label from the {@link Model}.
		 * @param labelChangeEvents
		 *            Label change events.
		 */
		@SafeVarargs
		public IdeLabeller(Function<M, String> labeller, E... labelChangeEvents) {
			this.labeller = labeller;
			this.labelChangeEvents = labelChangeEvents;
		}

		/**
		 * Obtains the label from the {@link Model}.
		 * 
		 * @param model
		 *            {@link Model}.
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
		public E[] getLabelChangeEvents() {
			return this.labelChangeEvents;
		}
	}

	/**
	 * Obtains the {@link IdeLabeller} for the {@link Model}.
	 * 
	 * @return {@link IdeLabeller}.
	 */
	protected abstract IdeLabeller label();

	/**
	 * Further adapt the {@link AdaptedChildBuilder}.
	 * 
	 * @param parent
	 *            {@link AdaptedChildBuilder}.
	 */
	protected void furtherAdapt(AdaptedChildBuilder<R, O, M, E> parent) {
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
		 * {@link AbstractItem} instances for the
		 * {@link ChildrenGroup}.
		 */
		private final AbstractItem<R, O, M, E, ?, ?>[] children;

		/**
		 * Instantiate for a single {@link AbstractItem}.
		 * 
		 * @param child
		 *            {@link AbstractItem}.
		 */
		@SuppressWarnings("unchecked")
		public IdeChildrenGroup(AbstractItem<R, O, M, E, ?, ?> child) {
			this.name = child.getClass().getSimpleName();
			this.children = new AbstractItem[] { child };
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
		 * Obtains the {@link AbstractItem} instances for the
		 * {@link ChildrenGroup}.
		 * 
		 * @return {@link AbstractItem} instances for the
		 *         {@link ChildrenGroup}.
		 */
		public AbstractItem<R, O, M, E, ?, ?>[] getChildren() {
			return this.children;
		}

	}

	/**
	 * Obtains the {@link AbstractItem} instances.
	 * 
	 * @return {@link AbstractItem} instances.
	 */
	@SuppressWarnings("unchecked")
	public IdeChildrenGroup[] getChildrenGroups() {
		List<IdeChildrenGroup> children = new LinkedList<>();
		this.children(children);
		return children.toArray(new AbstractItem.IdeChildrenGroup[children.size()]);
	}

	/**
	 * Loads the {@link IdeChildrenGroup} instances.
	 * 
	 * @param childGroups
	 *            {@link IdeChildrenGroup} instances.
	 */
	protected abstract void children(List<IdeChildrenGroup> childGroups);

	/**
	 * Creates the {@link AdaptedChildBuilder}.
	 * 
	 * @param childrenGroup
	 *            {@link ChildrenGroupBuilder}.
	 * @return Child {@link AdaptedChildBuilder}.
	 */
	public AdaptedChildBuilder<R, O, M, E> createChild(ChildrenGroupBuilder<R, O> childrenGroup) {

		// Add the child
		AdaptedChildBuilder<R, O, M, E> child = childrenGroup.addChild(this.prototype(),
				(model, ctx) -> this.visual(model, ctx));

		// Determine if configured with label
		IdeLabeller labeller = this.label();
		if (labeller != null) {
			child.label((model) -> labeller.getLabel(model), labeller.getLabelChangeEvents());
		}

		// Further adapt the child
		this.furtherAdapt(child);

		// Return the child
		return child;
	}

	/*
	 * ================= AbstractConfigurerRunnable =====================
	 */

	@Override
	protected void loadConfiguration(Shell shell) {
		new Label(shell, SWT.NONE).setText("Configuring " + this.getClass().getName() + " directly not supported");
	}

}