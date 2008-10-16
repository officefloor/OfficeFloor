/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.eclipse.common;

import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.eclipse.OfficeFloorPluginFailure;
import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.common.action.OperationAction;
import net.officefloor.eclipse.common.drag.LocalSelectionTransferDragTargetListener;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.common.persistence.FileConfigurationItem;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.model.Model;
import net.officefloor.repository.ConfigurationItem;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.Tool;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.CommandStackListener;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.palette.ConnectionCreationToolEntry;
import org.eclipse.gef.palette.PaletteGroup;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.SelectionToolEntry;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.gef.tools.SelectionTool;
import org.eclipse.gef.ui.palette.FlyoutPaletteComposite.FlyoutPreferences;
import org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.util.TransferDropTargetListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorInput;

/**
 * Provides an abstract {@link org.eclipse.gef.ui.parts.GraphicalEditor} for the
 * Office Floor items to edit.
 * 
 * @author Daniel
 */
public abstract class AbstractOfficeFloorEditor<M extends Model, E extends EditPart>
		extends GraphicalEditorWithFlyoutPalette implements EditPartFactory {

	/**
	 * Root {@link Model} being edited.
	 */
	protected M rootModel;

	/**
	 * Root {@link EditPart}.
	 */
	protected E rootEditPart;

	/**
	 * Map of {@link Model} type to {@link EditPart} type.
	 */
	protected Map<Class<?>, Class<? extends EditPart>> modelTypeToEditPartTypeMap = new HashMap<Class<?>, Class<? extends EditPart>>();

	/**
	 * Listing of the {@link Operation} instances for the context {@link Menu}.
	 */
	private Operation[] operations;

	/**
	 * {@link PaletteRoot}.
	 */
	protected PaletteRoot paletteRoot = null;

	/**
	 * Initiate the edit domain and the command stack.
	 */
	public AbstractOfficeFloorEditor() {
		// Specify the Edit Domain
		DefaultEditDomain editDomain = new DefaultEditDomain(this);
		editDomain.setDefaultTool(new OfficeFloorSelectionTool());
		editDomain.loadDefaultTool();
		this.setEditDomain(editDomain);

		// Set up the command stack
		CommandStack commandStack = new CommandStack();
		editDomain.setCommandStack(commandStack);
		commandStack.addCommandStackListener(new CommandStackListener() {
			@Override
			public void commandStackChanged(EventObject event) {
				// Update property dependent actions
				AbstractOfficeFloorEditor.this
						.updateActions(AbstractOfficeFloorEditor.this
								.getPropertyActions());

				// Notify change in dirty state
				AbstractOfficeFloorEditor.this.firePropertyChange(PROP_DIRTY);
			}
		});
	}

	/**
	 * <p>
	 * Flags this {@link org.eclipse.ui.IEditorPart} as dirty requiring a save.
	 * <p>
	 * Note this is mainly used for testing.
	 */
	public void flagDirty() {
		this.getCommandStack().execute(new Command() {
		});
	}

	/**
	 * Specifies the Model.
	 * 
	 * @param model
	 *            Model.
	 */
	protected void setCastedModel(M model) {
		this.rootModel = model;
	}

	/**
	 * Obtains the {@link Model}.
	 * 
	 * @return {@link Model}.
	 */
	public M getCastedModel() {
		return this.rootModel;
	}

	/**
	 * Obtains the root {@link EditPart}.
	 * 
	 * @return Root {@link EditPart}.
	 */
	public E getRootEditPart() {
		return this.rootEditPart;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.ui.parts.GraphicalEditor#initializeGraphicalViewer()
	 */
	protected void initializeGraphicalViewer() {
		super.initializeGraphicalViewer();

		// Initialise the graphical viewer
		GraphicalViewer viewer = this.getGraphicalViewer();
		viewer.setRootEditPart(new ScalableFreeformRootEditPart());
		viewer.setKeyHandler(new CommonGraphicalViewerKeyHandler(viewer));

		// Load the model
		viewer.setEditPartFactory(new WrappingEditPartFactory(this
				.createEditPartFactory(), this));
		viewer.setContents(this.getCastedModel());

		// Specify if capable of dropping items into editor
		if (this.isDragTarget()) {
			viewer
					.addDropTargetListener((TransferDropTargetListener) new LocalSelectionTransferDragTargetListener(
							viewer));
		}

		// Initialise the context menu
		this.initialiseContextMenu();
	}

	/**
	 * Initialises the context menu.
	 */
	protected void initialiseContextMenu() {

		// Obtain the listing of operations
		List<Operation> operationList = new LinkedList<Operation>();
		this.populateOperations(operationList);
		this.operations = operationList.toArray(new Operation[0]);

		// Ensure have operations
		if (this.operations.length == 0) {
			// No operations therefore do not provide context menu
			return;
		}

		// Create the context menu
		ContextMenuProvider menuProvider = new ContextMenuProvider(this
				.getGraphicalViewer()) {
			@Override
			public void buildContextMenu(IMenuManager menuManager) {

				// Obtain the location
				Tool tool = AbstractOfficeFloorEditor.this.getEditDomain()
						.getActiveTool();
				Point location;
				if (tool instanceof OfficeFloorSelectionTool) {
					// Obtain location of right-click
					OfficeFloorSelectionTool selectionTool = (OfficeFloorSelectionTool) tool;
					location = selectionTool.getLocation();
				} else {
					// Provide no location
					location = new Point(-1, -1);
				}

				// Obtain the selected edit parts
				List<AbstractOfficeFloorEditPart<?, ?>> selectedEditPartList = new LinkedList<AbstractOfficeFloorEditPart<?, ?>>();
				ISelection selection = AbstractOfficeFloorEditor.this
						.getGraphicalViewer().getSelection();
				IStructuredSelection structuredSelection = (IStructuredSelection) selection;
				for (Iterator<?> iterator = structuredSelection.iterator(); iterator
						.hasNext();) {
					Object selectedItem = iterator.next();

					// Obtain the edit part and add to listing
					AbstractOfficeFloorEditPart<?, ?> editPart = (AbstractOfficeFloorEditPart<?, ?>) selectedItem;
					selectedEditPartList.add(editPart);
				}
				AbstractOfficeFloorEditPart<?, ?>[] selectedEditParts = selectedEditPartList
						.toArray(new AbstractOfficeFloorEditPart[0]);

				// Ensure have selected edit parts
				if (selectedEditParts.length == 0) {
					// Nothing selected, therefore add no actions
					return;
				}

				// Add the appropriate actions
				for (Operation operation : AbstractOfficeFloorEditor.this.operations) {

					// Determine if handles all edit part types selected
					boolean isHandled = true;
					for (AbstractOfficeFloorEditPart<?, ?> editPart : selectedEditParts) {
						boolean isAssignable = false;
						for (Class<? extends EditPart> handledEditPartType : operation
								.getEditPartTypes()) {
							if (handledEditPartType.isAssignableFrom(editPart
									.getClass())) {
								isAssignable = true;
							}
						}
						if (!isAssignable) {
							isHandled = false;
						}
					}

					// Add if handles all model types
					if (isHandled) {
						// Add action for the commands to the menu
						menuManager.add(new OperationAction(
								AbstractOfficeFloorEditor.this
										.getCommandStack(), operation,
								selectedEditParts, location));
					}
				}
			}
		};
		Menu menu = menuProvider.createContextMenu(this.getGraphicalControl());

		// Register the context menu
		this.getGraphicalControl().setMenu(menu);
		this.getEditorSite().registerContextMenu(menuProvider,
				this.getGraphicalViewer());
	}

	/**
	 * Populates the listing of {@link Operation} instances.
	 * 
	 * @param list
	 *            Listing to add {@link Operation} instances.
	 */
	protected abstract void populateOperations(List<Operation> list);

	/**
	 * Allow to override to specify another {@link EditPartFactory}.
	 * 
	 * @return <code>this</code> if not overridden.
	 */
	protected EditPartFactory createEditPartFactory() {

		// Populate the map
		this.modelTypeToEditPartTypeMap.clear();
		this.populateEditPartTypes(this.modelTypeToEditPartTypeMap);

		// Wrap to ensure model is set on the edit part
		return new EditPartFactory() {
			public EditPart createEditPart(EditPart context, Object model) {

				// Determine if wrapping
				EditPart editPart;
				if (model instanceof ModelEditPart) {
					// Allow provide own Edit Part
					editPart = ((ModelEditPart) model).getEditPart();
				} else {
					// Create the Edit Part
					editPart = AbstractOfficeFloorEditor.this.createEditPart(
							context, model);
				}

				// Ensure created an edit part
				if (editPart == null) {
					System.out.println("Unknown model for EditPart - "
							+ model.getClass().getName());
				}

				// Set model on the Edit Part
				editPart.setModel(model);

				// Return the Edit Part
				return editPart;
			}
		};
	}

	/**
	 * Use the model type to {@link EditPart} map to create the appropriate
	 * {@link EditPart} via its default constructor.
	 */
	@SuppressWarnings("unchecked")
	public EditPart createEditPart(EditPart context, Object model) {

		// Ensure have a model
		if (model == null) {
			throw new OfficeFloorPluginFailure(
					"Must be provided a model to create an EditPart");
		}

		// Obtain the edit part type for the model
		Class<EditPart> editPartType = (Class<EditPart>) this.modelTypeToEditPartTypeMap
				.get(model.getClass());

		// Ensure have type
		if (editPartType == null) {
			throw new OfficeFloorPluginFailure("Unknown model '"
					+ model.getClass().getName()
					+ "' to create EditPart for Editor "
					+ this.getClass().getName());
		}

		// Create the instance of the edit part
		EditPart editPart = EclipseUtil.createInstance(editPartType);

		// Determine if created the root edit part
		if (this.getCastedModel().getClass() == model.getClass()) {
			// Specify the root edit part
			this.rootEditPart = (E) editPart;
		}

		// Return the edit part
		return editPart;
	}

	/**
	 * Populates the {@link EditPart} types for their respective model.
	 * 
	 * @param mapping
	 *            Registry to load the mappings.
	 */
	protected abstract void populateEditPartTypes(
			Map<Class<?>, Class<? extends EditPart>> map);

	/**
	 * Indicates if able to be a drop target listener.
	 * 
	 * @return <code>true</code> if this Editor is able to be a drop target.
	 */
	protected abstract boolean isDragTarget();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#setInput(org.eclipse.ui.IEditorInput)
	 */
	protected void setInput(IEditorInput input) {
		super.setInput(input);

		// Obtain the configuration
		FileConfigurationItem configuration = new FileConfigurationItem(this
				.getEditorInput());

		// Retrieve the Model
		try {
			this.setCastedModel(this.retrieveModel(configuration));
		} catch (Exception ex) {
			// Propagate failure
			throw new OfficeFloorPluginFailure(ex);
		}

		// Specify Title of editor
		this.setPartName(configuration.getFileName());
	}

	/**
	 * Retrieves the Model.
	 * 
	 * @param configuration
	 *            Configuration of the Model.
	 * @return Model to be editted.
	 * @throws Exception
	 *             If fails to obtain the Model.
	 */
	protected abstract M retrieveModel(ConfigurationItem configuration)
			throws Exception;

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.
	 * IProgressMonitor)
	 */
	public void doSave(IProgressMonitor monitor) {
		// Obtain the configuration
		FileConfigurationItem configuration = new FileConfigurationItem(this
				.getEditorInput(), monitor);

		try {
			// Store the Model
			this.storeModel(this.getCastedModel(), configuration);

			// Successfully saved, so flag not dirty
			this.getCommandStack().markSaveLocation();

		} catch (Exception ex) {
			// Propagate failure
			throw new OfficeFloorPluginFailure(ex);
		}
	}

	/**
	 * Stores the Model.
	 * 
	 * @param model
	 *            Model to be stored.
	 * @param configuration
	 *            Configuration of the Model.
	 * @throws Exception
	 *             If fails to store the Model.
	 */
	protected abstract void storeModel(M model, ConfigurationItem configuration)
			throws Exception;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#doSaveAs()
	 */
	public void doSaveAs() {
		// Provide empty method implementation.
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
	 */
	public boolean isSaveAsAllowed() {
		// Always able to save
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette#
	 * getPalettePreferences()
	 */
	protected FlyoutPreferences getPalettePreferences() {
		return new CommonFlyoutPreferences();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette#getPaletteRoot
	 * ()
	 */
	protected PaletteRoot getPaletteRoot() {
		// Create the root (if not created)
		if (this.paletteRoot == null) {
			this.paletteRoot = new PaletteRoot();

			// Add the selection tools
			PaletteGroup selectionGroup = new PaletteGroup("Select");
			selectionGroup.add(new SelectionToolEntry());
			paletteRoot.add(selectionGroup);

			// Add the connection group
			PaletteGroup connectionGroup = new PaletteGroup("Connection");
			connectionGroup.add(new ConnectionCreationToolEntry("Connection",
					"conn", new CreationFactory() {

						public Object getNewObject() {
							// Connection determined in command
							return null;
						}

						public Object getObjectType() {
							// Connection determined in command
							return null;
						}
					}, null, null));
			paletteRoot.add(connectionGroup);

			// Initialise remaining palette root
			this.initialisePaletteRoot();
		}

		// Return the Root
		return paletteRoot;
	}

	/**
	 * Override to initialise the {@link PaletteRoot}.
	 * 
	 * @see #getPaletteRoot()
	 */
	protected void initialisePaletteRoot() {
		// Do nothing
	}

	/**
	 * {@link SelectionTool} to allow obtaining the location of selection.
	 */
	private static class OfficeFloorSelectionTool extends SelectionTool {

		@Override
		public Point getLocation() {
			return super.getLocation();
		}
	}

	/**
	 * Wraps the {@link EditPartFactory} to provide additional details to the
	 * {@link EditPart} instances created.
	 */
	private class WrappingEditPartFactory implements EditPartFactory {

		/**
		 * {@link EditPartFactory} that is being wrapped.
		 */
		protected final EditPartFactory editPartFactory;

		/**
		 * Editor.
		 */
		protected final AbstractOfficeFloorEditor<?, ?> editor;

		/**
		 * Initiate with the {@link EditPartFactory} being wrapped.
		 * 
		 * @param editPartFactory
		 *            {@link EditPartFactory} to be wrapped.
		 * @param editor
		 *            Editor.
		 */
		public WrappingEditPartFactory(EditPartFactory editPartFactory,
				AbstractOfficeFloorEditor<?, ?> editor) {
			// Store state
			this.editPartFactory = editPartFactory;
			this.editor = editor;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.gef.EditPartFactory#createEditPart(org.eclipse.gef.EditPart
		 * , java.lang.Object)
		 */
		public EditPart createEditPart(EditPart context, Object model) {
			// Create the Edit Part
			EditPart editPart = this.editPartFactory.createEditPart(context,
					model);

			// Enrich the edit part
			if (editPart instanceof AbstractOfficeFloorEditPart) {
				AbstractOfficeFloorEditPart<?, ?> officeFloorEditPart = (AbstractOfficeFloorEditPart<?, ?>) editPart;

				// Specify details on edit part
				officeFloorEditPart.setOfficeFloorEditor(this.editor);
			}

			// Return the Edit Part
			return editPart;
		}
	}
}
