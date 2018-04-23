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
public abstract class AbstractChildConfigurableItem<R extends Model, O, P extends Model, PE extends Enum<PE>, M extends Model, E extends Enum<E>>
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
	 * @return Prototype. May be <code>null</code> if not able to create the item.
	 */
	protected abstract M createPrototype();

	/**
	 * Obtains the {@link Model} instances from the parent {@link Model}.
	 * 
	 * @param parentModel
	 *            Parent {@link Model}.
	 * @return {@link List} of {@link Model} instances.
	 */
	protected abstract List<M> getModels(P parentModel);

	/**
	 * Creates the visual for the {@link Model}.
	 * 
	 * @param model
	 *            {@link Model}.
	 * @param context
	 *            {@link AdaptedModelVisualFactoryContext}.
	 * @return {@link Pane} for the visual.
	 */
	protected abstract Pane createVisual(M model, AdaptedModelVisualFactoryContext<M> context);

	/**
	 * Obtains the change events regarding adding/removing the {@link Model} from
	 * the parent {@link Model}.
	 * 
	 * @return Parent change events.
	 */
	protected abstract PE[] parentChangeEvents();

	/**
	 * Obtains the label for the {@link Model}.
	 * 
	 * @param model
	 *            {@link Model}.
	 * @return Label for the model.
	 */
	protected abstract String getLabel(M model);

	/**
	 * Obtains the change events specific to the {@link Model}.
	 * 
	 * @return Change events.
	 */
	protected abstract E[] changeEvents();

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
		 * {@link AbstractChildConfigurableItem} instances for the
		 * {@link ChildrenGroup}.
		 */
		private final AbstractChildConfigurableItem<R, O, M, E, ?, ?>[] children;

		/**
		 * Instantiate for a single {@link AbstractChildConfigurableItem}.
		 * 
		 * @param child
		 *            {@link AbstractChildConfigurableItem}.
		 */
		@SuppressWarnings("unchecked")
		public IdeChildrenGroup(AbstractChildConfigurableItem<R, O, M, E, ?, ?> child) {
			this.name = child.getClass().getSimpleName();
			this.children = new AbstractChildConfigurableItem[] { child };
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
				return this.children[0].getModels(parent);
			} else {
				// Include all children together
				List<? extends Model> children = new LinkedList<>();
				for (AbstractChildConfigurableItem child : this.children) {
					children.addAll(child.getModels(parent));
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
				return this.children[0].changeEvents();
			} else {
				// Include all children together
				List children = new LinkedList<>();
				for (AbstractChildConfigurableItem child : this.children) {
					children.addAll(Arrays.asList(child.changeEvents()));
				}
				return (Enum<?>[]) children.toArray(new Enum[children.size()]);
			}
		}

		/**
		 * Obtains the {@link AbstractChildConfigurableItem} instances for the
		 * {@link ChildrenGroup}.
		 * 
		 * @return {@link AbstractChildConfigurableItem} instances for the
		 *         {@link ChildrenGroup}.
		 */
		public AbstractChildConfigurableItem<R, O, M, E, ?, ?>[] getChildren() {
			return this.children;
		}

	}

	/**
	 * Obtains the {@link AbstractChildConfigurableItem} instances.
	 * 
	 * @return {@link AbstractChildConfigurableItem} instances.
	 */
	@SuppressWarnings("unchecked")
	public IdeChildrenGroup[] getChildrenGroups() {
		List<IdeChildrenGroup> children = new LinkedList<>();
		this.loadChildren(children);
		return children.toArray(new AbstractChildConfigurableItem.IdeChildrenGroup[children.size()]);
	}

	/**
	 * Loads the {@link IdeChildrenGroup} instances.
	 * 
	 * @param childGroups
	 *            {@link IdeChildrenGroup} instances.
	 */
	protected abstract void loadChildren(List<IdeChildrenGroup> childGroups);

	/**
	 * Creates the {@link AdaptedChildBuilder}.
	 * 
	 * @param childrenGroup
	 *            {@link ChildrenGroupBuilder}.
	 * @return Child {@link AdaptedChildBuilder}.
	 */
	public AdaptedChildBuilder<R, O, M, E> createChild(ChildrenGroupBuilder<R, O> childrenGroup) {

		// Add the child
		AdaptedChildBuilder<R, O, M, E> child = childrenGroup.addChild(this.createPrototype(),
				(model, ctx) -> this.createVisual(model, ctx));
		child.label((model) -> this.getLabel(model), this.changeEvents());

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