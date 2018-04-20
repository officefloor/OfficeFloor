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

import java.util.List;
import java.util.function.Consumer;

import org.eclipse.gef.mvc.fx.operations.ITransactionalOperation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import javafx.scene.layout.Pane;
import net.officefloor.eclipse.configurer.AbstractConfigurerRunnable;
import net.officefloor.eclipse.configurer.CloseListener;
import net.officefloor.eclipse.configurer.ConfigurationBuilder;
import net.officefloor.eclipse.configurer.Configurer;
import net.officefloor.eclipse.editor.AdaptedModelVisualFactoryContext;
import net.officefloor.eclipse.editor.AdaptedParentBuilder;
import net.officefloor.eclipse.editor.AdaptedRootBuilder;
import net.officefloor.eclipse.editor.ChangeExecutor;
import net.officefloor.eclipse.editor.DefaultImages;
import net.officefloor.eclipse.ide.ConfigurableItem;
import net.officefloor.eclipse.osgi.OfficeFloorOsgiBridge;
import net.officefloor.model.Model;
import net.officefloor.model.change.Change;

/**
 * Abstract {@link ConfigurableItem}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractParentConfigurableItem<R extends Model, RE extends Enum<RE>, O, M extends Model, E extends Enum<E>, I>
		extends AbstractConfigurerRunnable implements ConfigurableItem<I> {

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
	protected abstract List<M> getModels(R parentModel);

	/**
	 * Creates the visual for the {@link Model}.
	 * 
	 * @param model
	 *            {@link Model}.
	 * @param context
	 *            {@link AdaptedModelVisualFactoryContext}.
	 * @return {@link Pane} for the visual.
	 */
	protected abstract Pane createVisual(M model, AdaptedModelVisualFactoryContext context);

	/**
	 * Obtains the change events regarding adding/removing the {@link Model} from
	 * the root {@link Model}.
	 * 
	 * @return Root change events.
	 */
	protected abstract RE[] rootChangeEvents();

	/**
	 * Obtains the change events specific to the {@link Model}.
	 * 
	 * @return Change events.
	 */
	protected abstract E[] changeEvents();

	/**
	 * Obtains the label for the {@link Model}.
	 * 
	 * @param model
	 *            {@link Model}.
	 * @return Label for the model.
	 */
	protected abstract String getLabel(M model);

	/**
	 * Creates an item from the {@link Model}.
	 * 
	 * @param model
	 *            {@link Model}. May be <code>null</code> if create a {@link Model}.
	 * @return Item.
	 */
	protected abstract I createItem(M model);

	/**
	 * Context for {@link Model} of {@link ConfigurableItem}.
	 */
	public static interface ConfigurableModelContext<O, M> {

		/**
		 * Obtains the operations.
		 * 
		 * @return Operations.
		 */
		O getOperations();

		/**
		 * Obtains the {@link Model}.
		 * 
		 * @return {@link Model}. Will be <code>null</code> if create the {@link Model}.
		 */
		M getModel();

		/**
		 * Executes the {@link Change}.
		 * 
		 * @param change
		 *            {@link Change}.
		 */
		void execute(Change<M> change);
	}

	/**
	 * Loads the common configuration. This is always invoked first before specific
	 * add/refactor configuration.
	 *
	 * @param builder
	 *            {@link ConfigurationBuilder}.
	 * @param context
	 *            {@link ConfigurableItemContext}.
	 */
	protected abstract void loadCommonConfiguration(ConfigurationBuilder<I> builder,
			ConfigurableModelContext<O, M> context);

	/**
	 * Loads the add configuration.
	 * 
	 * @param builder
	 *            {@link ConfigurationBuilder}.
	 * @param context
	 *            {@link ConfigurableItemContext}.
	 */
	protected abstract void loadAddConfiguration(ConfigurationBuilder<I> builder,
			ConfigurableModelContext<O, M> context);

	/**
	 * Loads the refactor configuration.
	 * 
	 * @param builder
	 *            {@link ConfigurationBuilder}.
	 * @param context
	 *            {@link ConfigurableItemContext}.
	 */
	protected abstract void loadRefactorConfiguration(ConfigurationBuilder<I> builder,
			ConfigurableModelContext<O, M> context);

	/**
	 * Deletes the {@link Model}.
	 * 
	 * @param context
	 *            {@link ConfigurableModelContext}.
	 */
	protected abstract void deleteModel(ConfigurableModelContext<O, M> context);

	/**
	 * Further adapt the {@link AdaptedParentBuilder}.
	 * 
	 * @param parent
	 *            {@link AdaptedParentBuilder}.
	 */
	protected void furtherAdaptParent(AdaptedParentBuilder<R, O, M, E> parent) {
		// Default implementation of nothing further
	}

	/**
	 * Creates the {@link AdaptedParentBuilder}.
	 * 
	 * @return {@link AdaptedParentBuilder}.
	 */
	public AdaptedParentBuilder<R, O, M, E> createAdaptedParent() {

		// Create the prototype
		M prototype = this.createPrototype();

		// Configure the parent
		AdaptedParentBuilder<R, O, M, E> parent = this.context.getRootBuilder().parent(prototype,
				(root) -> this.getModels(root), (adapted, ctx) -> this.createVisual(adapted.getModel(), ctx),
				this.rootChangeEvents());
		parent.label((model) -> this.getLabel(model), this.changeEvents());

		// Determine if can create parent
		if (prototype != null) {
			parent.create((ctx) -> {

				// Obtain details for dialog
				OfficeFloorOsgiBridge bridge = this.context.getOsgiBridge();
				Shell shell = this.context.getParentShell();

				// Obtain details for executing change
				O operations = ctx.getOperations();
				ChangeExecutor executor = ctx.getChangeExecutor();

				// Create the overlay to add external flow
				ctx.overlay((visual) -> {

					// Prepare the parent
					Pane overlay = visual.getOverlayParent();
					overlay.setMinHeight(200);
					overlay.applyCss();

					// Create the configurer
					Configurer<I> configurer = new Configurer<>(bridge, shell);
					ConfigurationBuilder<I> builder = configurer;

					// Create the configurable context
					ConfigurableModelContext<O, M> modelContext = new ConfigurableModelContext<O, M>() {

						@Override
						public O getOperations() {
							return operations;
						}

						@Override
						public M getModel() {
							return null; // no model on create
						}

						@Override
						public void execute(Change<M> change) {

							// Position the added model
							ctx.position(change.getTarget());

							// Execute the change
							executor.execute(change);
						}
					};
					this.loadCommonConfiguration(builder, modelContext);
					this.loadAddConfiguration(builder, modelContext);

					// Configure close
					builder.close(new CloseListener() {

						@Override
						public void cancelled() {
							visual.close();
						}

						@Override
						public void applied() {
							visual.close();
						}
					});

					// Show the configuration
					I item = this.createItem(null);
					configurer.loadConfiguration(item, overlay);
				});
			});
		}

		// Allow editing model
		parent.action((ctx) -> {

			// Obtain details for dialog
			OfficeFloorOsgiBridge bridge = this.context.getOsgiBridge();
			Shell shell = this.context.getParentShell();

			// Obtain details for executing change
			O operations = ctx.getOperations();
			ChangeExecutor executor = ctx.getChangeExecutor();

			// Create the overlay to add external flow
			ctx.overlay((visual) -> {

				// Prepare the parent
				Pane overlay = visual.getOverlayParent();
				overlay.setMinHeight(200);
				overlay.applyCss();

				// Create the configurer
				Configurer<I> configurer = new Configurer<>(bridge, shell);
				ConfigurationBuilder<I> builder = configurer;

				// Create the configurable context
				ConfigurableModelContext<O, M> modelContext = new ConfigurableModelContext<O, M>() {

					@Override
					public O getOperations() {
						return operations;
					}

					@Override
					public M getModel() {
						return ctx.getModel();
					}

					@Override
					public void execute(Change<M> change) {
						executor.execute(change);
					}
				};
				this.loadCommonConfiguration(builder, modelContext);
				this.loadRefactorConfiguration(builder, modelContext);

				// Configure close
				builder.close(new CloseListener() {

					@Override
					public void cancelled() {
						visual.close();
					}

					@Override
					public void applied() {
						visual.close();
					}
				});

				// Show the configuration
				I item = this.createItem(null);
				configurer.loadConfiguration(item, overlay);
			});

		}, DefaultImages.EDIT);

		// Further adapt (so delete action is last action)
		this.furtherAdaptParent(parent);

		// Allow deleting
		parent.action((ctx) -> {
			this.deleteModel(new ConfigurableModelContext<O, M>() {

				@Override
				public O getOperations() {
					return ctx.getOperations();
				}

				@Override
				public M getModel() {
					return ctx.getModel();
				}

				@Override
				public void execute(Change<M> change) {
					ctx.getChangeExecutor().execute(change);
				}
			});
		}, DefaultImages.DELETE);

		// Return the parent
		return parent;
	}

	/*
	 * =============== AbstractConfigurerRunnable =============
	 */

	/**
	 * Invoke to run in main method for external testing.
	 * 
	 * @param rootModel
	 *            Root {@link Model}.
	 * @param ideEditorClass
	 *            {@link AbstractIdeEditor} {@link Class} for this
	 *            {@link AbstractParentConfigurableItem}.
	 * @param decoratePrototype
	 *            Optional decorator of the prototype {@link Model} for refactor
	 *            testing. May be <code>null</code> to use prototype as is.
	 * @throws Exception
	 *             If fails to launch.
	 */
	public void main(R rootModel, Class<? extends AbstractIdeEditor<R, RE, O>> ideEditorClass,
			Consumer<M> decoratePrototype) throws Exception {
		AbstractIdeEditor.launchOutsideWorkbench(() -> {
			AbstractIdeEditor<R, RE, O> ideEditor = ideEditorClass.newInstance();
			this.init(new MainConfigurableContext(ideEditor.createOperations(rootModel), decoratePrototype));
			this.run();
		});
	}

	@Override
	protected void loadConfiguration(Shell shell) {

		// Ensure have context
		if ((this.context == null)
				|| (!(this.context instanceof AbstractParentConfigurableItem.MainConfigurableContext))) {
			throw new IllegalStateException("Must start main through instance main method");
		}
		MainConfigurableContext mainContext = (MainConfigurableContext) this.context;

		// Associate the parent shell
		mainContext.parentShell = shell;

		// Provide separate tabs for add and refactor
		TabFolder folder = new TabFolder(shell, SWT.NONE);

		// Load the add configuration
		TabItem addTab = new TabItem(folder, SWT.NONE);
		addTab.setText("Add");
		Group addGroup = new Group(folder, SWT.NONE);
		addGroup.setLayout(new FillLayout());
		addTab.setControl(addGroup);
		I addItem = this.createItem(null);
		Configurer<I> addConfigurer = new Configurer<>(OfficeFloorOsgiBridge.getClassLoaderInstance(), shell);
		ConfigurationBuilder<I> addBuilder = addConfigurer;
		ConfigurableModelContext<O, M> addContext = new ConfigurableModelContext<O, M>() {

			@Override
			public O getOperations() {
				return mainContext.getOperations();
			}

			@Override
			public M getModel() {
				return null;
			}

			@Override
			public void execute(Change<M> change) {
				mainContext.getChangeExecutor().execute(change);
			}
		};
		this.loadCommonConfiguration(addBuilder, addContext);
		this.loadAddConfiguration(addBuilder, addContext);
		addConfigurer.loadConfiguration(addItem, addGroup);

		// Load the refactor configuration
		TabItem refactorTab = new TabItem(folder, SWT.NONE);
		refactorTab.setText("Refactor");
		Group refactorGroup = new Group(folder, SWT.NONE);
		refactorGroup.setLayout(new FillLayout());
		refactorTab.setControl(refactorGroup);
		M prototype = this.createPrototype();
		if (mainContext.decoratePrototype != null) {
			mainContext.decoratePrototype.accept(prototype);
		}
		I refactorItem = this.createItem(prototype);
		Configurer<I> refactorConfigurer = new Configurer<>(OfficeFloorOsgiBridge.getClassLoaderInstance(), shell);
		ConfigurationBuilder<I> refactorBuilder = refactorConfigurer;
		ConfigurableModelContext<O, M> refactorContext = new ConfigurableModelContext<O, M>() {

			@Override
			public O getOperations() {
				return mainContext.getOperations();
			}

			@Override
			public M getModel() {
				return prototype;
			}

			@Override
			public void execute(Change<M> change) {
				mainContext.getChangeExecutor().execute(change);
			}
		};
		this.loadCommonConfiguration(refactorBuilder, refactorContext);
		this.loadRefactorConfiguration(refactorBuilder, refactorContext);
		refactorConfigurer.loadConfiguration(refactorItem, refactorGroup);
	}

	/**
	 * Main {@link ConfigurableContext} for testing configuration outside Eclipse
	 * IDE.
	 */
	private class MainConfigurableContext implements ConfigurableContext<R, O>, ChangeExecutor {

		/**
		 * Operations.
		 */
		private final O operations;

		/**
		 * {@link Consumer} to decorate the prototype.
		 */
		private final Consumer<M> decoratePrototype;

		/**
		 * Parent {@link Shell}.
		 */
		private Shell parentShell;

		/**
		 * Instantiate.
		 * 
		 * @param operations
		 *            Operations.
		 * @param decoratePrototype
		 *            {@link Consumer} to decorate the prototype.
		 */
		private MainConfigurableContext(O operations, Consumer<M> decoratePrototype) {
			this.operations = operations;
			this.decoratePrototype = decoratePrototype;
		}

		/**
		 * =============== ConfigurableContext ===================
		 */

		@Override
		public AdaptedRootBuilder<R, O> getRootBuilder() {
			throw new IllegalStateException(
					"Should not require " + AdaptedRootBuilder.class.getSimpleName() + " for testing configuration");
		}

		@Override
		public Shell getParentShell() {
			return this.parentShell;
		}

		@Override
		public OfficeFloorOsgiBridge getOsgiBridge() throws Exception {
			return OfficeFloorOsgiBridge.getClassLoaderInstance();
		}

		@Override
		public O getOperations() {
			return operations;
		}

		@Override
		public ChangeExecutor getChangeExecutor() {
			return this;
		}

		/*
		 * ================ ChangeExecutor =======================
		 */

		@Override
		public void execute(Change<?> change) {
			// Log running the change
			System.out.println("Executing change '" + change.getChangeDescription() + "' for target "
					+ change.getTarget().getClass().getName());
		}

		@Override
		public void execute(ITransactionalOperation operation) {
			throw new UnsupportedOperationException(this.getClass().getSimpleName() + " does not support executing "
					+ ITransactionalOperation.class.getSimpleName());
		}
	}

}